package com.WorkCollabration.App;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;



import java.util.ArrayList;




import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class Chatpage extends Fragment {

	ArrayList<Integer> chatRoomId=new ArrayList<Integer>();
	ArrayList<String> conversationImage=new ArrayList<String>();
	ArrayList<String> conversationTitle=new ArrayList<String>();
	ArrayList<String> lastMessage=new ArrayList<String>();
	ArrayList<Integer> isAGroupChat=new ArrayList<Integer>();
	ArrayList<Integer> friendId=new ArrayList<Integer>();
	ArrayList<String> userIds=new ArrayList<String>();
	//SQLiteDatabase MainActivity.centraldb;
	ListView customList;
	ChatPageCustomAdapter adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.activity_chatpage, container, false);
		MainActivity.centraldb=getActivity().openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
		ASynchronousTasks.updateChatRoomUsersTable(this.getActivity(),MainActivity.centraldb);
		customList=(ListView)rootView.findViewById(R.id.conversationList);
		conversationImage.clear();
		conversationTitle.clear();
		isAGroupChat.clear();
		userIds.clear();
		chatRoomId.clear();
		lastMessage.clear();
		friendId.clear();
		MainActivity.centraldb=getActivity().openOrCreateDatabase(DatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
		String query1="Select * from CHATROOM_USERS";
		Cursor d=null;
		d=MainActivity.centraldb.rawQuery(query1,null);
		if(d!=null){
			if(d.moveToFirst()){
				do{
					chatRoomId.add(d.getInt(d.getColumnIndex("ChatRoomId")));
					conversationImage.add(d.getString(d.getColumnIndex("LocalPath")));
					conversationTitle.add(d.getString(d.getColumnIndex("GroupName")));
					isAGroupChat.add(d.getInt(d.getColumnIndex("IsGroupChat")));
					userIds.add(d.getString(d.getColumnIndex("UserIds")));
				}while(d.moveToNext());
			}
		}
		int contactUserId=0;

		for (int i=0;i<chatRoomId.size();i++)
		{

			if (isAGroupChat.get(i)==0)
			{

				String uid=userIds.get(i);
				uid=uid.substring(1, uid.length());
				String []a=uid.split(";");
				for (String b : a){
					int x=Integer.parseInt(b);
					if (x!=MainActivity.globalUserId){
						friendId.add(x);
						contactUserId=x;
					}
					else{
						
					}
				}
				Cursor cur=null;
				String query3="select * from CONTACTS where Contacts_UserId="+contactUserId;
				cur=MainActivity.centraldb.rawQuery(query3,null);
				if(cur!=null){
					if(cur.moveToFirst()){
						do{
							conversationImage.set(i, cur.getString(cur.getColumnIndex("LocalPath")));
							conversationTitle.set(i, cur.getString(cur.getColumnIndex("Contacts_UserName")));
						}while(cur.moveToNext());
					}
					else{

					}
				}
			}
			friendId.add(0);
		}
		for (int j=0;j<chatRoomId.size();j++){

			Cursor c=null;
			String query2="SELECT * FROM CHATMESSAGES  where ChatRoomId="+chatRoomId.get(j)+" ORDER BY UpdatedAt DESC LIMIT 1";
			c=MainActivity.centraldb.rawQuery(query2,null);
			if(c!=null){
				if(c.moveToFirst()){
					do{
						lastMessage.add(c.getString(c.getColumnIndex("Message")));
					}while(c.moveToNext());
				}
				else{
					lastMessage.add("");
				}
			}
			else{
				lastMessage.add("");
			}
		}

		adapter=new ChatPageCustomAdapter(getActivity(),chatRoomId,conversationImage,conversationTitle,lastMessage,isAGroupChat,friendId);
		customList.setAdapter(adapter);
		return rootView;
	}
}
