package com.WorkCollabration.App;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.WorkCollabration.App.net.ApiClient;
import com.WorkCollabration.App.net.ApiConfig;
import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Grouppage extends Activity{
	ArrayList<Integer> UserId=new ArrayList<Integer>();
	ArrayList<String> UserName=new ArrayList<String>();
	public static ArrayList<Integer> Selection=new ArrayList<Integer>();
	ArrayList<String> LocalPath=new ArrayList<String>();
	ListView groupList;
	CustomGroupPageAdapter groupListAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grouppage);
		// Selection is static and shared with the adapter; reset it so picks from a previous visit don't leak in.
		Selection.clear();
		MainActivity.centraldb=openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		Cursor c=MainActivity.centraldb.rawQuery("SELECT Contacts_UserName, Contacts_UserId, LocalPath FROM CONTACTS WHERE IsAContact=1", null);
		try {
			while (c.moveToNext()){
				UserName.add(c.getString(0));
				UserId.add(c.getInt(1));
				LocalPath.add(c.getString(2));
				Selection.add(0);
			}
		} finally {
			c.close();
		}
		groupList=(ListView)findViewById(R.id.conclist);
		//groupList=getListView();
		groupList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		groupListAdapter=new CustomGroupPageAdapter(this,UserName,UserId,LocalPath,Selection);
		groupList.setAdapter(groupListAdapter);
	//	groupList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
	}
	
	public void createGroup(View v){
		StringBuilder members=new StringBuilder(";");
		int selected=0;
		for (int i = 0; i < UserId.size() && i < Selection.size(); i++) {
			if(Selection.get(i)==1){
				members.append(UserId.get(i)).append(';');
				selected++;
			}
		}
		members.append(MainActivity.globalUserId).append(';');
		String gname=((EditText)findViewById(R.id.groupName)).getText().toString().trim();
		if (gname.isEmpty()){
			Toast.makeText(this,"Enter a group name",Toast.LENGTH_SHORT).show();
			return;
		}
		if (selected==0){
			Toast.makeText(this,"Select at least one contact",Toast.LENGTH_SHORT).show();
			return;
		}
		Map<String, String> params=new LinkedHashMap<String, String>();
		params.put("IsGroupChat","true");
		params.put("UserIds",members.toString());
		params.put("GroupName",gname);
		try {
			JSONObject jsonObject=new JSONObject(ApiClient.postForm(ApiConfig.endpoint("ChatRoomCreation.php"), params));
			if(jsonObject.optInt("Result", -1)!=0){
				Toast.makeText(this,"Group not created, try again",Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this,"Group Created",Toast.LENGTH_SHORT).show();
			ASynchronousTasks.updateChatRoomUsersTable(this, MainActivity.centraldb);
		} catch (Exception e) {
			Log.w("Grouppage", "Group creation failed", e);
			Toast.makeText(this,"Could not reach the server, try again later",Toast.LENGTH_SHORT).show();
			return;
		}
		startActivity(new Intent(Grouppage.this,Homepage.class));
	}
	
	public void cancelGroup(View v){
		Intent back=new Intent(Grouppage.this,Homepage.class);
		startActivity(back);
	}
}
