package com.WorkCollabration.App;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;





import java.io.File;
import java.util.ArrayList;






import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


public class Myaccpage extends Fragment {
	//ListView settingslist;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.activity_myaccountpage, container, false);
		TextView username=(TextView)rootView.findViewById(R.id.accname);
		TextView emailid=(TextView)rootView.findViewById(R.id.emailname);
		TextView status=(TextView)rootView.findViewById(R.id.statusUpdate);
		ImageView prof=(ImageView)rootView.findViewById(R.id.profilePicture);
		username.setText(MainActivity.globalUserName);
		emailid.setText(MainActivity.globalEmailId);
		status.setText(MainActivity.globalStatus);
		
		//System.out.println();
//		String location="/storage/emulated/0/OnlineChat/1021021001448798140IMG_20151129_035448274.jpg";
		if(MainActivity.globalProf!=null){
			File imgFile = new  File(MainActivity.globalProf);
		    if(imgFile.exists())
		    {
		    	System.out.println("UUURRRRIII****location");
		       // prof.setImageURI(Uri.fromFile(imgFile));

		    }
		}
		
	    
		return rootView;
	}
}
