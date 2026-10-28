package com.WorkCollabration.App;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.Test;

public class ReminderTimeTest {

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Test
	public void parsesValidDateAs24HourTime() {
		// 2026-10-28 14:05 UTC
		assertEquals(Long.valueOf(1793196300000L), ReminderTime.parse("28", "10", "2026", "14", "05", UTC));
	}

	@Test
	public void toleratesWhitespaceAndLeadingZeros() {
		assertNotNull(ReminderTime.parse(" 01 ", "02", "2027", "00", "00", UTC));
	}

	@Test
	public void rejectsMissingOrNonNumericFields() {
		// The original service called Integer.parseInt on these and crashed.
		assertNull(ReminderTime.parse(null, "10", "2026", "14", "05", UTC));
		assertNull(ReminderTime.parse("", "10", "2026", "14", "05", UTC));
		assertNull(ReminderTime.parse("ab", "10", "2026", "14", "05", UTC));
		assertNull(ReminderTime.parse("28", "10", "2026", "2pm", "05", UTC));
		assertNull(ReminderTime.parse("-1", "10", "2026", "14", "05", UTC));
	}

	@Test
	public void rejectsOutOfRangeValues() {
		assertNull(ReminderTime.parse("31", "02", "2027", "10", "00", UTC));
		assertNull(ReminderTime.parse("29", "02", "2027", "10", "00", UTC));
		assertNotNull(ReminderTime.parse("29", "02", "2028", "10", "00", UTC)); // leap year
		assertNull(ReminderTime.parse("10", "13", "2027", "10", "00", UTC));
		assertNull(ReminderTime.parse("10", "00", "2027", "10", "00", UTC));
		assertNull(ReminderTime.parse("10", "10", "2027", "24", "00", UTC));
		assertNull(ReminderTime.parse("10", "10", "2027", "10", "60", UTC));
		assertNull(ReminderTime.parse("10", "10", "1969", "10", "00", UTC));
	}

	@Test
	public void dueOnceTimeIsReached() {
		long t = ReminderTime.parse("28", "10", "2026", "14", "05", UTC);
		assertFalse(ReminderTime.isDue(t, t - 1));
		assertTrue(ReminderTime.isDue(t, t));
		// A missed minute (service paused) still fires later instead of never.
		assertTrue(ReminderTime.isDue(t, t + 10 * 60 * 1000));
		assertFalse(ReminderTime.isDue(null, t));
	}
}
