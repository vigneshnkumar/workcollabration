package com.WorkCollabration.App;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Pulls users, contacts, chat rooms and messages from the backend into the
 * local SQLite cache. Sync jobs run one at a time on a background thread
 * (this replaces the deprecated AsyncTask, whose default executor was serial too).
 */
public final class ASynchronousTasks {
	private static final String TAG = "ASynchronousTasks";
	static final String DEFAULT_DATE = "2015-08-23 23:23:02";
	private static final ExecutorService SYNC_EXECUTOR = Executors.newSingleThreadExecutor();

	private ASynchronousTasks() {
	}

	public static void updateUsersTable(Context context, SharedPreferences userId) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("MaxDate", MainActivity.globalLastDate);
		params.put("FromId", Integer.toString(MainActivity.globalUserId));
		try {
			JSONObject jsonObject = new JSONObject(ApiClient.postForm(ApiConfig.endpoint("A_UserDetails.php"), params));
			if (jsonObject.optInt("Result", 0) == 0) {
				return;
			}
			JSONObject details = new JSONObject(jsonObject.getString("UserDetails"));
			String localPath = getLocalPath(context, details.optString("UserPictureLink", null));
			String statusUpdate = details.optString("StatusUpdate", null);
			SharedPreferences.Editor editor = userId.edit();
			editor.putInt("userId", MainActivity.globalUserId);
			editor.putString("emailId", MainActivity.globalEmailId);
			editor.putString("userName", MainActivity.globalUserName);
			editor.putString("ProfilePicture", localPath);
			editor.putString("StatusUpdate", statusUpdate);
			editor.putString("dateAdded", details.optString("UpdatedAt", null));
			editor.apply();
			MainActivity.globalStatus = statusUpdate;
			MainActivity.globalProf = localPath;
		} catch (Exception e) {
			Log.w(TAG, "User details sync failed", e);
		}
	}

	public static void updateContactsTable(final Context context, final SQLiteDatabase db) {
		SYNC_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject response = fetchSince(db, "Select max(Contacts_DateAdded) From CONTACTS", "A_ContactDetails.php");
					if (response == null) {
						return;
					}
					JSONArray contacts = response.getJSONArray("Contacts");
					for (int i = 0; i < contacts.length(); i++) {
						JSONObject c = contacts.getJSONObject(i);
						String pictureLink = c.optString("Contacts_PictureLink", null);
						db.execSQL("INSERT OR REPLACE INTO CONTACTS(ContactId,Contacts_UserId,Contacts_FromUserId,Contacts_UserName,"
								+ "Contacts_EmailId,Contacts_Status,Contacts_PictureLink,Contacts_StatusUpdate,Contacts_DateAdded,IsAContact,LocalPath)"
								+ " VALUES (?,?,?,?,?,?,?,?,?,?,?)",
								new Object[] { c.getInt("ContactId"), c.getInt("Contacts_UserId"), c.getInt("Contacts_FromUserId"),
										c.optString("Contacts_UserName"), c.optString("Contacts_EmailId"), c.getInt("Contacts_Status"),
										pictureLink, c.optString("Contacts_StatusUpdate"), c.optString("Contacts_DateAdded"),
										c.getInt("IsAContact"), getLocalPath(context, pictureLink) });
					}
				} catch (Exception e) {
					Log.w(TAG, "Contacts sync failed", e);
				}
			}
		});
	}

	public static void updateChatRoomUsersTable(final Context context, final SQLiteDatabase db) {
		SYNC_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject response = fetchSince(db, "Select max(UpdatedAt) From CHATROOM_USERS", "A_ChatRoomDetails.php");
					if (response == null) {
						return;
					}
					JSONArray rooms = response.getJSONArray("ChatRoomDetails");
					for (int i = 0; i < rooms.length(); i++) {
						JSONObject r = rooms.getJSONObject(i);
						String groupImage = r.optString("GroupImage", null);
						db.execSQL("INSERT OR REPLACE INTO CHATROOM_USERS(ChatRoomId,UserIds,IsGroupChat,GroupName,GroupImage,UpdatedAt,LocalPath)"
								+ " VALUES (?,?,?,?,?,?,?)",
								new Object[] { r.getInt("ChatRoomId"), r.optString("UserIds"), r.getInt("IsGroupChat"),
										r.optString("GroupName"), groupImage, r.optString("UpdatedAt"),
										getLocalPath(context, groupImage) });
					}
				} catch (Exception e) {
					Log.w(TAG, "Chat room sync failed", e);
				}
			}
		});
	}

	public static void updateChatMessagesTable(final Context context, final SQLiteDatabase db) {
		SYNC_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject response = fetchSince(db, "Select max(UpdatedAt) From CHATMESSAGES", "A_ChatMessages.php");
					if (response == null) {
						return;
					}
					JSONArray messages = response.getJSONArray("Messages");
					for (int i = 0; i < messages.length(); i++) {
						JSONObject m = messages.getJSONObject(i);
						String messageLink = m.optString("MessageLink", null);
						db.execSQL("INSERT OR REPLACE INTO CHATMESSAGES(MessageId,ChatRoomId,FromUserId,Message,MessageLink,UpdatedAt,LocalPath)"
								+ " VALUES (?,?,?,?,?,?,?)",
								new Object[] { m.getInt("MessageId"), m.getInt("ChatRoomId"), m.getInt("FromUserId"),
										m.optString("Message"), messageLink, m.optString("UpdatedAt"),
										getLocalPath(context, messageLink) });
					}
				} catch (Exception e) {
					Log.w(TAG, "Chat message sync failed", e);
				}
			}
		});
	}

	/**
	 * Posts the newest local timestamp to {@code script} and returns the parsed
	 * response, or null when the server reports nothing new.
	 */
	private static JSONObject fetchSince(SQLiteDatabase db, String maxDateQuery, String script) throws Exception {
		String lastDate = DEFAULT_DATE;
		Cursor c = db.rawQuery(maxDateQuery, null);
		try {
			if (c.moveToFirst() && !c.isNull(0)) {
				lastDate = c.getString(0);
			}
		} finally {
			c.close();
		}
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("MaxDate", lastDate);
		params.put("FromId", Integer.toString(MainActivity.globalUserId));
		JSONObject json = new JSONObject(ApiClient.postForm(ApiConfig.endpoint(script), params));
		return json.optInt("Result", 0) == 0 ? null : json;
	}

	/**
	 * Downloads an uploaded file into the app's media folder and returns its
	 * local path, or null if the link is empty or the download fails.
	 */
	static String getLocalPath(Context context, String pictureLink) {
		String fileName = ApiConfig.fileNameFromLink(pictureLink);
		if (fileName == null) {
			return null;
		}
		File cacheFile = new File(getDataFolder(context), fileName);
		if (cacheFile.isFile() && cacheFile.length() > 0) {
			return cacheFile.getAbsolutePath();
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new BufferedInputStream(URI.create(ApiConfig.uploadUrl(fileName)).toURL().openStream(), 10240);
			out = new FileOutputStream(cacheFile);
			byte[] buffer = new byte[4096];
			int n;
			while ((n = in.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}
			return cacheFile.getAbsolutePath();
		} catch (IOException e) {
			Log.w(TAG, "Download failed for " + fileName, e);
			cacheFile.delete();
			return null;
		} finally {
			closeQuietly(in);
			closeQuietly(out);
		}
	}

	/**
	 * App-specific media folder. Uses external app storage when available
	 * (no permission needed on API 19+) and falls back to internal storage.
	 */
	public static File getDataFolder(Context context) {
		File dataDir = context.getExternalFilesDir("OnlineChat");
		if (dataDir != null && (dataDir.isDirectory() || dataDir.mkdirs())) {
			return dataDir;
		}
		return context.getFilesDir();
	}

	private static void closeQuietly(java.io.Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException ignored) {
			}
		}
	}
}
