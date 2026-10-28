package com.WorkCollabration.App;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;
import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Homepage extends Activity {
	private static final String TAG = "Homepage";

	LinearLayout contactss,chatss,myaccc;
	Fragment Contactspage= new Contactspage();
	Fragment Chatpage= new Chatpage();
	Fragment Myaccpage= new Myaccpage();
	TextView conc,chattt,myacccc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homepage);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		MainActivity.centraldb=openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
		ASynchronousTasks.updateChatRoomUsersTable(this,MainActivity.centraldb);
		ASynchronousTasks.updateChatMessagesTable(this,MainActivity.centraldb);
		contactss=(LinearLayout)findViewById(R.id.contacts);
		chatss=(LinearLayout)findViewById(R.id.chat);
		myaccc=(LinearLayout)findViewById(R.id.myacc);
		conc=(TextView)findViewById(R.id.contacctt);
		chattt=(TextView)findViewById(R.id.chattxt);
		myacccc=(TextView)findViewById(R.id.myacctxt);

		RelativeLayout customfrag=(RelativeLayout)findViewById(R.id.customfrag);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		p.addRule(RelativeLayout.BELOW, R.id.custom);
		p.addRule(RelativeLayout.ABOVE, R.id.footer);
		customfrag.setLayoutParams(p);

		selectTab(contactss, conc, Contactspage);
		contactss.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectTab(contactss, conc, Contactspage);
			}
		});
		chatss.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectTab(chatss, chattt, Chatpage);
			}
		});
		myaccc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectTab(myaccc, myacccc, Myaccpage);
			}
		});
	}

	/** Highlights the chosen footer tab and shows its fragment. */
	private void selectTab(LinearLayout tab, TextView label, Fragment page) {
		LinearLayout[] tabs = { contactss, chatss, myaccc };
		TextView[] labels = { conc, chattt, myacccc };
		for (int i = 0; i < tabs.length; i++) {
			boolean selected = tabs[i] == tab;
			tabs[i].setBackgroundResource(selected ? R.color.violet : R.color.white);
			labels[i].setTextColor(selected ? Color.WHITE : Color.BLACK);
		}
		if (page.isAdded()) {
			return;
		}
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, page);
		transaction.commit();
	}

	public void openAddContacts(View v){
		startActivity(new Intent(Homepage.this,AddContact.class));
	}

	public void logOut(View v){
		SharedPreferences.Editor editor = getSharedPreferences("com.onlinechat.app.userInfo", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.apply();
		MainActivity.globalUserId=0;
		MainActivity.globalEmailId="";
		MainActivity.globalStatus="";
		MainActivity.globalUserName="";
		MainActivity.globalProf=null;
		MainActivity.globalLastDate=null;
		stopService(new Intent(this, Reminderservice.class));
		clearDatabase();
		Intent signIn=new Intent(Homepage.this,Loginpage.class);
		// Drop the logged-in screens so Back cannot return to them.
		signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(signIn);
		finish();
	}

	private void clearDatabase() {
		MainActivity.centraldb = openOrCreateDatabase(DatabaseHelper.DATABASE_NAME,Context.MODE_PRIVATE,null);
		MainActivity.centraldb.execSQL("delete from USERS");
		MainActivity.centraldb.execSQL("delete from CONTACTS");
		MainActivity.centraldb.execSQL("delete from CHATROOM_USERS");
		MainActivity.centraldb.execSQL("delete from CHATMESSAGES");
		Reminderservice.openReminderDb(this).execSQL("delete from reminder");
	}

	/** Called by the contacts list: flagType 1 opens a chat, 2 deletes the contact. */
	public void onItemClick(View v,int mPosition, int flagType,String userEmailId, int  friendId) {
		if (flagType==1){
			openDirectChat(friendId);
		}
		else if (flagType==2){
			deleteContact(userEmailId);
		}
	}

	private void openDirectChat(int friendId) {
		// Direct chat room ids are derived from both user ids (server-side convention).
		int chatRoomId = MainActivity.globalUserId>friendId
				? (MainActivity.globalUserId*1000)+friendId
				: (friendId*1000)+MainActivity.globalUserId;
		MainActivity.centraldb=openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor a=MainActivity.centraldb.rawQuery("SELECT 1 FROM CHATROOM_USERS WHERE ChatRoomId=?", new String[]{Integer.toString(chatRoomId)});
		boolean exists;
		try {
			exists=a.moveToFirst();
		} finally {
			a.close();
		}
		if(!exists){
			Map<String, String> params=new LinkedHashMap<String, String>();
			params.put("IsGroupChat","0");
			params.put("UserIds",";"+MainActivity.globalUserId+";"+friendId+";");
			params.put("ChatRoomId",Integer.toString(chatRoomId));
			try {
				JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("ChatRoomCreation.php"), params));
				if (jsonObject.optInt("Result", -1)==0){
					Toast.makeText(getApplicationContext(), "Chat Room Created", Toast.LENGTH_SHORT).show();
				}
				ASynchronousTasks.updateChatRoomUsersTable(this,MainActivity.centraldb);
			} catch (Exception e) {
				Log.w(TAG, "Chat room creation failed", e);
				Toast.makeText(getApplicationContext(), "Could not reach the server, try again later", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		openChatWindow(chatRoomId, friendId, 0);
	}

	private void deleteContact(String userEmailId) {
		MainActivity.centraldb = openOrCreateDatabase(DatabaseHelper.DATABASE_NAME,Context.MODE_PRIVATE,null);
		Integer contactId=null;
		Cursor e=MainActivity.centraldb.rawQuery("SELECT ContactId FROM CONTACTS WHERE Contacts_EmailId=?", new String[]{userEmailId});
		try {
			if (e.moveToFirst()){
				contactId=e.getInt(0);
			}
		} finally {
			e.close();
		}
		if (contactId==null){
			Toast.makeText(getApplicationContext(), "Contact not found", Toast.LENGTH_SHORT).show();
			return;
		}
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("ContactId",Integer.toString(contactId));
		try {
			JSONObject jsonObject = new JSONObject(ApiClient.postForm(ApiConfig.endpoint("DeleteContact.php"), params));
			if (jsonObject.optInt("Result", -1)==0){
				Toast.makeText(getApplicationContext(), "Contact deleted successfully", Toast.LENGTH_SHORT).show();
			}
			ASynchronousTasks.updateContactsTable(this,MainActivity.centraldb);
		}
		catch(Exception ex){
			Log.w(TAG, "Delete contact failed", ex);
			Toast.makeText(getApplicationContext(), "Could not reach the server, try again later", Toast.LENGTH_SHORT).show();
		}
	}

	public void openChatWindow(int chatRoomId, int friendId, int isAGroupChat) {
		Bundle bundle=new Bundle();
		Intent conversations=new Intent(Homepage.this,ChatWindow.class);
		bundle.putInt("chatRoomId", chatRoomId);
		bundle.putInt("friendId", friendId);
		bundle.putInt("isAGroupChat", isAGroupChat);
		conversations.putExtras(bundle);
		startActivity(conversations);
	}

	public void updateStatus(View v){
		Button done=(Button)findViewById(R.id.statusEditDoneButton);
		Button edit=(Button)findViewById(R.id.statusEditButton);
		TextView status=(TextView)findViewById(R.id.statusUpdate);
		TextView tempStatus=(TextView)findViewById(R.id.editStatusUpdate);
		edit.setVisibility(View.GONE);
		done.setVisibility(View.VISIBLE);
		status.setVisibility(View.GONE);
		tempStatus.setVisibility(View.VISIBLE);
		tempStatus.setText(status.getText().toString());
	}

	public void doneStatus(View v){
		Button done=(Button)findViewById(R.id.statusEditDoneButton);
		Button edit=(Button)findViewById(R.id.statusEditButton);
		TextView status=(TextView)findViewById(R.id.statusUpdate);
		TextView tempStatus=(TextView)findViewById(R.id.editStatusUpdate);
		String s=tempStatus.getText().toString().trim();
		done.setVisibility(View.GONE);
		edit.setVisibility(View.VISIBLE);
		tempStatus.setVisibility(View.GONE);
		status.setVisibility(View.VISIBLE);

		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("UserId",Integer.toString(MainActivity.globalUserId));
		params.put("StatusUpdate",s);
		try {
			JSONObject jsonObject = new JSONObject(ApiClient.postForm(ApiConfig.endpoint("StatusUpdate.php"), params));
			if (jsonObject.optInt("Result", -1)==0){
				Toast.makeText(getApplicationContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
			}
			ASynchronousTasks.updateUsersTable(this,getSharedPreferences("com.onlinechat.app.userInfo", Context.MODE_PRIVATE));
		}
		catch(Exception e){
			Log.w(TAG, "Status update failed", e);
			Toast.makeText(getApplicationContext(), "Could not reach the server, status not updated", Toast.LENGTH_SHORT).show();
		}
		status.setText(MainActivity.globalStatus);
	}

	public void editProfilePicture(View v){
		// Profile picture editing was never implemented in the original app.
		Toast.makeText(getApplicationContext(), "Changing the profile picture is not supported yet", Toast.LENGTH_SHORT).show();
	}

	public void groupCreation(View v){
		startActivity(new Intent(Homepage.this,Grouppage.class));
	}
}
