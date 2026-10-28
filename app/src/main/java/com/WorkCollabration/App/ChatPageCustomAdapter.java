package com.WorkCollabration.App;

import java.util.ArrayList;

//import com.sheikbro.onlinechat.ContactsCustomAdapter.OnItemClickListener;
//import com.sheikbro.onlinechat.ContactsCustomAdapter.ViewHolder;


import com.sheikbro.onlinechat.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatPageCustomAdapter extends BaseAdapter implements OnClickListener{

	Activity activity;
	ArrayList<Integer> chatRoomId;
	ArrayList<String> conversationImage;
	ArrayList<String> conversationTitle;
	ArrayList<String> lastMessage;
	ArrayList<Integer> isAGroupChat;
	ArrayList<Integer> friendId;
	static LayoutInflater inflater=null;
	public ChatPageCustomAdapter(Activity activity,
			ArrayList<Integer> chatRoomId, ArrayList<String> conversationImage,
			ArrayList<String> conversationTitle, ArrayList<String> lastMessage, ArrayList<Integer> isAGroupChat, ArrayList<Integer> friendId) {
		this.activity=activity;
		this.chatRoomId=chatRoomId;
		this.conversationImage=conversationImage;
		this.conversationTitle=conversationTitle;
		this.lastMessage=lastMessage;
		this.isAGroupChat=isAGroupChat;
		this.friendId=friendId;
		inflater= (LayoutInflater )this.activity.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (this.chatRoomId.size()<=0){
			return 1;
		}
		return this.chatRoomId.size();
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi=null;
		vi=inflater.inflate(R.layout.conversations_list_row,null);

		ImageView conImage=(ImageView)vi.findViewById(R.id.conversationImage);
		TextView userName=(TextView)vi.findViewById(R.id.textTitle);
		TextView userMessage=(TextView)vi.findViewById(R.id.textMessage);
		if(chatRoomId.size()<=0){
			userName.setText("No Conversations");

		}
		else{
			userName.setText(conversationTitle.get(position));
			userMessage.setText(lastMessage.get(position));
			vi.setOnClickListener(new OnItemClickListener( position ));
		}
		
		return vi;
	}
	@SuppressWarnings("unused")
	private class OnItemClickListener implements OnClickListener{
		int mPosition;
		public OnItemClickListener(int position) {
			// TODO Auto-generated constructor stub
			mPosition=position;
			
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Homepage h=(Homepage)activity;
			h.openChatWindow(chatRoomId.get(mPosition),friendId.get(mPosition),isAGroupChat.get(mPosition));
		}

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
