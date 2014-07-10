package com.alexsum.tcw;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;




public class TcwService extends Service {
//	Log.i("TcwService", "BEGIN");
	public final static UUID PEBBLE_APP_UUID = UUID.fromString("17E9E8A6-C57D-46C5-A8DA-2C7211DE0B73");
	public final static UUID PEBBLE_APPTOOLS_UUID = UUID.fromString("AB1E32DD-05D7-4DA4-A123-2D5ECE64FC2C");	
	private static final Uri SMS_INBOX_CONTENT_URI = Uri.parse("content://sms/inbox");
	private static final Uri MMS_INBOX_CONTENT_URI = Uri.parse("content://mms/inbox");
	private static final Uri CALLS_CONTENT_URI = Uri.parse("content://call_log/calls");
	private MediaPlayer player; 
	
	private static int incom_mess = 0;
	private static int outgo_mess =0;
	
	int curPhoneBatteryStatus =-1;
	boolean isPhoneCharging = false;
	boolean isPhonePluged = false;
	int curPebbleBatteryStatus = -1;
	boolean isPebbleCharging = false;
	boolean isPebblePlugged =false;
	boolean PebbleConnected = false;	
    int incomSMSCount = 0;
    int incomLostCall = 0;
    int taid = 1;
    boolean isInetOnline = false;
    int audioManagerState = 2;
    int pebbleTCWVersion = 0;
    int gmailCount = 0;
    int isAlarmSet = 0;
    int min_phone_bat_state=101;
    String AlarmTime ="";
    
	int oldcurPhoneBatteryStatus =-1;
	boolean oldisPhoneCharging = false;
	boolean oldisPhonePluged = false;
	int oldcurPebbleBatteryStatus = -1;
	boolean oldisPebbleCharging = false;
	boolean oldisPebblePlugged =false;
//	boolean PebbleConnected = false;	
    int oldincomSMSCount = 0;
    int oldincomLostCall = 0;
//    int taid = 1;
    boolean oldisInetOnline = false;
    int oldaudioManagerState = 2;
    int oldgmailCount = 0;
    int oldisAlarmSet = 0;
//    int pebbleTCWVersion = 0;   
    DBHelper dbHelper;
    NotificationManager nm;
    
    protected void ValueEqually(){
    	oldcurPhoneBatteryStatus = curPhoneBatteryStatus;
    	oldisPhoneCharging = isPhoneCharging;
    	oldisPhonePluged = isPhonePluged;
    	oldcurPebbleBatteryStatus = curPebbleBatteryStatus ;
    	oldisPebbleCharging = isPebbleCharging;
    	oldisPebblePlugged = isPebblePlugged;
        oldincomSMSCount = incomSMSCount ;
        oldincomLostCall = incomLostCall ;
        oldisInetOnline = isInetOnline;
        oldaudioManagerState = audioManagerState;
        oldgmailCount = gmailCount;
        oldisAlarmSet = isAlarmSet; 
    }
    
    protected boolean isValueEqualy(){
    	boolean rv = true;
    	if (oldcurPhoneBatteryStatus != curPhoneBatteryStatus){
    		if ((oldcurPhoneBatteryStatus ==-1) || ((isPhonePluged || isPhoneCharging) && (oldcurPhoneBatteryStatus > curPhoneBatteryStatus)) || ((curPhoneBatteryStatus%10==0) && (isPhonePluged || isPhoneCharging))){
    			if ((min_phone_bat_state>curPhoneBatteryStatus)||(isPhonePluged || isPhoneCharging)){
    				rv=false;
    			}
    		}
    	}
//    	if (oldcurPhoneBatteryStatus != curPhoneBatteryStatus) rv=false;
    	if (oldisPhoneCharging != isPhoneCharging) rv=false;
    	if (oldisPhonePluged != isPhonePluged) rv=false;
    	if (oldcurPebbleBatteryStatus != curPebbleBatteryStatus) rv=false;
    	if (oldisPebbleCharging != isPebbleCharging) rv=false;
    	if (oldisPebblePlugged != isPebblePlugged) rv=false;
    	if ((oldincomSMSCount == 0 && incomSMSCount != 0)||(oldincomSMSCount !=0 && incomSMSCount == 0)) rv=false;
    	if ((oldincomLostCall == 0 && incomLostCall != 0)||(oldincomLostCall !=0 && incomLostCall == 0)) rv=false;
    	if (oldisInetOnline != isInetOnline) rv=false;
    	if (oldaudioManagerState != audioManagerState) rv=false;
    	if ((oldgmailCount == 0 && gmailCount!=0)||(oldgmailCount != 0 && gmailCount ==0)) rv=false;
    	if (oldisAlarmSet != isAlarmSet) rv=false;
    	return rv;
    }

