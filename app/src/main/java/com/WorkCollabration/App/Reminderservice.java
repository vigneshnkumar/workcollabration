package com.WorkCollabration.App;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.IBinder;
import android.util.Log;

/**
 * Polls the local "reminder" table and posts each reminder to its chat room
 * once it is due. Stops itself when no reminders are left.
 */
public class Reminderservice extends Service {
	private static final String TAG = "Reminderservice";
	private static final int POLL_DELAY_MS = 3000;

	private final Handler h = new Handler(Looper.getMainLooper());
	private SQLiteDatabase db;

	private final Runnable poll = new Runnable() {
		@Override
		public void run() {
			if (checkReminders()) {
				h.postDelayed(this, POLL_DELAY_MS);
			} else {
				stopSelf();
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (db == null) {
			db = openReminderDb(this);
		}
		h.removeCallbacks(poll);
		h.postDelayed(poll, POLL_DELAY_MS);
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		h.removeCallbacksAndMessages(null);
		if (db != null) {
			db.close();
		}
		super.onDestroy();
	}

	static SQLiteDatabase openReminderDb(Context context) {
		SQLiteDatabase reminderDb = context.openOrCreateDatabase("Reminder", Context.MODE_PRIVATE, null);
		reminderDb.execSQL("CREATE TABLE IF NOT EXISTS reminder(groupid VARCHAR,message VARCHAR,day VARCHAR,month VARCHAR ,year VARCHAR , hour VARCHAR , minute VARCHAR);");
		return reminderDb;
	}

	/** Sends every due reminder; returns true while reminders are still pending. */
	private boolean checkReminders() {
		long now = System.currentTimeMillis();
		int pending = 0;
		Cursor c = db.rawQuery("SELECT rowid, groupid, message, day, month, year, hour, minute FROM reminder", null);
		try {
			while (c.moveToNext()) {
				Long due = ReminderTime.parse(c.getString(3), c.getString(4), c.getString(5),
						c.getString(6), c.getString(7), TimeZone.getDefault());
				if (due == null) {
					// Unparseable rows can never fire; drop them instead of polling forever.
					db.execSQL("DELETE FROM reminder WHERE rowid=?", new Object[] { c.getLong(0) });
				} else if (ReminderTime.isDue(due, now)) {
					sendMessage(c.getString(1), c.getString(2));
					db.execSQL("DELETE FROM reminder WHERE rowid=?", new Object[] { c.getLong(0) });
				} else {
					pending++;
				}
			}
		} finally {
			c.close();
		}
		return pending > 0;
	}

	private void sendMessage(final String chatRoomId, final String message) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Map<String, String> params = new LinkedHashMap<String, String>();
				params.put("IsTextMessage", "true");
				params.put("Message", message == null ? "" : message);
				params.put("FromUserId", Integer.toString(MainActivity.globalUserId));
				params.put("ChatRoomId", chatRoomId);
				try {
					JSONObject response = new JSONObject(ApiClient.postForm(ApiConfig.endpoint("ChatMessage_Push.php"), params));
					if (response.optInt("Result", -1) != 0) {
						Log.w(TAG, "Server rejected reminder message: " + response);
					}
					MainActivity.centraldb = openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
					ASynchronousTasks.updateChatMessagesTable(Reminderservice.this, MainActivity.centraldb);
				} catch (Exception e) {
					Log.w(TAG, "Could not send reminder", e);
				}
			}
		}, "reminder-send").start();
	}
}
