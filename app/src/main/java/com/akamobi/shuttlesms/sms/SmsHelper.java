package com.akamobi.shuttlesms.sms;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.telephony.SmsManager;

public class SmsHelper {
	private Context mCtx;
	
	public SmsHelper(Context context){
		mCtx = context;
	}
	
    int MarkAllAsRead() {
		final String 	UNREAD_SELECTION 	= "(read=0 OR seen=0)";

		ContentResolver cr = mCtx.getContentResolver();
        ContentValues 	cv	= new ContentValues();
        Uri uri;
        
        uri	= Uri.parse("content://sms/");

        cv.put("read", 1);
        cv.put("seen", 1);
        return cr.update(uri, cv, UNREAD_SELECTION, null);
	}

    // 将短信放入收件箱
	public void saveInbox(String phoneNumber, String incomingMessage
			, int read, int seen) 
	{
		ContentResolver contentResolver = mCtx.getContentResolver();
        ContentValues 	values 			= new ContentValues();
        Uri uri;
        
        values.put("address", phoneNumber);
		values.put("read", read);
		values.put("seen", seen);
		values.put("body", incomingMessage);
		
		uri = Uri.parse("content://sms/inbox");
		contentResolver.insert(uri, values);
	}

    // 将短信放入发件箱
	public void saveSent(String phoneNumber, String sentMessage) {
		ContentResolver contentResolver = mCtx.getContentResolver();
        ContentValues 	values 			= new ContentValues();
        Uri uri;
        
        values.put("address", phoneNumber);
		values.put("body", sentMessage);
		values.put("read", 1);
		values.put("seen", 1);
		
		uri = Uri.parse("content://sms/sent");
		contentResolver.insert(uri, values);
		
	}

	public void sendMessage(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
	    ArrayList<String> parts = sms.divideMessage(message);
	    sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
	}

}