    protected int GetMailCount() {
        final String ACCOUNT_TYPE_GOOGLE = "com.google";
        Context context = getApplicationContext();
        AccountManager accountManager = AccountManager.get(context);
        Account[] acnt = accountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
        if (acnt != null && acnt.length > 0) {
            String account = acnt[0].name;
            final Uri labelsUri = GmailContract.Labels.getLabelsUri(account);
            final String inboxCanonicalName = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_ALL_MAIL;
            if (inboxCanonicalName != null) {
                Cursor cursor = context.getContentResolver().query(labelsUri, null, null, null, null);
                if (cursor != null) {
                    try {
                        int googleMailUnreadMessages = 0;
                        if (cursor.moveToFirst()) {
                            final int canonicalNameIndex = cursor.getColumnIndexOrThrow(GmailContract.Labels.CANONICAL_NAME);
                            int unreadColumn = cursor.getColumnIndex("numUnreadConversations");
                            do {
                                if (inboxCanonicalName.equals(cursor.getString(canonicalNameIndex))) {
                                    googleMailUnreadMessages += cursor.getInt(unreadColumn);
                                }
                            } while (cursor.moveToNext());
                        }
                        return googleMailUnreadMessages;
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    } finally {
                        cursor.close();
                    }
                }
            }
        }

        // получаем почту от k9, если установлен, у нас есть на это права и он активен
        if (K9Helper.isK9Installed(context) && K9Helper.hasK9ReadPermission(context) && K9Helper.isK9Enabled(context)) {
            List<K9Helper.Account> accounts = K9Helper.getAccounts(context);
            if (accounts != null) {
                int totalUnread = 0;
                for (K9Helper.Account k9account : accounts) {
                    totalUnread += K9Helper.getUnreadCount(context, k9account);
                }
                return totalUnread;
            }
        }

        return 0;
    }

