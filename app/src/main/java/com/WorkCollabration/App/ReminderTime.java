package com.WorkCollabration.App;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Parses the free-text date fields of the reminder dialog. Pure Java so it
 * can be unit tested off-device.
 */
public final class ReminderTime {

	private ReminderTime() {
	}

	/**
	 * Returns the reminder time in epoch millis, or null if any field is
	 * missing, non-numeric or out of range (e.g. 31/02). Hour is 0-23.
	 */
	public static Long parse(String day, String month, String year, String hour, String minute, TimeZone zone) {
		Integer d = toInt(day), mo = toInt(month), y = toInt(year), h = toInt(hour), mi = toInt(minute);
		if (d == null || mo == null || y == null || h == null || mi == null) {
			return null;
		}
		if (mo < 1 || mo > 12 || d < 1 || h < 0 || h > 23 || mi < 0 || mi > 59 || y < 1970 || y > 9999) {
			return null;
		}
		Calendar cal = Calendar.getInstance(zone);
		cal.clear();
		cal.set(Calendar.YEAR, y);
		cal.set(Calendar.MONTH, mo - 1);
		if (d > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			return null;
		}
		cal.set(Calendar.DAY_OF_MONTH, d);
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, mi);
		return cal.getTimeInMillis();
	}

	/** A reminder is due once its time has been reached (so a missed minute still fires). */
	public static boolean isDue(Long reminderMillis, long nowMillis) {
		return reminderMillis != null && reminderMillis <= nowMillis;
	}

	private static Integer toInt(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		if (!t.matches("\\d{1,4}")) {
			return null;
		}
		return Integer.valueOf(t);
	}
}
