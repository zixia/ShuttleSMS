package com.akamobi.shuttlesms.engine;


import android.content.Context;
import android.util.Log;

/*
 * 创意来自于反垃圾邮件领域中的 Challenge-Response(C/R) Filter 技术。 
此项技术的细节在WikiPedia中有详细说明： 
http://en.wikipedia.org/wiki/Challenge–response_spam_filtering 。
应用在短信领域，其优势在于可以达到非常低的漏判率和误判率
（false-negative rate/false-positive rate）。
简单描述来讲，原理是：针对未存在通讯录中手机号码发来的短信，系统拦截。
同时自动回复短信质询，发送方回答正确后，将短信移入收件箱，并自动加入白名单。
 */
/*
 * 陌生短信
 * 1、收到陌生号码来的短信
 * 	a. 是黑名单？直接存入Spam；
 *  b. 是白名单？直接存入Inbox；
 *  c. 已经发送过CR提示？检查CR认证；
 *  c. 第一次发送。回复CR提示给号码，消息存入待审核Quarantine；
 *  c. Quarantine消息，一周后未能认证，则移入Spam，号码加入黑名单，Notification提示用户；
 * 2、检查CR认证：
 * 	a. 符合CR要求：
 * 		I. 将发送号码存入白名单
 * 		II. 回复CR成功提示
 * 		III. 将相关短信移入Inbox
 * 		IV. Notification提示用户新增白名单
 * 		V. 提示用户是否存入通讯录；
 *  b. 不符合CR要求
 *  	I. 第一次：提示“如何CR验证”
 *  	II. 第二次：提示“格式错误，正确格式如何”
 *  	III. 第三次：加黑“验证失败，号码已经加黑。如果你认为是误操作，请电话联系。”
 *  			将相关短信移入Spam；
 *  			同时Notification提示用户新增黑名单；
 *
 *  Input: phoneNumber, textMessage
 *  Logic: saveSMS, sendSMS, deleteSMS, queryDb, 
 *  Output: 
 */

public class Shuttle {
	private static final String TAG	= "Shuttle";
	private Context mCtx;
	private String	mReplyMessage;

	public enum Action {
		PASS	//放行
		, DROP	//丢弃
		, QUAR	//隔离
		, ACPT	//认证成功
	};

	public Shuttle(Context context)
	{
		mCtx = context;
	}
		
	/*
	 * 返回需要回复的短信内容String
	 */
	public Action Process(final String phoneNumber, final String textMessage) {
		CreditJudge judger = new CreditJudge(mCtx);
		
		/*
		 * 如果是白名单，则放行；
		 */
		if (judger.isNumberInContact(phoneNumber)		// 通讯录号码
				|| judger.isNumberWhiteList(phoneNumber)	// 号码白名单
				|| judger.isTextWhiteList(textMessage)		// 关键字白名单
				) {
			return Action.PASS;
		}
		
		/*
		 * 如果是黑名单，则丢弃；
		 */
		if (judger.isNumberBlackList(phoneNumber)			// 号码黑名单
				|| judger.isTextBlackList(textMessage)		// 关键字黑名单
				) {
			return Action.DROP;
		}
		
		/*
		 * 如果机主曾经给phoneNumber主动发送过短信，则：
		 * 	1. 先加入白名单
		 * 	2. 然后放行
		 */
		if (judger.isNumberInSentBox(phoneNumber)) {
			judger.addNumberWhiteList(phoneNumber);
			return Action.PASS;
		}
		
		/*
		 * 进行Challenge-Respons质询回复处理判断
		 */
		ChallengeResponse cr = new ChallengeResponse(mCtx);
				

		// 获取 Challange-Response验证结果
		ChallengeResponse.Result crResult;
		
		crResult		= cr.process(phoneNumber, textMessage);
		mReplyMessage	= cr.getChallengeMessage();

		/*
		 * 根据CR验证结果，决定处理动作
		 */
		Action stAction;
		
		switch (crResult) {
		case PENDING:	// 等待结果，隔离
		case SOFTFAIL:	// 等待结果，隔离
			stAction = Action.QUAR;
			break;
		case ACCEPT:		// 加白
			judger.addNumberWhiteList(phoneNumber) ;
			stAction = Action.ACPT;
			break;
		case HARDFAIL:	// 加黑
			judger.addNumberBlackList(phoneNumber) ;
			stAction = Action.QUAR;	// 第一次CR失败后设置为QUAR，因为还要记录回复
			break;
		default:
			Log.e(TAG,"unknown cr.process act val:" + crResult);
			stAction = Action.PASS;
			break;
		}
		
		return stAction;
	}

	public String getReplyMessage()
	{
		return mReplyMessage;
	}
}