	protected void PlayRington(){
//		MediaPlayer player =  MediaPlayer.create(this,Settings.System.DEFAULT_RINGTONE_URI);
//		MediaPlayer player = new MediaPlayer();
	
		try{
			Uri uripath = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
			player.reset();
			if( uripath != null){
				player.setDataSource(this,uripath);
			}else{
				AssetFileDescriptor afd = getAssets().openFd("vertu.mp3");
				player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			}
			player.setAudioStreamType(AudioManager.MODE_RINGTONE);
			player.setVolume(1,1);
			player.prepare(); 
			player.start();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	protected void StopPlayRington(){
		player.stop();
	}
	protected void StartVibration(){
	
		String vibratorService = Context.VIBRATOR_SERVICE;
		Vibrator vibrator = (Vibrator)getSystemService(vibratorService);
		long[] pattern = {1000, 1000, 1000, 1000 };
		try{
			vibrator.vibrate(pattern, -1);
		}finally{;}
	}
	
	protected void wakeUp(){
		 PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
         WakeLock wakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
         wakeLock.acquire();
         wakeLock.release();
	}    
    
    
    public static boolean isOnline(Context context) {
    	boolean rv=false;
    	try{
    		ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    		NetworkInfo nInfo = cm.getActiveNetworkInfo();
    		if (nInfo != null && nInfo.isConnected()) {
//            Log.i("status", "ONLINE");
    			rv= true;
    		}
    	}finally{;}
        return rv;
    }
    public int OnChangeAudioMangerState(){
    	int ams = -1;
    	try{
    		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    		ams=am.getRingerMode();
    		switch (am.getRingerMode()) {
    			case AudioManager.RINGER_MODE_SILENT:
//    				Log.i("MyApp","Silent mode");
    				break;
    			case AudioManager.RINGER_MODE_VIBRATE:
//    				Log.i("MyApp","Vibrate mode");
    				break;
    			case AudioManager.RINGER_MODE_NORMAL:
//    				Log.i("MyApp","Normal mode");
    				break;
    		}
    	}finally{;}
    	audioManagerState=ams;
    	return ams;
    }
	
	protected int noReadSMSCount(){
		int count=0;
		Context context=getApplicationContext();
		String SMS_READ_COLUMN = "read";
	    String WHERE_CONDITION = SMS_READ_COLUMN + " = 0";
	    String SORT_ORDER = "";
	    try{
	    	Cursor cursor = context.getContentResolver().query(SMS_INBOX_CONTENT_URI,new String[] { "_id" },WHERE_CONDITION, null, SORT_ORDER);
	    	if (cursor != null) {
	            try {
	            	count = cursor.getCount();
	            } finally {
                    cursor.close();
	            }
	    	}
	    	cursor = context.getContentResolver().query(MMS_INBOX_CONTENT_URI,new String[] { "*" },WHERE_CONDITION, null, SORT_ORDER);
	    	if (cursor != null) {
	    		try {
	            	count = count + cursor.getCount();
	            } finally {
                    cursor.close();
	            }
	    	}
	    }finally{ ;}
	    return count;
	}
	protected int lostCalls(){
		int count = 0;
		Context context=getApplicationContext();
//		String[] projection = { "*" CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
//		String[] projection = { "*" };		
	    String where = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1";          
//	       Cursor c = context.getContentResolver().query(CALLS_CONTENT_URI, projection,where, null, null);
	    try{
	       Cursor c = context.getContentResolver().query(CALLS_CONTENT_URI, null,where, null, null);
	       try {
	    	   count =c.getCount();
	       } finally {
               c.close();
           }
//	       Log.d("CALL", ""+c.getCount());
	    }finally{ ; }
		return count;
	}
	public void sendToMainActivity(){
		AlarmTime = Settings.System.getString(getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		if(AlarmTime.length()>0) isAlarmSet = 1; else isAlarmSet = 0;
		gmailCount=GetMailCount();
		
		incomSMSCount=noReadSMSCount();
		incomLostCall=lostCalls();	
		isInetOnline = isOnline(getApplicationContext());
		Intent intent = new Intent("com.alexsum.tcw");
		intent.putExtra(MainActivity.PnBS, curPhoneBatteryStatus);
		intent.putExtra(MainActivity.PbBS, curPebbleBatteryStatus);
		if (isPhoneCharging){intent.putExtra(MainActivity.PnCR, 1);}else{intent.putExtra(MainActivity.PnCR, 0);};
		if (isPebbleCharging){intent.putExtra(MainActivity.PbCR, 1);}else{intent.putExtra(MainActivity.PbCR, 0);};
		if (isPhonePluged){intent.putExtra(MainActivity.PhPl, 1);}else{intent.putExtra(MainActivity.PhPl, 0);};
		if (isPebblePlugged){intent.putExtra(MainActivity.PbPl, 1);}else{intent.putExtra(MainActivity.PbPl, 0);};		
		intent.putExtra(MainActivity.PcSMS, incomSMSCount);
		intent.putExtra(MainActivity.PcMC, incomLostCall);
		intent.putExtra(MainActivity.pTCWv, pebbleTCWVersion);
		if (isInetOnline){intent.putExtra(MainActivity.IsInet, 1);}else{intent.putExtra(MainActivity.IsInet, 0);};	
		intent.putExtra(MainActivity.PcMail, gmailCount);
		intent.putExtra(MainActivity.IncomMess, incom_mess);
		intent.putExtra(MainActivity.OutgoMess, outgo_mess);
        sendBroadcast(intent);	
        sendNotif();
	}
	
	public void OnChangeStatus(){
		sendToMainActivity();
		if(! isValueEqualy()) SendBStoPebble();		
	}
	
	
	public void SendBStoPebble(){
		if (curPhoneBatteryStatus>0){
			if(PebbleKit.isWatchConnected(getApplicationContext())){
				outgo_mess++;
				savestat(0, 1);
				Log.i("TcwService", "outgo_mess ++ "+outgo_mess);
//    			PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
				PebbleDictionary data = new PebbleDictionary();
//				data.addInt32(201, (int) curPhoneBatteryStatus);
				data.addUint8(201, (byte) curPhoneBatteryStatus);
				if (isPhoneCharging){
					data.addUint8(202, (byte)1);
				}else{
					data.addUint8(202, (byte)0);    			
				}
				data.addUint8(203, (byte)curPebbleBatteryStatus);
				if (isPebbleCharging){
					data.addUint8(204, (byte)1);
				}else{
					data.addUint8(204, (byte)0);   			
				}
				data.addInt16(205, (short)incomSMSCount);
				data.addInt16(206, (short) incomLostCall);
				if (isPhonePluged){
					data.addUint8(207, (byte)1);
				}else{
					data.addUint8(207, (byte)0);
				}
				if (isInetOnline){
					data.addUint8(208, (byte)1);
				}else{
					data.addUint8(208, (byte)0);
				}
				data.addUint8(209, (byte) audioManagerState);// OnChangeAudioMangerState());
				data.addUint8(210, (byte) isAlarmSet);
				data.addString(211, AlarmTime);
				data.addInt16(212, (short)gmailCount);
				
				if (taid>250){taid=1;}else{taid=taid+1;}
				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PEBBLE_APP_UUID, data,taid);
//				Log.i("TcwService", "Send state to pebble "+curPhoneBatteryStatus+ " "+isPhoneCharging+" transactionid="+taid);
				ValueEqually();
			}
		}
	}
	public void SendAscBStoPebble(){
       	if(PebbleKit.isWatchConnected(getApplicationContext())){
    		PebbleDictionary data = new PebbleDictionary();
    		data.addInt32(200, 1);    
//    		Log.i("send asc for pebble battary status","");
    		PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
       	}
	}
	
	public String getContactName(Context context, String phoneNumber) {
	    ContentResolver cr = context.getContentResolver();
	    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
	            Uri.encode(phoneNumber));
	    Cursor cursor = cr.query(uri,
	            new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
	    if (cursor == null) {
	        return null;
	    }
	    String contactName = null;
	    if (cursor.moveToFirst()) {
	        contactName = cursor.getString(cursor
	                .getColumnIndex(PhoneLookup.DISPLAY_NAME));
	    }
	    if (cursor != null && !cursor.isClosed()) {
	        cursor.close();
	    }
	    return contactName;
	}	
		
	public void SendSmsToPebble(int qvr,int num, long part){
		Context context=getApplicationContext();
//		if( incomSMSCount>0){
		String SMS_READ_COLUMN = "read";
//		String WHERE_CONDITION = incomSMSCount>0 ? SMS_READ_COLUMN + " = 0" : null;
		String WHERE_CONDITION =  null;
//		}
        String SORT_ORDER = "date DESC";
        int count = 0;

        //Log.v(WHERE_CONDITION);

//        if (ignoreThreadId > 0) {
//                WHERE_CONDITION += " AND thread_id != " + ignoreThreadId;
//        }

        Cursor cursor = context.getContentResolver().query(
                        SMS_INBOX_CONTENT_URI,
                        new String[] { "_id", "thread_id", "address", "person", "date", "body" },
                        WHERE_CONDITION,
                        null,
                        SORT_ORDER);

        if (cursor != null) {
        	try {
        		PebbleDictionary data = new PebbleDictionary();
                count = cursor.getCount();
                if(qvr==500){
                	if(PebbleKit.isWatchConnected(getApplicationContext())){
                	data.addInt32(500, count);
//                	data.addInt32(500, 1);
                	if (taid>250){taid=1;}else{taid=taid+1;}
//    				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PEBBLE_APPTOOLS_UUID, data,taid);
    				PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APPTOOLS_UUID, data);    				
//    				Log.i("TcwService", "Send count sms "+data.toJsonString()+" taid "+taid);
                	}
                }else{
                	if(cursor.moveToPosition(num)){
                        long messageId = cursor.getLong(0);
                        long threadId = cursor.getLong(1);
                        String address = cursor.getString(2);
                        long contactId = cursor.getLong(3);
                        String contactId_string = address;
                        if(contactId>0){
                        	contactId_string = getContactName(getApplicationContext(), address);
                        }
//                        String contactId_string = String.valueOf(contactId);
                        long timestamp = cursor.getLong(4);
                        String body = cursor.getString(5);
                        
                		switch (qvr){
                		case 502:
                            data.addInt32(501, num);
                            data.addString(502, contactId_string);
                            if (taid>250){taid=1;}else{taid=taid+1;}
            				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PEBBLE_APPTOOLS_UUID, data,taid);
//            				Log.i("TcwService", "Send  sms 502 contact="+contactId_string);
            				break;
                		case 503:
                            data.addInt32(501, num);
                            data.addUint32(503,(int) timestamp);
                            if (taid>250){taid=1;}else{taid=taid+1;}
            				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PEBBLE_APPTOOLS_UUID, data,taid);
//            				Log.i("TcwService", "Send  sms 503 time="+timestamp);
            				break;
                		case 504:
                            data.addInt32(501, num);                
//                            data.addString(504, body);
                            
                            String newStr  = body.replaceAll("(.{30})", "$1|");
                            String[] newStrings = newStr.split("\\|");
                            
                            data.addInt32(504, newStrings.length);
                            
/*                            for(int i=0;i<newStrings.length;i++){
                            	data.addString(600+i, newStrings[i]);
                            }*/

                            data.addInt32(505, (int)part);
                           	data.addString(506, newStrings[(int)part]);
                            
                            if (taid>250){taid=1;}else{taid=taid+1;}
            				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PEBBLE_APPTOOLS_UUID, data,taid);
//            				Log.i("TcwService", "Send  sms 504 body="+newStrings[(int)part]);
            				break;
                		}
                		
                	}
                }

                } finally {
                        cursor.close();
                }
        }               
