package com.WorkCollabration.App;

import java.util.ArrayList;
import java.util.List;






import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class Contactspage extends Fragment {
	ListView settingsList;
	static ContactsCustomAdapter customAdapter;
	View rootView;
	ArrayList<String> userNames=new ArrayList<String>();
	ArrayList<String> userEmailId=new ArrayList<String>();
	ArrayList<Integer> userStatus=new ArrayList<Integer>();
	ArrayList<Integer> userId=new ArrayList<Integer>();
	ArrayList<String> statusUpdate=new ArrayList<String>();
	ArrayList<String> imagePath=new ArrayList<String>();
	
	ArrayList<Boolean> extend=new ArrayList<Boolean>();
	SQLiteDatabase localdb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_contactspage, container, false);

		settingsList=(ListView)rootView.findViewById(R.id.contactlist);
		
		String contactDetails="Select * from CONTACTS where IsAContact=1";
		System.out.println(contactDetails);
		// DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());
		//  SQLiteDatabase db2 = db.getWritableDatabase();
		this.localdb = getActivity().openOrCreateDatabase(DatabaseHelper.DATABASE_NAME,Context.MODE_PRIVATE,null);
		ASynchronousTasks.updateContactsTable(this.getActivity(),this.localdb);
		userNames.clear();
		userEmailId.clear();
		userStatus.clear();
		userId.clear();
		statusUpdate.clear();
		imagePath.clear();
		extend.clear();
		System.out.println("******Before RAW Query***********");
		Cursor c = this.localdb.rawQuery(contactDetails, null);
		System.out.println("******AfterRAW Query***********"+c);
		if(c!=null)
		{
			Log.d("NOT RETRIVED", "RRr");
			if  (c.moveToFirst()) 
			{
				do {

					userId.add(c.getInt(c.getColumnIndex("Contacts_UserId")));
					userNames.add(c.getString(c.getColumnIndex("Contacts_UserName")));
					userEmailId.add(c.getString(c.getColumnIndex("Contacts_EmailId")));
					userStatus.add(c.getInt(c.getColumnIndex("Contacts_Status")));
					statusUpdate.add(c.getString(c.getColumnIndex("Contacts_StatusUpdate")));
					imagePath.add(c.getString(c.getColumnIndex("LocalPath")));
					extend.add(false);

				}while (c.moveToNext());
			}

		}
		else
		{
			Log.d("NOT RETRIVED", "RRr");
		}
		//this.localdb.close();
		customAdapter=new ContactsCustomAdapter(getActivity(),userNames,extend,userId,userEmailId,userStatus,statusUpdate,imagePath);

		settingsList.setAdapter(customAdapter);
		
		return rootView;
	}


}