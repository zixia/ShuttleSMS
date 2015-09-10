package com.akamobi.shuttlesms.engine;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

import com.akamobi.shuttlesms.db.CreditDb;

public class CreditJudge {
	//private final String	TAG="CreditJudge";
	
	private Context 		mCtx;
	private CreditDb 		mCreditDb;
	
	CreditJudge(Context context) {
		mCtx		= context;
		mCreditDb 	= new CreditDb(context);
	}
	
	/*
	 * 检查手机号码是否在通讯录中
	 */
	public boolean isNumberInContact(String phoneNumber) {
		Uri uri = Uri.withAppendedPath(	PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber) );
		String[] projection = {PhoneLookup.DISPLAY_NAME,PhoneLookup._ID};

		ContentResolver resolver = mCtx.getContentResolver();
		Cursor c = resolver.query( uri, projection, null, null,null);
		
		if ( c.getCount()>0 )
			return true;

		return false;
	}

	/*
	 * 检查本机机主是否主动给手机号码发过短信
	 */
	public boolean isNumberInSentBox(String phoneNumber) {
		// TODO 如何兼容各种SMS应用程序？
		return false;
	}
	
	
	/*
	 * 手机号码判断
	 */
	public boolean isNumberWhiteList(String phoneNumber) {
		return judgeNumber(phoneNumber, CreditDb.TYPE_WHITENUM);
	}

	public boolean isNumberBlackList(String phoneNumber) {
		return judgeNumber(phoneNumber, CreditDb.TYPE_BLACKNUM);
	}

	/*
	 * 关键字判断
	 */
	public boolean isTextWhiteList(String textMessage) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTextBlackList(String textMessage) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * ADD / DEL
	 */
	public void addNumberBlackList(String phoneNumber) {
		mCreditDb.createCredit(CreditDb.TYPE_BLACKNUM
				, phoneNumber, "XXX");		
	}

	public void addNumberWhiteList(String phoneNumber) {
		mCreditDb.createCredit(CreditDb.TYPE_WHITENUM
				, phoneNumber, "XXX");
	}

	public void delNumberBlackList(String phoneNumber) {
		// TODO Auto-generated method stub
		
	}

	public void delNumberWhiteList(String phoneNumber) {
		// TODO Auto-generated method stub
		
	}

	public void addTextBlackList(String textMessage) {
		// TODO Auto-generated method stub
		
	}

	public void addTextWhiteList(String textMessage) {
		// TODO Auto-generated method stub
		
	}

	public void delTextBlackList(String textMessage) {
		// TODO Auto-generated method stub
		
	}

	public void delTextWhiteList(String textMessage) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * 底层函数
	 */
	private boolean judgeNumber(String phoneNumber, int type) {
		Cursor cur;
		
		cur = mCreditDb.queryNumber(phoneNumber);

		if (null==cur)
			return false;

        if ( 0>=cur.getCount() )
        	return false;

		if (!cur.moveToFirst())
			return false;
	       
		int dbType = cur.getInt(cur.getColumnIndex(CreditDb.KEY_TYPE));
		int enable = cur.getInt(cur.getColumnIndex(CreditDb.KEY_ENABLED));
		
		cur.close();
		
		if ( 0==enable )
			return false;
		
		return type==dbType;		
	}

}
