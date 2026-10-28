package com.WorkCollabration.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;
import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatWindow extends Activity{
	private static final String TAG = "ChatWindow";
	private static final int UPDATE_TIME=3000;

	ListView messageList;
	ChatWindowCustomAdapter chatWindowCustomAdapter;
	SQLiteDatabase db;
	String notee;
	EditText message,day,month,year,hour,minute,note;
	TextView title;
	int isGroupChat;
	String chatRoomName;
	String chatRoomImage;
	String userIds;
	int chatroomId;
	ArrayList<Integer> fromUserId;
	ArrayList<String> time;
	ArrayList<String> textMessage;
	ArrayList<String> messageLink;
	HashMap<Integer,String> UserNames;
	Button sendButton;
	ImageView camera;
	ImageView audio;
	EditText chatBox;

	private final Handler refreshHandler = new Handler(Looper.getMainLooper());
	private final Runnable refreshMessages = new Runnable() {
		@Override
		public void run() {
			updateMessageList();
			refreshHandler.postDelayed(this, UPDATE_TIME);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle=getIntent().getExtras();
		if (bundle==null){
			Log.w(TAG, "Opened without a chat room; returning to home");
			openConversationsPage(null);
			return;
		}
		chatroomId=bundle.getInt("chatRoomId");
		isGroupChat=bundle.getInt("isAGroupChat");
		int friendId=bundle.getInt("friendId");

		setContentView(R.layout.activity_chatwindow);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		db=Reminderservice.openReminderDb(this);
		MainActivity.centraldb=openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);

		messageList=(ListView)findViewById(R.id.chatWindow);
		fromUserId=new ArrayList<Integer>();
		time=new ArrayList<String>();
		textMessage=new ArrayList<String>();
		messageLink=new ArrayList<String>();
		UserNames=new HashMap<Integer,String>();
		title=(TextView)findViewById(R.id.title);
		sendButton=(Button)findViewById(R.id.sendButton);
		camera=(ImageView)findViewById(R.id.camera);
		audio=(ImageView)findViewById(R.id.audio);
		chatBox=(EditText)findViewById(R.id.chatBox);
		chatBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				showSendButton(s.toString().trim().length()>0);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		audio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNotesDialog();
			}
		});
		camera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showReminderDialog();
			}
		});
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendTextMessage();
			}
		});

		if (isGroupChat==1){
			loadGroupChatHeader();
		}
		else{
			Cursor d=MainActivity.centraldb.rawQuery("SELECT Contacts_UserName, LocalPath FROM CONTACTS WHERE Contacts_UserId=?",
					new String[]{Integer.toString(friendId)});
			try {
				if (d.moveToFirst()){
					chatRoomName=d.getString(0);
					chatRoomImage=d.getString(1);
				}
			} finally {
				d.close();
			}
		}
		title.setText(chatRoomName==null ? "" : chatRoomName);
		chatWindowCustomAdapter=new ChatWindowCustomAdapter(this,isGroupChat,time,fromUserId,UserNames,textMessage,messageLink);
		messageList.setAdapter(chatWindowCustomAdapter);
		getMessages();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (chatWindowCustomAdapter!=null){
			refreshHandler.postDelayed(refreshMessages, UPDATE_TIME);
		}
	}

	@Override
	protected void onPause() {
		refreshHandler.removeCallbacks(refreshMessages);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		refreshHandler.removeCallbacksAndMessages(null);
		if (db!=null){
			db.close();
		}
		super.onDestroy();
	}

	private void loadGroupChatHeader() {
		Cursor d=MainActivity.centraldb.rawQuery("SELECT GroupName, LocalPath, UserIds FROM CHATROOM_USERS WHERE ChatRoomId=?",
				new String[]{Integer.toString(chatroomId)});
		try {
			if (d.moveToFirst()){
				chatRoomName=d.getString(0);
				chatRoomImage=d.getString(1);
				userIds=d.getString(2);
			}
		} finally {
			d.close();
		}
		if (userIds==null){
			return;
		}
		// UserIds is stored as ";12;34;56;"
		for (String uid:userIds.split(";")){
			String trimmed=uid.trim();
			if (!trimmed.matches("\\d+")){
				continue;
			}
			Cursor cur=MainActivity.centraldb.rawQuery("SELECT Contacts_UserName FROM CONTACTS WHERE Contacts_UserId=?", new String[]{trimmed});
			try {
				if (cur.moveToFirst()){
					UserNames.put(Integer.valueOf(trimmed),cur.getString(0));
				}
			} finally {
				cur.close();
			}
		}
	}

	private void showSendButton(boolean show) {
		sendButton.setVisibility(show ? View.VISIBLE : View.GONE);
		camera.setVisibility(show ? View.GONE : View.VISIBLE);
		audio.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	private void sendTextMessage() {
		String text=chatBox.getText().toString().trim();
		if (text.isEmpty()){
			return;
		}
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("IsTextMessage","true");
		params.put("Message",text);
		params.put("FromUserId",Integer.toString(MainActivity.globalUserId));
		params.put("ChatRoomId",Integer.toString(chatroomId));
		try {
			JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("ChatMessage_Push.php"), params));
			if (jsonObject.optInt("Result", -1)!=0){
				Toast.makeText(getApplicationContext(), "Message not sent, try again", Toast.LENGTH_SHORT).show();
				return;
			}
			chatBox.setText("");
		} catch (Exception e) {
			Log.w(TAG, "Send failed", e);
			Toast.makeText(getApplicationContext(), "Could not reach the server, message not sent", Toast.LENGTH_SHORT).show();
			return;
		}
		showSendButton(false);
		hideSoftKeyboard(this);
		updateMessageList();
		messageList.setSelection(chatWindowCustomAdapter.getCount() - 1);
	}

	private void showNotesDialog() {
		final Dialog dialog = new Dialog(ChatWindow.this);
		dialog.setContentView(R.layout.custom2);
		dialog.setTitle("Notes");
		note = (EditText) dialog.findViewById(R.id.note);
		viewnote();
		note.setText(notee);
		((Button) dialog.findViewById(R.id.dialogButtonOK)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				notee=note.getText().toString();
				sendnote();
				dialog.dismiss();
			}
		});
		((Button) dialog.findViewById(R.id.dialogButtonCancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showReminderDialog() {
		final Dialog dialog = new Dialog(ChatWindow.this);
		dialog.setContentView(R.layout.custom);
		dialog.setTitle("Reminder");
		message = (EditText) dialog.findViewById(R.id.remindername);
		month = (EditText) dialog.findViewById(R.id.month);
		day = (EditText) dialog.findViewById(R.id.day);
		year = (EditText) dialog.findViewById(R.id.year);
		hour = (EditText) dialog.findViewById(R.id.hour);
		minute = (EditText) dialog.findViewById(R.id.minute);
		((Button) dialog.findViewById(R.id.dialogButtonOK)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text=message.getText().toString().trim();
				Long due=ReminderTime.parse(day.getText().toString(), month.getText().toString(), year.getText().toString(),
						hour.getText().toString(), minute.getText().toString(), TimeZone.getDefault());
				if (text.isEmpty()){
					message.setError("Enter a reminder message");
					return;
				}
				if (due==null){
					Toast.makeText(getApplicationContext(), "Enter a valid date (MM DD YYYY) and 24-hour time (HH MM)", Toast.LENGTH_LONG).show();
					return;
				}
				if (ReminderTime.isDue(due, System.currentTimeMillis())){
					Toast.makeText(getApplicationContext(), "Reminder time must be in the future", Toast.LENGTH_LONG).show();
					return;
				}
				db.execSQL("INSERT INTO reminder(groupid,message,day,month,year,hour,minute) VALUES(?,?,?,?,?,?,?)",
						new Object[]{Integer.toString(chatroomId), text, day.getText().toString().trim(), month.getText().toString().trim(),
						year.getText().toString().trim(), hour.getText().toString().trim(), minute.getText().toString().trim()});
				startService(new Intent(ChatWindow.this,Reminderservice.class));
				Toast.makeText(getApplicationContext(), "Reminder set", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		});
		((Button) dialog.findViewById(R.id.dialogButtonCancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void getMessages() {
		Cursor c=MainActivity.centraldb.rawQuery("SELECT FromUserId, UpdatedAt, Message, LocalPath FROM CHATMESSAGES WHERE ChatRoomId=? ORDER BY UpdatedAt, MessageId",
				new String[]{Integer.toString(chatroomId)});
		fromUserId.clear();
		time.clear();
		textMessage.clear();
		messageLink.clear();
		try {
			while (c.moveToNext()){
				fromUserId.add(c.getInt(0));
				time.add(c.getString(1));
				textMessage.add(c.getString(2));
				messageLink.add(c.getString(3));
			}
		} finally {
			c.close();
		}
	}

	public void updateMessageList(){
		ASynchronousTasks.updateChatMessagesTable(this,MainActivity.centraldb);
		getMessages();
		chatWindowCustomAdapter.notifyDataSetChanged();
	}

	public void openConversationsPage(View v){
		MainActivity.centraldb=openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
		ASynchronousTasks.updateChatRoomUsersTable(this,MainActivity.centraldb);
		ASynchronousTasks.updateChatMessagesTable(this,MainActivity.centraldb);
		startActivity(new Intent(ChatWindow.this,Homepage.class));
		finish();
	}

	public static void hideSoftKeyboard(Activity activity) {
		View focus=activity.getCurrentFocus();
		if (focus==null){
			return;
		}
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
	}

	public void sendnote() {
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("IsTextMessage","true");
		params.put("NoteId",Integer.toString(chatroomId));
		params.put("NoteData",notee==null ? "" : notee);
		try {
			JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("InsertNote.php"), params));
			if (jsonObject.optInt("Result", -1)!=0){
				Toast.makeText(getApplicationContext(), "Note not saved, try again", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.w(TAG, "Saving note failed", e);
			Toast.makeText(getApplicationContext(), "Could not reach the server, note not saved", Toast.LENGTH_SHORT).show();
		}
	}

	/** Loads the shared note for this chat room into {@link #notee}. */
	public void viewnote() {
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("IsTextMessage","true");
		params.put("NoteId",Integer.toString(chatroomId));
		notee="";
		try {
			JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("GetNotes.php"), params));
			JSONObject noteData=jsonObject.optJSONObject("NoteData");
			if (noteData!=null){
				notee=noteData.optString("NoteData", "");
			}
		} catch (Exception e) {
			Log.w(TAG, "Loading note failed", e);
		}
	}

	@Override
	public void onBackPressed() {
		openConversationsPage(null);
	}
}
