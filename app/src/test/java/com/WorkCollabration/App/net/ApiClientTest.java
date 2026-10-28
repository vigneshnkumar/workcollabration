package com.WorkCollabration.App.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

public class ApiClientTest {

	private HttpServer server;
	private final AtomicReference<String> lastBody = new AtomicReference<String>();
	private final AtomicReference<String> lastContentType = new AtomicReference<String>();
	private final AtomicReference<String> lastMethod = new AtomicReference<String>();
	private volatile int status = 200;
	private volatile String reply = "{\"Result\":1}";

	@Before
	public void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
			lastMethod.set(exchange.getRequestMethod());
			lastContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
			lastBody.set(readAll(exchange.getRequestBody()));
			byte[] out = reply.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(status, out.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(out);
			}
		});
		server.start();
	}

	@After
	public void stopServer() {
		server.stop(0);
	}

	private String url() {
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/SignIn.php";
	}

	@Test
	public void postFormSendsUrlEncodedBodyAndReturnsResponse() throws IOException {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("EmailId", "a.user+test@example.com");
		params.put("Password", "p&ss=word");

		String response = ApiClient.postForm(url(), params);

		assertEquals("{\"Result\":1}", response);
		assertEquals("POST", lastMethod.get());
		assertTrue(lastContentType.get().startsWith("application/x-www-form-urlencoded"));
		assertEquals("EmailId=a.user%2Btest%40example.com&Password=p%26ss%3Dword", lastBody.get());
	}

	@Test
	public void postFormHandlesUnicodeAndApostrophes() throws IOException {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("Message", "don't panic – café");
		reply = "{\"Result\":0,\"Echo\":\"café\"}";

		String response = ApiClient.postForm(url(), params);

		assertEquals("Message=don%27t+panic+%E2%80%93+caf%C3%A9", lastBody.get());
		assertTrue("response should be decoded as UTF-8", response.contains("café"));
	}

	@Test
	public void nonSuccessStatusThrowsWithCode() {
		status = 404;
		try {
			ApiClient.postForm(url(), new LinkedHashMap<String, String>());
			fail("expected HttpStatusException");
		} catch (ApiClient.HttpStatusException e) {
			assertEquals(404, e.statusCode);
		} catch (IOException e) {
			fail("expected HttpStatusException but got " + e);
		}
	}

	@Test
	public void unreachableServerThrowsIOException() {
		int port = server.getAddress().getPort();
		server.stop(0);
		try {
			ApiClient.postForm("http://127.0.0.1:" + port + "/x.php", null);
			fail("expected IOException");
		} catch (IOException expected) {
			// connection refused
		}
	}

	@Test
	public void postMultipartSendsFieldsAndFile() throws IOException {
		Map<String, String> fields = new LinkedHashMap<String, String>();
		fields.put("FromUserId", "42");
		fields.put("ChatRoomId", "7");
		byte[] data = "JPEGDATA".getBytes(StandardCharsets.US_ASCII);

		ApiClient.postMultipart(url(), fields, "userfile", "photo.jpg", "image/jpeg", data);

		String ct = lastContentType.get();
		assertTrue(ct, ct.startsWith("multipart/form-data; boundary="));
		String boundary = ct.substring(ct.indexOf('=') + 1);
		String body = lastBody.get();
		assertTrue(body.startsWith("--" + boundary + "\r\n"));
		assertTrue(body.contains("name=\"FromUserId\"\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n42\r\n"));
		assertTrue(body.contains("name=\"ChatRoomId\"\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n7\r\n"));
		assertTrue(body.contains("name=\"userfile\"; filename=\"photo.jpg\"\r\nContent-Type: image/jpeg\r\n\r\nJPEGDATA\r\n"));
		assertTrue(body.endsWith("--" + boundary + "--\r\n"));
	}

	@Test
	public void encodeFormTreatsNullAsEmpty() {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("MaxDate", null);
		params.put("FromId", "3");
		assertEquals("MaxDate=&FromId=3", ApiClient.encodeForm(params));
		assertEquals("", ApiClient.encodeForm(null));
	}

	private static String readAll(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) != -1) {
			out.write(buf, 0, n);
		}
		return new String(out.toByteArray(), StandardCharsets.UTF_8);
	}
}
