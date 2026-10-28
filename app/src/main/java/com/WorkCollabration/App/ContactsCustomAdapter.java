package com.WorkCollabration.App;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsCustomAdapter extends BaseAdapter implements OnClickListener{
	Activity activity;
	ArrayList<String> userName;
	ArrayList<Integer> userStatus=null;
	ArrayList<String> userEmailId=null;
	ArrayList<String> imagePath=null;
	ArrayList<Integer> userId=null;
	ArrayList<String> statusUpdate=null;
	ArrayList<Boolean> extend=null;
	static LayoutInflater inflater=null;
	String signInURL;

	int i=0;
	SQLiteDatabase db;
	public ContactsCustomAdapter(Activity a,ArrayList<String> userName,ArrayList<Boolean> extend,ArrayList<Integer> userId,ArrayList<String> userEmailId,ArrayList<Integer> userStatus,ArrayList<String> statusUpdate,ArrayList<String> imagePath){
		
		this.activity=a;
		this.userName=userName;
		this.userStatus=userStatus;
		this.userId=userId;
		this.statusUpdate=statusUpdate;
		this.imagePath=imagePath;
		this.userEmailId=userEmailId;
		this.extend=extend;
		inflater= (LayoutInflater )this.activity.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (this.userName.size()<=0){
			return 1;
		}
		return userName.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = null;
		ViewHolder view;
		//if (convertView ==null)
		vi=inflater.inflate(R.layout.contact_list_view_row,null);
		view=new ViewHolder();
		view.textUserName=(TextView)vi.findViewById(R.id.textUserName);
		view.textUserStatus=(TextView)vi.findViewById(R.id.textStatusUpdate);
		view.textEmailId=(TextView)vi.findViewById(R.id.textEmailId);
		view.profilePicture=(ImageView)vi.findViewById(R.id.profilePicture);
		view.onlineStatus=(ImageView)vi.findViewById(R.id.onlineStatus);
		view.extendedLayout=(LinearLayout)vi.findViewById(R.id.extendedLayout);
		view.message=(ImageView)vi.findViewById(R.id.imageChat);
		view.delete=(ImageView)vi.findViewById(R.id.imageDelete);
		vi.setTag(view);

		if(userName.size()<=0){
			view.textUserName.setText("No Contacts");
			view.textUserStatus.setText("");
			view.extendedLayout.setVisibility(View.GONE);
		}
		else{
			view.textUserName.setText(this.userName.get(position).toString());
			view.textUserStatus.setText(this.statusUpdate.get(position).toString());
		if(this.extend.get(position)){
			view.extendedLayout.setVisibility(View.VISIBLE);
		}
		else{
			view.extendedLayout.setVisibility(View.GONE);

		}
			//view.profilePicture.setImageResource(new Resources(null, null, null).getIdentifier("com.sheikbro.onlinechat:drawable/ic_launcher",null,null));
			//onclick
			if (Integer.parseInt( this.userStatus.get(position).toString())==1){
				//Resources res=getResources();
				view.onlineStatus.setBackgroundResource(R.drawable.online);}			
			else{
				view.onlineStatus.setBackgroundResource(R.drawable.offline);			

			}
			view.textEmailId.setText(this.userEmailId.get(position).toString());

			//	view.deleteIcon.setImage(R.drawable.deleteIcon);
			// view.chatIcon.setImageResourse(new Resourse().getIdentifier(R.drawable.messageIcon.getImage));
			vi.setOnClickListener(new OnItemClickListener( position, view,0 ));
			view.message.setOnClickListener(new OnItemClickListener(position,view,1));
			view.delete.setOnClickListener(new OnItemClickListener(position,view,2));
		//	notifyDataSetChanged();
		}
		return vi;
	}
	public static class ViewHolder{
		TextView textUserName;
		TextView textUserStatus;
		TextView textEmailId;
		ImageView profilePicture;
		//	ImageView deleteIcon;
		ImageView onlineStatus;
		LinearLayout extendedLayout;
		ImageView message;
		ImageView delete;
		
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
	@SuppressWarnings("unused")
	private class OnItemClickListener implements OnClickListener{
		int mPosition;
		ViewHolder viewHolder;
		int flagType;
		public OnItemClickListener(int position, ViewHolder view, int flag) {
			// TODO Auto-generated constructor stub
			mPosition=position;
			viewHolder=view;
			flagType=flag;
			
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for (int i=0;i<extend.size();i++)
			{
				extend.set(i,false);
			}
			extend.set(mPosition, true);
			notifyDataSetChanged();
			if (flagType==1){//message
				Homepage h=(Homepage)activity;
				h.onItemClick(v, mPosition,flagType,"",userId.get(mPosition));
				System.out.println("*********message icon is clicked : "+mPosition);
				
			}
			if (flagType==2){
				Homepage h=(Homepage)activity;
				h.onItemClick(v, mPosition,flagType,userEmailId.get(mPosition),0);
				notifyDataSetChanged();
			}
		}

	}

}
