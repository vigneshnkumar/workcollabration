package com.WorkCollabration.App.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApiClientTest {

	// A minimal one-request-per-connection HTTP server on java.net only, because Android
	// unit tests compile against android.jar, which hides com.sun.net.httpserver.
	private ServerSocket server;
	private Thread serverThread;
	private final AtomicReference<String> lastBody = new AtomicReference<String>();
	private final AtomicReference<String> lastContentType = new AtomicReference<String>();
	private final AtomicReference<String> lastMethod = new AtomicReference<String>();
	private volatile int status = 200;
	private volatile String reply = "{\"Result\":1}";

	@Before
	public void startServer() throws IOException {
		server = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
		serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!server.isClosed()) {
					try (Socket socket = server.accept()) {
						handle(socket);
					} catch (IOException e) {
						// socket closed by stopServer()
					}
				}
			}
		}, "test-http-server");
		serverThread.setDaemon(true);
		serverThread.start();
	}

	@After
	public void stopServer() throws IOException {
		server.close();
	}

	private void handle(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		String requestLine = readLine(in);
		lastMethod.set(requestLine.substring(0, requestLine.indexOf(' ')));
		int contentLength = 0;
		String contentType = null;
		String header;
		while (!(header = readLine(in)).isEmpty()) {
			int colon = header.indexOf(':');
			String name = header.substring(0, colon).trim().toLowerCase(Locale.ROOT);
			String value = header.substring(colon + 1).trim();
			if (name.equals("content-length")) {
				contentLength = Integer.parseInt(value);
			} else if (name.equals("content-type")) {
				contentType = value;
			}
		}
		byte[] body = new byte[contentLength];
		int read = 0;
		while (read < contentLength) {
			int n = in.read(body, read, contentLength - read);
			if (n < 0) {
				break;
			}
			read += n;
		}
		lastContentType.set(contentType);
		lastBody.set(new String(body, 0, read, StandardCharsets.UTF_8));

		byte[] out = reply.getBytes(StandardCharsets.UTF_8);
		OutputStream os = socket.getOutputStream();
		os.write(("HTTP/1.1 " + status + " X\r\nContent-Type: application/json; charset=UTF-8\r\nContent-Length: "
				+ out.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
		os.write(out);
		os.flush();
	}

	private static String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream line = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1 && b != '\n') {
			if (b != '\r') {
				line.write(b);
			}
		}
		return new String(line.toByteArray(), StandardCharsets.US_ASCII);
	}

	private String url() {
		return "http://127.0.0.1:" + server.getLocalPort() + "/SignIn.php";
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
		int port = server.getLocalPort();
		try {
			stopServer();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
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
}
