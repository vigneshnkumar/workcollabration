package com.WorkCollabration.App.net;

/**
 * Single place for backend locations. The original PHP/MySQL backend was
 * hosted on NJIT student web space and is no longer online; point these
 * constants at a new deployment to bring the app back to life.
 */
public final class ApiConfig {

	public static final String BASE_URL = "https://web.njit.edu/~sm2239/WorkCollaboration/";
	public static final String UPLOADS_URL = "https://harp.njit.edu/~hhm4/UPLOADS/";

	private ApiConfig() {
	}

	/** Full URL for a backend script, e.g. {@code endpoint("SignIn.php")}. */
	public static String endpoint(String script) {
		if (script == null || script.isEmpty()) {
			throw new IllegalArgumentException("script must not be empty");
		}
		return BASE_URL + (script.startsWith("/") ? script.substring(1) : script);
	}

	/** Download URL for a file previously uploaded to the backend. */
	public static String uploadUrl(String fileName) {
		return UPLOADS_URL + fileName;
	}

	/**
	 * Extracts the file name from an upload link returned by the backend.
	 * Returns null for empty / "null" links (the backend sends the literal
	 * string "null" when a user has no picture).
	 */
	public static String fileNameFromLink(String link) {
		if (link == null) {
			return null;
		}
		String trimmed = link.trim();
		if (trimmed.isEmpty() || trimmed.equals("null")) {
			return null;
		}
		int query = trimmed.indexOf('?');
		if (query >= 0) {
			trimmed = trimmed.substring(0, query);
		}
		String name = trimmed.substring(trimmed.lastIndexOf('/') + 1);
		return name.isEmpty() ? null : name;
	}
}
