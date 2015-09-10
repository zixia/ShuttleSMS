package com.akamobi.shuttlesms.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

import com.akamobi.shuttlesms.db.ChallengeResponseDb;

public class ChallengeResponse {
	public enum Result {PENDING, SOFTFAIL, HARDFAIL, ACCEPT};

	final String 	TAG	= "ChallengeResponse";
	Context			mCtx = null;
	
	ChallengeResponseDb mDb = null;
		
	/*
	 * 用户手机号，以及CR结束后，识别出来的名字（如果有）以及回复给用户的消息
	 */
	String 	mPhoneNumber;
	String	mTextMessage;
	
	String 	mChallengeMessage;
	String 	mResponseName;
	long	mRowId;

	Result mResult 		= Result.PENDING;

	ChallengeResponse(Context context) {
		mCtx = context;
	}
	
	
	/** 
	 * 判断结果：
	 * 1、待定
	 * 2、入白名单
	 * 3、入黑名单
	 */
	public Result process(String phoneNumber, String textMessage) {
		Result result					= Result.PENDING;

		this.mPhoneNumber	= phoneNumber;
		this.mTextMessage	= textMessage;
		this.mResponseName 	= "";
		this.mChallengeMessage 	= "";
		this.mRowId			= -1;

		dbInit();

		/*
		 * 获取已经发送的质询消息数
		 */
		int challengeCounter	= getChallengeCounter();

		switch (challengeCounter) {
		case 0:	// 尚未进行C-R质询，回复第一条短信，告知如何CR验证；
			result 				= Result.PENDING;
			this.mChallengeMessage	= 
				"抱歉：你的短信未能送达，因为号码未知。请回复“短信门神#你的姓名”验证身份。【短信门神】";
			
			logTextMessage();
			logChallenge(1);
			logResult(result);
			
			break;
			
		case 1: // 发送过第一次C-R质询，是第二条短信，本次或者成功验证，或者告知验证失败
			if (isQualifiedChallengeResponse(phoneNumber, textMessage)) {
				// CR质询第一次验证成功
				result			= Result.ACCEPT;
				mChallengeMessage	= String.format(
						"%s你好：确认身份成功！感谢配合，你的短信已经送达。【短信门神】"
						, mResponseName);
			} else {
				// CR质询第一次验证失败
				result			= Result.SOFTFAIL;
				mChallengeMessage	= "抱歉，请按格式要求，回复“短信门神#你的姓名”验证身份。【短信门神】";
			}
			
			logResponse(1);
			logChallenge(2);
			logResult(result);
			
			break;
			
		case 2: // 发送过二次C-R质询，这是收到的第三条短信。或者成功验证，或者告知2次验证失败，加入黑名单；
			if (isQualifiedChallengeResponse(phoneNumber, textMessage)) {
				result			= Result.ACCEPT;
				mChallengeMessage	= String.format(
						"%s你好：确认身份成功！感谢配合，你的短信已经送达。【短信门神】"
						, mResponseName);
			} else {
				result			= Result.SOFTFAIL;
				mChallengeMessage	= "抱歉，你的回复不符合格式要求。请按格式要求，回复“短信门神#你的姓名”验证身份。【短信门神】";
			}
			
			logResponse(2);
			logChallenge(3);
			logResult(result);
			
			break;
			
		default: // CR质询多次失败，加入黑名单
			result				= Result.HARDFAIL;
			mChallengeMessage		= "抱歉，短信未能送达。请通过电话联系机主，将您加入通讯录后即可正常通信。【短信门神】";
			
			logResponse(3);
			logChallenge(4);
			logResult(result);
			
			break;
		}
		
		return result;
	}

	private void logTextMessage() {
		mRowId = mDb.createCR(mPhoneNumber, mTextMessage);
	}

	private long getRowId()
	{
		if ( 0 <= mRowId )
			return mRowId;
		
		mRowId = mDb.getRowId(mPhoneNumber);
		
		return mRowId;
	}
	
	private int getResponseCounter() {
		long rowId = getRowId();
		
		if ( 0>rowId )
			return 0;
		
		return mDb.getResponseCounter(rowId);
	}

	private int getChallengeCounter() {
		long rowId = getRowId();
		
		if ( 0>rowId )
			return 0;
		
		return mDb.getChallengeCounter(rowId);
	}

	private void logResult(Result result) {
		long rowId = getRowId();
		mDb.setResult(rowId, result);		
	}

	/**
	 * 记录APP发出的质询信息
	 * @param num 第几条
	 */
	private void logChallenge(int num) {
		long rowId = getRowId();
		
		if ( 0>rowId ) {
			Log.e(TAG, "rowId < 0");
			return;
		}
		
		mDb.setChallenge(rowId, num, mChallengeMessage);
	}

	/**
	 * 记录用户回复的应答信息
	 * @param num 第几条
	 */
	private void logResponse(int num) {
		long rowId = getRowId();
		
		if ( 0>rowId ) {
			Log.e(TAG, "rowId < 0");
			return;
		}
		
		mDb.setResponse(rowId, num, mTextMessage);
	}

	private boolean isQualifiedChallengeResponse(String phoneNumber,
			String textMessage) 
	{	
        Pattern p = Pattern.compile("短信门神(#|＃)(.{2,6})");
        Matcher m = p.matcher(textMessage);
        
        if (!m.find())
        	return false;
        
        // 将认证姓名保存备用
        this.mResponseName = m.group(2);
        
        return true;
	}
	
	private void dbInit(){
		if ( null==mDb )
			mDb = new ChallengeResponseDb(mCtx);
	}
	
	public String getResponseName(){
		return mResponseName;
	}
	
	public String getChallengeMessage(){
		return mChallengeMessage;
	}
}