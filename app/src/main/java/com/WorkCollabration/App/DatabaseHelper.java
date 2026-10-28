package com.WorkCollabration.App;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Hari on 10/23/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="ONLINECHAT";
    public static final int DATABASE_VERSION=1;

  
    		private static final String CREATE_TABLE_USERS="CREATE TABLE IF NOT EXISTS USERS ("+
    		"UserId int(10) NOT NULL ,"+
    		"UserName varchar(100) NOT NULL,"+
    		"EmailId  varchar(250) NOT NULL,"+
    		"User_Password varchar(100) NOT NULL,"+
    		"UpdatedAt TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
    		"UserStatus int(1)  NOT NULL  DEFAULT '1',"+
    		"UserPictureLink varchar(100) DEFAULT NULL,"+
    		"StatusUpdate varchar(250) DEFAULT NULL,"+
    		"LocalPath varchar(500) DEFAULT NULL,"+
    		"PRIMARY KEY (UserId),"+
    		"UNIQUE (EmailId)"+
    		");";
    
    	
    		private static final String CREATE_TABLE_CHATROOM_USERS="CREATE TABLE IF NOT EXISTS CHATROOM_USERS ("+
    		"ChatRoomId int(10) NOT NULL ,"+
    		"UserIds varchar(500) NOT NULL,"+
    		"IsGroupChat int(1) NOT NULL DEFAULT 0,"+
    		"GroupName varchar(100) DEFAULT NULL,"+
    		"GroupImage varchar(100) DEFAULT NULL,"+
    		"UpdatedAt TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
    		"LocalPath varchar(500) DEFAULT NULL,"+
    		"PRIMARY KEY (ChatRoomId)"+
    		");";
    
    		private static final String CREATE_TABLE_CONTACTS="CREATE TABLE IF NOT EXISTS CONTACTS ("+
			"ContactId int(10) NOT NULL,"+
			"Contacts_UserId int(100) NOT NULL ,"+
			"Contacts_FromUserId int(100) NOT NULL,"+
			"Contacts_UserName varchar(100) NOT NULL,"+
			"Contacts_EmailId varchar(100) NOT NULL,"+
			"Contacts_Status int(1) NOT NULL DEFAULT 1,"+
			"Contacts_PictureLink varchar(100) DEFAULT NULL,"+
			"Contacts_StatusUpdate varchar(250) DEFAULT NULL,"+
			"Contacts_DateAdded TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
			"IsAContact int(1) NOT NULL DEFAULT 1,"+
			"LocalPath varchar(500) DEFAULT NULL,"+
			"PRIMARY KEY (ContactId)"+
			");";
   
    		private static final String CREATE_TABLE_CHATMESSAGES="CREATE TABLE IF NOT EXISTS CHATMESSAGES ("+

    		"MessageId int(10) NOT NULL ,"+
    		"ChatRoomId int(100) NOT NULL,"+
    		"FromUserId int(100) NOT NULL,"+
    		"Message varchar(1000) DEFAULT NULL,"+
    		"MessageLink varchar(250) DEFAULT NULL,"+
    		"UpdatedAt TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
    		"LocalPath varchar(250) DEFAULT NULL,"+
    		"PRIMARY KEY (MessageId)"+
    		");";

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CONTACTS);
        db.execSQL(CREATE_TABLE_CHATROOM_USERS);
        db.execSQL(CREATE_TABLE_CHATMESSAGES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Every table is a cache of server data, so rebuild them all.
        db.execSQL("DROP TABLE IF EXISTS USERS");
        db.execSQL("DROP TABLE IF EXISTS CONTACTS");
        db.execSQL("DROP TABLE IF EXISTS CHATROOM_USERS");
        db.execSQL("DROP TABLE IF EXISTS CHATMESSAGES");
        onCreate(db);
    }

}

