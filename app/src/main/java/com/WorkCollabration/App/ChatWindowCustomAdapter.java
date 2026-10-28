package com.WorkCollabration.App;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


//import com.sheikbro.onlinechat.ContactsCustomAdapter.OnItemClickListener;

import com.WorkCollabration.App.ContactsCustomAdapter.ViewHolder;
import com.sheikbro.onlinechat.R;

public class ChatWindowCustomAdapter extends BaseAdapter {

	Activity activity;
	String ContactsName;
	int isGroupChat;
	SeekBar seek_bar;
	Button play_button, pause_button;
	MediaPlayer player;
	Handler seekHandler = new Handler(Looper.getMainLooper());
	ArrayList<Integer> fromUserId; 
	ArrayList<String> time;
	Runnable run;
	ArrayList<String> textMessage;
	ArrayList<String> messageLink;
	ArrayList<String> fromUserName;
	HashMap<Integer,String> UserNames;
	LayoutInflater inflater=null;
	SQLiteDatabase db;
	public ChatWindowCustomAdapter(Activity activity,int isGroupChat,ArrayList<String> time,ArrayList<Integer> fromUserId,HashMap<Integer,String> UserNames,ArrayList<String> textMessage,ArrayList<String> messageLink){
		this.activity=activity;
		this.fromUserId=fromUserId;
		this.textMessage=textMessage;
		this.messageLink=messageLink;
		this.isGroupChat=isGroupChat;
	//	this.fromUserName=fromUserName;
		this.UserNames=UserNames;
		this.time=time;
		inflater= (LayoutInflater )this.activity.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (this.fromUserId.size()<=0){
			return 1;
		}
		return fromUserId.size();
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

		View vi = null;
		vi=inflater.inflate(R.layout.message_list_row,null);
		TextView chatUserName=(TextView)vi.findViewById(R.id.textUserName);
		TextView chatMessage=(TextView)vi.findViewById(R.id.textMessage);
		SeekBar seek_bar=(SeekBar)vi.findViewById(R.id.seek_bar);

		Button play_button=(Button)vi.findViewById(R.id.play_button);
		Button pause_button=(Button)vi.findViewById(R.id.pause_button);
		play_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				player.start();
			}
		});
		pause_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				player.pause();
			}
		});

		//	VideoView vid=(VideoView)vi.findViewById(R.id.vidview);
		TextView chatTime=(TextView)vi.findViewById(R.id.textTime);




		ImageView image=(ImageView)vi.findViewById(R.id.image);
		LinearLayout contentLayout=(LinearLayout)vi.findViewById(R.id.contentLayout);
		RelativeLayout messageLayout=(RelativeLayout)vi.findViewById(R.id.messageLayout);
		if (this.fromUserId.size()<=0){
			chatMessage.setText("No Messages");
		}
		else{
			if (isGroupChat==1){
				//System.out.println("******Printing from user Names"+fromUserName.get(position));
				chatUserName.setVisibility(View.VISIBLE);
				chatUserName.setText(UserNames.get(fromUserId.get(position)));
			}
			if (this.messageLink.get(position)==null||this.messageLink.get(position).equals("null")){
				//System.out.println("success case value *******"+this.textMessage.get(position));
				image.setVisibility(View.GONE);
				chatMessage.setVisibility(View.VISIBLE);
				chatMessage.setText(this.textMessage.get(position));
				
			}
			else{
				chatMessage.setVisibility(View.GONE);
				//System.out.println("Failed case value *******"+this.textMessage.get(position));

				//	System.out.println("UUURRRRIII****checkpoing");
				//	System.out.println("Message link for get position*****"+messageLink.get(position));
				File imgFile = new  File(messageLink.get(position));

				if(messageLink.get(position).endsWith(".3gp"))
				{
					seek_bar.setVisibility(View.VISIBLE);
					play_button.setVisibility(View.VISIBLE);
					pause_button.setVisibility(View.VISIBLE);

					try {
						// Rows are re-rendered on every refresh; release the old player instead of leaking it.
						if (player != null) {
							player.release();
						}
						player = new MediaPlayer();
						player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build());
						player.setDataSource(messageLink.get(position));
						player.prepare();
						seek_bar.setMax(player.getDuration());

						run = new Runnable() {

							@Override
							public void run() {
								seekUpdation();
							}
						};

					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//					vid.setVisibility(View.VISIBLE);
					//					vid.setVideoURI(Uri.fromFile(imgFile));
				}
				else
				{
					image.setVisibility(View.VISIBLE);
					if(imgFile.exists())
					{
						//	System.out.println("UUURRRRIII****checkpoing222");
						image.setImageURI(Uri.fromFile(imgFile));

					}

				}

			}
			chatTime.setText(this.time.get(position));
			if(this.fromUserId.get(position)==MainActivity.globalUserId){
				messageLayout.setGravity(Gravity.RIGHT);                                                                                                      
				contentLayout.setBackgroundColor(Color.WHITE);
				//contentLayout.setBackgroundResource(R.drawable.rightchat);
				//contentLayout.setBackgroundResource(R.drawable.online);
			}
			else{
				messageLayout.setGravity(Gravity.LEFT);
				//contentLayout.setBackgroundResource(R.drawable.leftchat);
				contentLayout.setBackgroundColor(Color.LTGRAY);
			}

			vi.setOnClickListener(new OnItemClickListener( position,0));
			//chatBox.setOnClickListener(new OnItemClickListener(position,1));
			//sendMessage.setOnClickListener(new OnItemClickListener(position,2));
			//notifyDataSetChanged();
		}
		return vi;
	}
	public void seekUpdation() {

		seek_bar.setProgress(player.getCurrentPosition());
		seekHandler.postDelayed(run, 1000);
	}



	private class OnItemClickListener implements OnClickListener{
		int mPosition;
		ViewHolder viewHolder;
		int flagType;
		public OnItemClickListener(int position, int flagType) {
			// TODO Auto-generated constructor stub
			mPosition=position;
			this.flagType=flagType;
		}
		@Override
		public void onClick(View v) {
			//ChatWindow.hideSoftKeyboard(this.getActivity());
			//			if(flagType==1){
			//				ChatWindow h=(ChatWindow)activity;
			//				h.showSendButton(v);
			//			}
			//			if(flagType==2){
			//				ChatWindow h=(ChatWindow)activity;
			//				h.sendMessage(v);
			//				notifyDataSetChanged();
			//
			//			}
			// TODO Auto-generated method stub
			//notifyDataSetChanged();
		}

	}
}
