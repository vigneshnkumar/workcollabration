package com.WorkCollabration.App;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;
import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddContact extends Activity {
	private static final String TAG = "AddContact";
	// Result codes returned by AddContact.php
	static final int RESULT_ADDED = 0;
	static final int RESULT_ALREADY_A_CONTACT = 1;

	EditText editName;
	EditText editEmail;
	String userName;
	String userEmailId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		MainActivity.centraldb=openOrCreateDatabase(DatabaseHelper.DATABASE_NAME,Context.MODE_PRIVATE,null);
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = 
		        new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
	}
	public void openContacts(View v){
		Intent openContacts=new Intent(AddContact.this,Homepage.class);
		startActivity(openContacts);
	}
	public void addContact(View v){
		editName=(EditText)findViewById(R.id.editName);
		editEmail=(EditText)findViewById(R.id.editEmail);
		userName=editName.getText().toString().trim();
		userEmailId=editEmail.getText().toString().trim();
		if(userName.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userEmailId).matches()){
			Toast.makeText(getApplicationContext(), "Enter a name and a valid email address", Toast.LENGTH_SHORT).show();
			return;
		}
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("EmailId",userEmailId);
		params.put("ContactName",userName);
		params.put("FromUserId",Integer.toString(MainActivity.globalUserId));
		int result;
		try {
			result=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("AddContact.php"), params)).getInt("Result");
		} catch (Exception e) {
			Log.w(TAG, "Add contact failed", e);
			Toast.makeText(getApplicationContext(), "Could not reach the server, try again later", Toast.LENGTH_LONG).show();
			return;
		}
		if(result==RESULT_ADDED){
			ASynchronousTasks.updateContactsTable(this,MainActivity.centraldb);
			startActivity(new Intent(AddContact.this,Homepage.class));
		}
		else if(result==RESULT_ALREADY_A_CONTACT){
			Toast.makeText(getApplicationContext(), "Contact Already Exists", Toast.LENGTH_SHORT).show();
		}
		else{
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which==DialogInterface.BUTTON_POSITIVE){
						sendInvite(userEmailId);
					}
				}
			};
			new AlertDialog.Builder(this)
					.setMessage("This user doesn't have a Work Collaboration account yet. Do you want to send an invite?")
					.setPositiveButton("Yes",dialogClickListener)
					.setNegativeButton("No",dialogClickListener)
					.show();
			editName.setText("");
			editEmail.setText("");
		}
	}

	private void sendInvite(String emailId){
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("EmailId",emailId);
		boolean sent=false;
		try {
			sent=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("Invite.php"), params)).getInt("Result")==0;
		} catch (Exception e) {
			Log.w(TAG, "Invite failed", e);
		}
		Toast.makeText(getApplicationContext(), sent ? "Invite sent successfully" : "Invite not sent, try again", Toast.LENGTH_SHORT).show();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//MainActivity.centraldb.close();
		super.onDestroy();
	}
}
