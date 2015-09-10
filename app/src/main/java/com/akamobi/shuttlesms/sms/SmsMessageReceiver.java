package com.akamobi.shuttlesms.sms;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;

import com.akamobi.shuttlesms.engine.Shuttle;

public class SmsMessageReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsMessageReceiver";
    
    public static final String ACTION_SMS_SENT = "com.akamobi.shuttlesms.SMS_SENT_ACTION";

    public void onReceive(Context context, Intent intent) {    	
    	SmsMessage[] incomingMessages;
    	incomingMessages = getIncomingMessages(intent.getExtras());


        for ( int i=0; i<incomingMessages.length; i++ ) {
            String incomingMessage;
            String phoneNumber;
        	
            SmsHelper smsHelper;
            
            incomingMessage = incomingMessages[i].getMessageBody().toString();
    		phoneNumber 	= incomingMessages[i].getOriginatingAddress();
    		
            Shuttle 		shuttle;
            Shuttle.Action 	action;
            String			replyMessage;
            
            shuttle 		= new Shuttle(context);
            action 			= shuttle.Process(phoneNumber, incomingMessage);
            replyMessage 	= shuttle.getReplyMessage();
            
            switch (action) {
            case QUAR:
        		this.abortBroadcast(); //终止广播
        		
        		smsHelper = new SmsHelper(context);
                smsHelper.sendMessage(phoneNumber,replyMessage);
                smsHelper.saveSent(phoneNumber,replyMessage);
                smsHelper.saveInbox(phoneNumber,incomingMessage,1,1);
                
                Log.i(TAG, "challenge msg to " + phoneNumber + ": " + replyMessage);
                break;
            case DROP:
        		this.abortBroadcast(); //终止广播

        		//TODO mLogHelper.saveDrop(phoneNumber,incomingMessage);

                Log.i(TAG, "drop msg from " + phoneNumber + ": " + incomingMessage);
            	break;
            case ACPT:
          		smsHelper = new SmsHelper(context);
                smsHelper.sendMessage(phoneNumber,replyMessage);
                smsHelper.saveSent(phoneNumber,replyMessage);
                Log.i(TAG, "accept msg from " + phoneNumber + ": " + incomingMessage);
            	break;
            case PASS:
            	break;
            }
        }
    }
    
    private SmsMessage[] getIncomingMessages(Bundle extras) {
    	if (extras == null)
           return null;

        Object[] pdus = (Object[]) extras.get("pdus");

        SmsMessage[] incomingMessages = new SmsMessage[pdus.length];

        for (int i = 0; i < pdus.length; i++) {
            incomingMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
          
        return incomingMessages;
    }

	String getDisplayName(final Context context, final String phoneNumber) 
    {  
		Uri uri = Uri.withAppendedPath(	PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber) );
		String[] projection = {PhoneLookup.DISPLAY_NAME,PhoneLookup._ID};

		ContentResolver resolver = context.getContentResolver();
		Cursor c = resolver.query( uri, projection, null, null,null);
		
		if ( c.getCount()<=0 )
			return null;
		
		String displayName;
		
		try {
		    c.moveToFirst();
		    displayName = c.getString(c.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
		} finally {
		    c.close();
		}
		
		return displayName;
    }
}