//        return null;		
	}

	  final String LOG_TAG = "myLogs";

	  public void onCreate() {
	    super.onCreate();
	    
		player = new MediaPlayer();
		dbHelper = new DBHelper(this);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    
		BroadcastReceiver PhoneBatteryReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int curPBS = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
				int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				boolean chpl = true;
				if (chargePlug==0){ chpl=false;}
//				Log.i("TcwService", "Phone battery value=" + curPBS+"  isCharginge=" + isCharging+"  isPlugged=" + chpl+ "  real "+chargePlug);		
				curPhoneBatteryStatus = curPBS;
				isPhoneCharging = isCharging;
				isPhonePluged = chpl;
				OnChangeStatus();
			}
		};	    
		BroadcastReceiver InetStatusReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				OnChangeStatus();
			}
		};
		BroadcastReceiver incomSMSReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				OnChangeStatus();
			}
		};
		BroadcastReceiver AudioManagerState = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context,Intent intent) {
				OnChangeAudioMangerState();
				OnChangeStatus();
			}
		};
		
		BroadcastReceiver MainActivitiRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int isStart = intent.getIntExtra("Start", -1);
				if(isStart==1){
					SendBStoPebble();
				}
			}
		};


		PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleAckReceiver(PEBBLE_APP_UUID) {
			  @Override
			  public void receiveAck(Context context, int transactionId) {
//			    Log.i("TcwService", "Received ack for transaction " + transactionId);			    
			  }
		});		
		
	    PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
	    	  @Override
	    	  public void onReceive(Context context, Intent intent) {
	    		  //peblle connect
	    		  PebbleConnected =true;
	    			SendBStoPebble();
	    			SendAscBStoPebble();
	    	  }
    	});

	    PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
	    	  @Override
	    	  public void onReceive(Context context, Intent intent) {
	    		  // pebble disconnect
	    		  PebbleConnected = false;
	    	  }
    	});		
	    
	    
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APPTOOLS_UUID) {
		    @Override
		    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
//		    	Log.i("TcwService", "Received pebble value data "+ data.toJsonString());

		    	if (data.contains(300)){
		    		int s300 = (int)(data.getInteger(300)*1);
		    		if (s300==1){
		    			// start music
		    			PlayRington();
		    		}
		    		if (s300==2){
		    			// start vibr
		    			StartVibration();
		    		}
		    		if(s300==3){
		    			// stop music
		    			StopPlayRington();
		    		}
		    	}
		    	if (data.contains(500)){
		    		int s500 = (int) (data.getInteger(500)*1);
		    		if(s500==0){
		    			SendSmsToPebble(500,s500,0);
		    		}
		    	}
		    	if (data.contains(502)){
		    		int s502 = (int) (data.getInteger(502)*1);
	    			SendSmsToPebble(502,s502,0);
		    	}
		    	if (data.contains(503)){
		    		int s503 = (int) (data.getInteger(503)*1);
	    			SendSmsToPebble(503,s503,0);
		    	}
		    	if (data.contains(504)){
		    		int s504 = (int) (data.getInteger(504)*1);
		    		if (data.contains(506)){
		    			SendSmsToPebble(504,s504,data.getInteger(506)*1);
		    		}
		    	}
		      PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
		    }
		});		    
	    
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
		    @Override
		    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
		    	Log.i("TcwService", "Received pebble value data "+ data.toJsonString());
		    	incom_mess++;
		    	savestat(1, 0);
		    	Log.i("TcwService", "Incom_mess++ "+incom_mess);
		    	if (data.contains(211)){
		    		curPebbleBatteryStatus=(int)(data.getInteger(211)*1);
//		    		Log.i("TcwService", "Received pebble value 211 212=" + curPebbleBatteryStatus);
		    		if(data.getInteger(212)==1){
		    			isPebbleCharging=true;
		    		}else{
		    			isPebbleCharging=false;
		    		}
		    		if(data.getInteger(213)==1){
		    			isPebblePlugged=true;
		    		}else{
		    			isPebblePlugged=false;
		    		}
		    		sendToMainActivity();
		    	}
		    	if (data.contains(200)){
//		    		Log.i("TcwService", "Incom request 200");
		    		SendBStoPebble();
		    	}
		    	if (data.contains(333)){
		    		byte[] dat= data.getBytes(333);
		    		pebbleTCWVersion = (int)dat[0];
		    		curPebbleBatteryStatus=(int) dat[1];
		    		min_phone_bat_state= (int)dat[3]; 		    		
    		
		    		if((dat[2]&1)!=0){
		    			isPebbleCharging=true;
		    		}else{
		    			isPebbleCharging=false;
		    		}		    		
	    			if((dat[2]&1<<1)!=0){
	    				isPebblePlugged=true;
	    			}else{
	    				isPebblePlugged=false;
	    			}
		    		if ((dat[2]&1<<2)!=0){
		    			SendBStoPebble();
		    		}	    			
		    		sendToMainActivity();
		    	}
		    	if (data.contains(1018)){
		    		pebbleTCWVersion = (int) (data.getInteger(1018)*1);
		    		sendToMainActivity();
		    	}
		    	if (data.contains(104)){
		    		min_phone_bat_state= (int)(data.getInteger(104)*1);
		    	}
		      PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
		    }
		});			
		registerReceiver(PhoneBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));	
		registerReceiver(InetStatusReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		registerReceiver(AudioManagerState, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
		IntentFilter smsif = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		smsif.setPriority(100);
		registerReceiver(incomSMSReceiver, smsif);
		registerReceiver(MainActivitiRec, new IntentFilter("TcwService") );
	    Log.d(LOG_TAG, "onCreate");
	    SendBStoPebble();
	  }
	  
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.i(LOG_TAG, "onStartCommand");
//	    PendingIntent pi = intent.getParcelableExtra("connect");

	    
	    someTask();
	    SendAscBStoPebble();
	    sendNotif();
	    return super.onStartCommand(intent, flags, startId);
	  }

	  public void onDestroy() {
		  curPebbleBatteryStatus = 0;
		  isPebbleCharging = false;
		  SendBStoPebble();
	    super.onDestroy();
	    Log.d(LOG_TAG, "onDestroy");
	  }	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO ������������� ��������� �������� ������
		return null;
	}

	void someTask() {
	    OnChangeStatus();

	}
	
	void savestat(int in, int out){
		if(curPebbleBatteryStatus==-1) return;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Date dNow = new Date( );
		
		Long time=dNow.getTime();
		Long razn=(long)(60*60*24*1000*(-24));
		time = time + razn; // -31 ����
		Date cdNow = new Date(time);
		
		
	    SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd HH");
		String datstr=ft.format(dNow);
		
		String olddatstr=ft.format(cdNow);
		
		String dquery="delete from stat where date<'"+olddatstr+"' or prc<0";
		db.execSQL(dquery);
		
		String where="date='"+datstr+"' and prc="+curPebbleBatteryStatus;
		Cursor c = db.query("stat", null, where , null, null, null, null);
		if (c.getCount()==0){
			String quern="insert into stat (date,incom,outgo,prc) values ('"+datstr+"',"+in+","+out+","+curPebbleBatteryStatus+")";
			db.execSQL(quern);
		}else{
			if( c.moveToFirst()){
				int idIncom = c.getColumnIndex("incom");
				int idOutgo = c.getColumnIndex("outgo");
				int cinc= c.getInt(idIncom);
				int cout= c.getInt(idOutgo);
				cinc=cinc+in; 
				cout=cout+out;
				String queru="update stat set incom="+cinc+",outgo="+cout+" where "+where;
				Log.i("TcwService", queru);
				db.execSQL(queru);
			}
		}
		c.close();
		dbHelper.close();
	}
	
	  void sendNotif() {
		  
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(context, MainActivity.class);
//		Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://developer.alexanderklimov.ru/android/"));
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		String messg ="";
	    int SmallIcon= R.drawable.si0;
		if (curPebbleBatteryStatus==-1){
	    	messg =  "Get data ...";
	    }else{
	    	messg= "Battery "+curPebbleBatteryStatus+"%";
	    	switch (curPebbleBatteryStatus){
	    		case 10:
	    			SmallIcon= R.drawable.si10;
	    			break;
	    		case 20:
	    			SmallIcon= R.drawable.si20;
	    			break;
	    		case 30:
	    			SmallIcon= R.drawable.si30;
	    			break;
	    		case 40:
	    			SmallIcon= R.drawable.si40;
	    			break;
	    		case 50:
	    			SmallIcon= R.drawable.si50;
	    			break;
	    		case 60:
	    			SmallIcon= R.drawable.si60;
	    			break;	    			
	    		case 70:
	    			SmallIcon= R.drawable.si70;
	    			break;	    			
	    		case 80:
	    			SmallIcon= R.drawable.si80;
	    			break;	    			
	    		case 90:
	    			SmallIcon= R.drawable.si90;
	    			break;	    			
	    		default:
	    			SmallIcon= R.drawable.si100;
	    	}
	  	}

		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
		Notification.Builder builder = new Notification.Builder(context)
			.setContentTitle("Pebble SmartWatch: TCW")
			.setContentText(messg)
			.setTicker("Pebble TCW: starting").setWhen(System.currentTimeMillis()) // java.lang.System.currentTimeMillis()
			.setContentIntent(pendingIntent)
			.setLargeIcon(bm)
			.setOngoing(true)
			.setSmallIcon(SmallIcon);
		
//		.setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true)
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(1/*NOTIFY_ID*/, builder.getNotification());
		  
		  
/*		    // 1-� �����
		    Notification notif = new Notification(R.drawable.icon , "Pebble TCW: starting", 
		      System.currentTimeMillis());
		    
		    // 3-� �����
		    Intent intent = new Intent(this, MainActivity.class);
		    intent.putExtra("filename", "somefile");
		    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		    
		    // 2-� �����
		    if (curPebbleBatteryStatus==-1){
		    	notif.setLatestEventInfo(this, "Pebble SmartWatch: TCW", "Get data ...", pIntent);
		    }else{
		    	notif.setLatestEventInfo(this, "Pebble SmartWatch: TCW", "Battery "+curPebbleBatteryStatus+"%", pIntent);
		    }
		    
		    
		    // ������ ����, ����� ����������� ������� ����� �������
//		    notif.flags |= Notification.FLAG_AUTO_CANCEL;
		    
		    // ����������
		    nm.notify(1, notif);*/
		  }
	
	class DBHelper extends SQLiteOpenHelper {

	    public DBHelper(Context context) {
	      // ����������� �����������
	      super(context, "baseTCW", null, 1);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	      Log.d(LOG_TAG, "--- onCreate database ---");
	      // ������� ������� � ������
	      db.execSQL("create table stat (id integer primary key autoincrement,date text,incom integer, outgo integer, prc integer);");
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	    }
	  }
	
	
	
	
}
