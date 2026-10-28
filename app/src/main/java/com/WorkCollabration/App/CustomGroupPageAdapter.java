

package com.WorkCollabration.App;

import java.io.File;
import java.util.ArrayList;


//import com.sheikbro.onlinechat.ContactsCustomAdapter.OnItemClickListener;

import com.WorkCollabration.App.ContactsCustomAdapter.ViewHolder;
import com.sheikbro.onlinechat.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomGroupPageAdapter extends BaseAdapter{

	Activity activity;
	ArrayList<Integer> userId; 
	ArrayList<String> userName;
	ArrayList<Integer> selection; 
	ArrayList<String> selectedcontacts = new ArrayList<String>();
	ArrayList<String> localPath;
	CheckBox checkbox;
	LayoutInflater inflater=null;
	int pos;
	public CustomGroupPageAdapter(Activity activity,
			ArrayList<String> userName, ArrayList<Integer> userId,
			ArrayList<String> localPath, ArrayList<Integer> selection) {


		this.activity=activity;
		this.userId=userId;
		this.userName=userName;
		this.selection=selection;
		this.localPath=localPath;
		inflater= (LayoutInflater )this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (this.userId.size()<=0){
			return 1;
		}
		return userId.size();

	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = null;
		vi=inflater.inflate(R.layout.group_team_list,null);
		TextView UserName=(TextView)vi.findViewById(R.id.contactUserName);
		ImageView ContactIcon=(ImageView)vi.findViewById(R.id.contactPicture);
		 CheckBox checkbox1=(CheckBox)vi.findViewById(R.id.checkBox);
		 checkbox1.setTag(position);
		if(this.userId.size()<=0){
			UserName.setText("No Contacts");
		}
		else{
			UserName.setText(this.userName.get(position));
			//Iinsert Image here code
			if(this.localPath.get(position)!=null){
				File imgFile = new  File(this.localPath.get(position));
				if(imgFile.exists())
				{
					System.out.println("UUURRRRIII****location");
					ContactIcon.setImageURI(Uri.fromFile(imgFile));

				}
			}

		}
		pos=position;
		//		checkbox.setOnClickListener(new OnItemClickListener(position));
		//		checkbox.setOnCheckedChangeListener((new OnCheckedChangeListener() {
		//
		//			@Override
		//			public void onCheckedChanged(CompoundButton buttonView,
		//					boolean isChecked) {
		//				// TODO Auto-generated method stub
		//				if (isChecked){
		//					Grouppage.Selection.set(pos, 1);
		//				}
		//				else{
		//					Grouppage.Selection.set(pos, 0);
		//				}
		//			}
		//			
		//		}));
		checkbox1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(activity, userName.get((Integer) v.getTag()),Toast.LENGTH_SHORT).show();
				if(Grouppage.Selection.get((Integer) v.getTag())==0){
					Grouppage.Selection.set((Integer) v.getTag(), 1);

				}
				else{
					Grouppage.Selection.set((Integer) v.getTag(), 0);
				}
			}
		});
//		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// TODO Auto-generated method stub
//				if(isChecked)
//				{
//					Toast.makeText(activity, checkbox.getTag().toString(),Toast.LENGTH_SHORT).show();
//				}
//				else
//				{
//
//				}
//
//			}
//		});

		return vi;
	}
	//	private class OnItemClickListener implements OnClickListener{
	//		int mPosition;
	//		
	//		
	//		public OnItemClickListener(int position) {
	//			// TODO Auto-generated constructor stub
	//			mPosition=position;
	//		}
	//		@Override
	//		public void onClick(View v) {
	//			// TODO Auto-generated method stub
	//			if(Grouppage.Selection.get(mPosition)==1){
	//				Grouppage.Selection.set(mPosition, 0);
	//			}
	//			else{
	//				Grouppage.Selection.set(mPosition, 1);
	//			}
	//			
	//			
	//		}
	//
	//	}
}

