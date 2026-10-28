package com.WorkCollabration.App.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal HTTP client built on {@link HttpURLConnection}. Replaces the Apache
 * HttpClient classes that were removed from the Android platform in API 23.
 * Deliberately free of Android dependencies so it can be unit tested on the JVM.
 */
public final class ApiClient {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	static int connectTimeoutMs = 15000;
	static int readTimeoutMs = 30000;

	private ApiClient() {
	}

	/** POSTs {@code application/x-www-form-urlencoded} params and returns the body. */
	public static String postForm(String url, Map<String, String> params) throws IOException {
		byte[] body = encodeForm(params).getBytes(UTF8);
		HttpURLConnection conn = open(url);
		try {
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setFixedLengthStreamingMode(body.length);
			OutputStream out = conn.getOutputStream();
			try {
				out.write(body);
			} finally {
				out.close();
			}
			return readResponse(conn);
		} finally {
			conn.disconnect();
		}
	}

	/**
	 * POSTs a {@code multipart/form-data} request with text fields and one
	 * binary part, and returns the body.
	 */
	public static String postMultipart(String url, Map<String, String> fields, String fileField,
			String fileName, String contentType, byte[] data) throws IOException {
		String boundary = "----WorkCollab" + UUID.randomUUID().toString().replace("-", "");
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		if (fields != null) {
			for (Map.Entry<String, String> e : fields.entrySet()) {
				writeAscii(buf, "--" + boundary + "\r\n");
				writeUtf8(buf, "Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n");
				writeAscii(buf, "Content-Type: text/plain; charset=UTF-8\r\n\r\n");
				writeUtf8(buf, e.getValue() == null ? "" : e.getValue());
				writeAscii(buf, "\r\n");
			}
		}
		if (fileField != null && data != null) {
			writeAscii(buf, "--" + boundary + "\r\n");
			writeUtf8(buf, "Content-Disposition: form-data; name=\"" + fileField
					+ "\"; filename=\"" + (fileName == null ? "file" : fileName) + "\"\r\n");
			writeAscii(buf, "Content-Type: "
					+ (contentType == null ? "application/octet-stream" : contentType) + "\r\n\r\n");
			buf.write(data);
			writeAscii(buf, "\r\n");
		}
		writeAscii(buf, "--" + boundary + "--\r\n");
		byte[] body = buf.toByteArray();

		HttpURLConnection conn = open(url);
		try {
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setFixedLengthStreamingMode(body.length);
			OutputStream out = conn.getOutputStream();
			try {
				out.write(body);
			} finally {
				out.close();
			}
			return readResponse(conn);
		} finally {
			conn.disconnect();
		}
	}

	/** Encodes params as a URL-encoded form body. Null values are sent as empty strings. */
	public static String encodeForm(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : params.entrySet()) {
			if (sb.length() > 0) {
				sb.append('&');
			}
			sb.append(urlEncode(e.getKey())).append('=')
					.append(urlEncode(e.getValue() == null ? "" : e.getValue()));
		}
		return sb.toString();
	}

	private static HttpURLConnection open(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setConnectTimeout(connectTimeoutMs);
		conn.setReadTimeout(readTimeoutMs);
		return conn;
	}

	private static String readResponse(HttpURLConnection conn) throws IOException {
		int code = conn.getResponseCode();
		if (code < 200 || code >= 300) {
			throw new HttpStatusException(code, conn.getURL().toString());
		}
		InputStream in = conn.getInputStream();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int n;
			while ((n = in.read(chunk)) != -1) {
				out.write(chunk, 0, n);
			}
			return new String(out.toByteArray(), UTF8);
		} finally {
			in.close();
		}
	}

	private static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	private static void writeAscii(ByteArrayOutputStream out, String s) {
		byte[] b = s.getBytes(Charset.forName("US-ASCII"));
		out.write(b, 0, b.length);
	}

	private static void writeUtf8(ByteArrayOutputStream out, String s) {
		byte[] b = s.getBytes(UTF8);
		out.write(b, 0, b.length);
	}

	/** Thrown when the server answers with a non-2xx status. */
	public static class HttpStatusException extends IOException {
		private static final long serialVersionUID = 1L;
		public final int statusCode;

		public HttpStatusException(int statusCode, String url) {
			super("HTTP " + statusCode + " from " + url);
			this.statusCode = statusCode;
		}
	}
}
