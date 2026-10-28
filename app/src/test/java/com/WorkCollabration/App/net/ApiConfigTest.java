package com.WorkCollabration.App.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ApiConfigTest {

	@Test
	public void endpointJoinsBaseUrlAndScript() {
		assertEquals(ApiConfig.BASE_URL + "SignIn.php", ApiConfig.endpoint("SignIn.php"));
		assertEquals(ApiConfig.BASE_URL + "SignIn.php", ApiConfig.endpoint("/SignIn.php"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void endpointRejectsEmptyScript() {
		ApiConfig.endpoint("");
	}

	@Test
	public void fileNameFromLinkTakesLastPathSegment() {
		assertEquals("pic_12.jpg", ApiConfig.fileNameFromLink("https://harp.njit.edu/~hhm4/UPLOADS/pic_12.jpg"));
		assertEquals("a.3gp", ApiConfig.fileNameFromLink("http://localhost/online_chat/uploads/sub/a.3gp?v=2"));
		assertEquals("plain.png", ApiConfig.fileNameFromLink("plain.png"));
	}

	@Test
	public void fileNameFromLinkHandlesMissingValues() {
		// The backend sends the literal string "null" for users without a picture.
		assertNull(ApiConfig.fileNameFromLink(null));
		assertNull(ApiConfig.fileNameFromLink(""));
		assertNull(ApiConfig.fileNameFromLink("null"));
		assertNull(ApiConfig.fileNameFromLink("   "));
		assertNull(ApiConfig.fileNameFromLink("https://host/dir/"));
	}

	@Test
	public void shortLinksNoLongerCrash() {
		// The original code used pictureLink.substring(40), which threw for links shorter than 40 chars.
		assertEquals("x.jpg", ApiConfig.fileNameFromLink("http://a/x.jpg"));
	}
}
