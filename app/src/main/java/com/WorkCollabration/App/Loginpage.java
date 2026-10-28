package com.WorkCollabration.App;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;




import org.json.JSONArray;
import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;
import com.sheikbro.onlinechat.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Loginpage extends Activity {
	int userStatus;
	int uId;
	int contactId;
	Timer timer;

	TimerTask timerTask;
	String baselink;
	final Handler handler = new Handler(Looper.getMainLooper());
	int contacts_userId;
	int contacts_FromUserId;
	private static int SPLASH_TIME_OUT = 3500;

	int contacts_status;
	EditText editName;
	private TextView windowsTvOne;
	private TextView windowsTvThree;
	private TextView windowsTvTwo;
	private int screenWidth;
	private AnimatorSet windowsAnimatorSet;
	EditText editEmail;
	EditText editPassword;
	String userName;
	String userEmailId;
	String userPassword;
	String updatedAt;
	String signInQuery;
	String contacts_UserName;
	String contacts_EmailId;
	String contacts_dateAdded;
	String contacts_pictureLink;
	String contacts_statusUpdate;
	String userPictureLink;
	String statusUpdate;
	String contactsQuery;
	JSONArray contacts;
	JSONArray chatRoom;
	JSONArray chatMessage;
	JSONObject jsonObject;
	JSONObject contactDetails;
	JSONObject chatRoomDetails;
	JSONObject chatMessageDetails;

	public void openSignUp(View v){
		Intent signUp=new Intent(Loginpage.this,Signup.class);
		startActivity(signUp);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loginpage);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
					new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		windowsTvOne = (TextView) findViewById(R.id.windowsTvOne);
		windowsTvTwo = (TextView) findViewById(R.id.windowsTvTwo);
		windowsTvThree = (TextView) findViewById(R.id.windowsTvThree);
		windowsTvOne.setVisibility(View.GONE);
		windowsTvTwo.setVisibility(View.GONE);
		windowsTvThree.setVisibility(View.GONE);
		screenWidth = getResources().getDisplayMetrics().widthPixels;



	}
	public void submitSignIn(View v){
		if(!isNetworkAvailable(this)){
			Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
			return;
		}
		editEmail= (EditText)findViewById(R.id.editEmail);
		editPassword= (EditText)findViewById(R.id.editPassword);
		userEmailId=editEmail.getText().toString().trim();
		userPassword=editPassword.getText().toString();
		if(userEmailId.isEmpty() || userPassword.isEmpty()){
			Toast.makeText(getApplicationContext(), "Enter your email and password", Toast.LENGTH_SHORT).show();
			return;
		}
		windowsAnimation();
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("Password",userPassword);
		params.put("EmailId",userEmailId);
		try {
			jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("SignIn.php"), params));
			if (jsonObject.optInt("Result", 0)==1){
				uId=jsonObject.getInt("UserId");
				String usnm=jsonObject.optString("UserName");
				String emld=jsonObject.optString("EmailId");
				String statUpdt=jsonObject.optString("StatusUpdate", null);
				String prof=ASynchronousTasks.getLocalPath(this, jsonObject.optString("UserPictureLink", null));
				SharedPreferences userId= getSharedPreferences("com.onlinechat.app.userInfo", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = userId.edit();
				editor.putInt("userId", uId);
				editor.putString("emailId", emld);
				editor.putString("userName", usnm);
				editor.putString("StatusUpdate", statUpdt);
				editor.putString("ProfilePicture", prof);
				editor.apply();
				MainActivity.globalUserId=uId;
				MainActivity.globalEmailId=emld;
				MainActivity.globalUserName=usnm;
				MainActivity.globalStatus=statUpdt;
				MainActivity.globalProf=prof;
				Intent homepage=new Intent(Loginpage.this,Homepage.class);
				homepage.putExtra("emailid", userEmailId);
				setUpDatabase();
				startActivity(homepage);
			}
			else{
				stopAnimation();
				Toast.makeText(getApplicationContext(), "Invalid Credentials, Try Again", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.w("Loginpage", "Sign in failed", e);
			stopAnimation();
			Toast.makeText(getApplicationContext(), "Could not reach the server, try again later", Toast.LENGTH_LONG).show();
		}
	}
	private void stopAnimation(){
		if(windowsAnimatorSet!=null){
			windowsAnimatorSet.removeAllListeners();
			windowsAnimatorSet.cancel();
		}
		windowsTvOne.setVisibility(View.GONE);
		windowsTvTwo.setVisibility(View.GONE);
		windowsTvThree.setVisibility(View.GONE);
	}
	public void setUpDatabase(){
		DatabaseHelper db = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase db2 = db.getWritableDatabase();
		insertUserQuery(db2);
		insertContactsQuery(db2);
		insertChatroomQuery(db2);
		insertChatmessageQuery(db2);
	}
	private void insertChatmessageQuery(SQLiteDatabase db2) {
		JSONArray chatMessage=jsonObject.optJSONArray("Chatmessage");
		if(chatMessage==null){
			return;
		}
		for(int i=0;i<chatMessage.length();i++){
			try {
				JSONObject m = chatMessage.getJSONObject(i);
				String messageLink = m.optString("MessageLink", null);
				db2.execSQL("INSERT OR REPLACE INTO CHATMESSAGES (MessageId, ChatRoomId, FromUserId, Message, MessageLink, LocalPath, UpdatedAt) VALUES (?,?,?,?,?,?,?)",
						new Object[]{m.getInt("MessageId"), m.getInt("ChatRoomId"), m.getInt("FromUserId"), m.optString("Message"),
						messageLink, ASynchronousTasks.getLocalPath(this,messageLink), m.optString("UpdatedAt")});
			}
			catch (Exception e) {
				Log.w("Loginpage", "Skipping bad chat message row", e);
			}
		}
	}

	private void insertChatroomQuery(SQLiteDatabase db2) {
		JSONArray chatRoom=jsonObject.optJSONArray("Chatroom");
		if(chatRoom==null){
			return;
		}
		for(int i=0;i<chatRoom.length();i++){
			try {
				JSONObject r = chatRoom.getJSONObject(i);
				String groupImage = r.optString("GroupImage", null);
				db2.execSQL("INSERT OR REPLACE INTO CHATROOM_USERS (ChatRoomId, UserIds, IsGroupChat, GroupName, GroupImage, LocalPath, UpdatedAt) VALUES (?,?,?,?,?,?,?)",
						new Object[]{r.getInt("ChatRoomId"), r.optString("UserIds"), r.getInt("IsGroupChat"), r.optString("GroupName"),
						groupImage, ASynchronousTasks.getLocalPath(this,groupImage), r.optString("UpdatedAt")});
			}
			catch (Exception e) {
				Log.w("Loginpage", "Skipping bad chat room row", e);
			}
		}
	}

	private void insertContactsQuery(SQLiteDatabase db2) {
		JSONArray contacts=jsonObject.optJSONArray("Contacts");
		if(contacts==null){
			return;
		}
		for(int i=0;i<contacts.length();i++){
			try {
				JSONObject c = contacts.getJSONObject(i);
				String pictureLink = c.optString("Contacts_PictureLink", null);
				db2.execSQL("INSERT OR REPLACE INTO CONTACTS (ContactId,Contacts_UserId,Contacts_FromUserId,Contacts_UserName,Contacts_EmailId,Contacts_Status,Contacts_DateAdded,Contacts_PictureLink,Contacts_StatusUpdate,LocalPath,IsAContact) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
						new Object[]{c.getInt("ContactId"), c.getInt("Contacts_UserId"), c.getInt("Contacts_FromUserId"), c.optString("Contacts_UserName"),
						c.optString("Contacts_EmailId"), c.getInt("Contacts_Status"), c.optString("Contacts_DateAdded"), pictureLink,
						c.optString("Contacts_StatusUpdate"), ASynchronousTasks.getLocalPath(this,pictureLink), c.getInt("IsAContact")});
			}
			catch (Exception e) {
				Log.w("Loginpage", "Skipping bad contact row", e);
			}
		}
	}
	public void windowsAnimation() {

		final ValueAnimator valueTvOne_x = ObjectAnimator.ofFloat(windowsTvOne, "x", windowsTvOne.getX() - 40, windowsTvOne.getX() - 50, (screenWidth / 2) + 10f, (screenWidth / 2) + 25f,
				(screenWidth / 2) + 50f//,(screenWidth / 2)+55f//,(screenWidth / 2)+80f//,(screenWidth/2 )+25f,(screenWidth / 2) +30f//, (screenWidth / 2)+35f,(screenWidth / 2)+40f,(screenWidth / 2)+45f //,(screenWidth / 2)+6.6f,(screenWidth / 2)+7.7f,(screenWidth / 2)+8.8f //, (screenWidth / 2)+9,(screenWidth / 2)+10,
				, screenWidth * .92f, screenWidth + 5);

		valueTvOne_x.setDuration(5200);
		valueTvOne_x.setRepeatCount(0);
		valueTvOne_x.setRepeatMode(ValueAnimator.REVERSE);

		valueTvOne_x.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				windowsTvOne.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});

		final ValueAnimator valueTvTwo_x = ObjectAnimator.ofFloat(windowsTvTwo, "x", windowsTvTwo.getX() - 50, (screenWidth / 2.1f) + 10f, (screenWidth / 2.1f) + 25f, (screenWidth / 2.1f) + 50f//, (screenWidth / 2.1f) +55f//, (screenWidth / 2.1f) +80f//, (screenWidth / 2.1f) +25f,(screenWidth / 2.1f) +30f
				, screenWidth * .94f, screenWidth + 5);

		valueTvTwo_x.setDuration(6000);
		valueTvTwo_x.setRepeatCount(0);
		valueTvTwo_x.setStartDelay(200);
		valueTvTwo_x.setRepeatMode(ValueAnimator.REVERSE);

		valueTvTwo_x.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				windowsTvTwo.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});

		final ValueAnimator valueTvThree_x = ObjectAnimator.ofFloat(windowsTvThree, "x", windowsTvThree.getX() - 50, (screenWidth / 2.2f) + 10f, (screenWidth / 2.2f) + 25f, (screenWidth / 2.2f) + 50f//,(screenWidth / 2.2f) +55f//,(screenWidth / 2.2f) +80f//,(screenWidth / 2.2f) +25f,(screenWidth / 2.2f) +30f
				, screenWidth * .94f, screenWidth + 5);

		valueTvThree_x.setDuration(6500);
		valueTvThree_x.setRepeatCount(0);
		valueTvTwo_x.setStartDelay(500);
		valueTvThree_x.setRepeatMode(ValueAnimator.REVERSE);

		valueTvThree_x.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				windowsTvThree.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});

		windowsAnimatorSet = new AnimatorSet();
		windowsAnimatorSet.playTogether(valueTvTwo_x, valueTvThree_x, valueTvOne_x);

		windowsAnimatorSet.start();
		windowsAnimatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {

				windowsAnimatorSet.start();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});

	}
	public void initializeTimerTask() {



		timerTask = new TimerTask() {

			public void run() {



				//use a handler to run a toast that shows the current timestamp

				handler.post(new Runnable() {

					public void run() {

						//get the current timeStamp



					}

				});

			}

		};

	}

	public boolean isNetworkAvailable(final Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		}
		NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
		return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
	}


	private void insertUserQuery(SQLiteDatabase db2) {
		try {
			JSONObject user=jsonObject.getJSONObject("Users");
			String pictureLink=user.optString("UserPictureLink", null);
			// The password is never stored on the device; the column is NOT NULL so an empty value is written.
			db2.execSQL("INSERT OR REPLACE INTO USERS(UserId,UserName,EmailId,User_Password,LocalPath,UpdatedAt) VALUES (?,?,?,?,?,?)",
					new Object[]{uId, user.optString("UserName"), userEmailId, "", ASynchronousTasks.getLocalPath(this,pictureLink), user.optString("UpdatedAt")});
		} catch (Exception e) {
			Log.w("Loginpage", "Could not cache user row", e);
		}
	}

}
