package com.alexsum.tcw;

//200 - get_battery_status     �������� 200 ��������� ������ ������� ������
//201 - send_battery_status
//202 - send_battery_charged

//210 - set_battery_status    ��������� 210 �������� ������ �������� ������
//211 - receive_battery_status
//212 - receive_battery_charged

import java.util.UUID;

import android.R.color;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.CallLog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;

public class MainActivity extends Activity {
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("17E9E8A6-C57D-46C5-A8DA-2C7211DE0B73");
//	TextView PhoneBSpl;
//	TextView PebbleBSpl;
	ImageView phoneImage;
	ImageView pebbleImage;
	ImageView smsImage;
	ImageView callImage;
	ImageView inetImage;
	ImageView mailImage;
	TextView textFW;
	TextView textTCWV;
	TextView viewUSSD;
	ProgressBar pbLoad;
	Button TestButon;
	TextView textIOM;
	Button bStat;
	
	int TCVversion=0;
	
//	public void SendBStoPebble();

//	int curPhoneBatteryStatus;
//	boolean isPhoneCharging;
//	int curPebbleBatteryStatus;
//	boolean isPebbleCharging;
	boolean PebbleConnected = false;
	public final static String PnBS = "pnbs";
	public final static String PbBS = "pbbs";
	public final static String PnCR = "pncr";
	public final static String PbCR = "pbcr";
	public final static String PcSMS = "pcsms";
	public final static String PcMC = "pcmc";
	public final static String PhPl = "phpl";
	public final static String PbPl = "pbpl";
	public final static String IsInet = "isinet";
	public final static String pTCWv = "ptcwv";
	public final static String PcMail = "pcmail";
	public final static String IncomMess = "incommess";
	public final static String OutgoMess = "outgomess";
	
	private  CountDownTimer StatTimer ;

//	String vibratorService = Context.VIBRATOR_SERVICE;
//	Vibrator vibrator = (Vibrator)getSystemService(vibratorService);	
	
	BroadcastReceiver br;
	
//	public void OnChangePhoneBS(int curPBS, boolean Chg){
//		curPhoneBatteryStatus = curPBS;
//		isPhoneCharging = Chg;
//		PhoneBS.setText(curPhoneBatteryStatus+"%");
//		if(Chg==true){
//			PhoneBSch.setText("�������");
//		}else{
//			PhoneBSch.setText(""); 
//		}
// //       SendBStoPebble();
//	};
	
//	public void OnChangePebbleBS(int curPBS, boolean Chg){
//		curPebbleBatteryStatus = curPBS;
//		isPebbleCharging = Chg;
//		PebbleBS.setText(curPebbleBatteryStatus+"%");
//		if(Chg==true){
//			PebbleBSch.setText("�������");
//		}else{
//			PebbleBSch.setText("");
//		}
// //       SendBStoPebble();
//	};	 


	
	protected float PxToDp (float px){
//		int dpSize = 5 ;
		DisplayMetrics dm = getResources().getDisplayMetrics() ;

		float inPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px , dm);
		return inPixels ;//* px;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
/*        UssdMessage ussd = new UssdMessage(4000,4000); // ���������� ��� ���������, �������� �� � ����� (ms) �������� ���������
        if (ussd.IsFound())
                viewUSSD.setText("\n"+ussd.getMsg());
        else
        	viewUSSD.setText("������ USSD");*/
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("MainActivity", "requestCode = " + requestCode + ", resultCode = " + resultCode);
	
       

        
        
	}

	private int textWidth(String str,Paint p){
		int rv =0;
		float[] widths = new float[str.length()+1];
		int cnt =p.getTextWidths(str, widths);
		for (int i=0;i<cnt;i++) {
			rv=rv+(int)widths[i];
		}
		return rv;
	}
	
	private void inetDraw(int cnt){
		if(cnt>0){
			Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.internet);
			inetImage.setImageBitmap(bmp);
		}else{
	    	Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.nointernet);
	    	inetImage.setImageBitmap(bmp);			
		}	
	}
	
	private void smsDraw(int cnt){
		double left=0;
		double top=0;
		double right= 60;
		double bottom = 60;
		left=PxToDp((float)left);
		top=PxToDp((float)top);
		right=PxToDp((float)right);
		bottom=PxToDp((float)bottom);		
		if(cnt>0){
			Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sms);
			smsImage.setImageBitmap(bmp);
			bmp = Bitmap.createBitmap(smsImage.getWidth(), smsImage.getHeight(), Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			smsImage.draw(c);
			Paint p = new Paint();	
			p.setColor(Color.BLACK);
			p.setStyle(Paint.Style.FILL);
			p.setAntiAlias(true);
			p.setTextSize((int)PxToDp(23));
			String scnt=" "+cnt+" ";
			int tw = textWidth(scnt, p);
			c.drawText(scnt, (float)(left+(right-left)/2-tw/2), (float)(top+(bottom-top)/2+11), p);
			smsImage.setImageBitmap(bmp);
		}else{
	    	Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.nosms);
	    	smsImage.setImageBitmap(bmp);			
		}
	}
	
	private void callDraw(int cnt){
		double left=0;
		double top=0;
		double right= 60;
		double bottom = 60;
		left=PxToDp((float)left);
		top=PxToDp((float)top);
		right=PxToDp((float)right);
		bottom=PxToDp((float)bottom);		
	    if(cnt>0){		
			Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.call);
			callImage.setImageBitmap(bmp);	    	
	    	bmp = Bitmap.createBitmap(callImage.getWidth(), callImage.getHeight(), Config.ARGB_8888);;//Bitmap.createBitmap(callImage.getWidth(), callImage.getHeight(), Config.ARGB_8888);
	    	Canvas c = new Canvas(bmp);
	    	callImage.draw(c);
	    	Paint p = new Paint();	
	    	p.setColor(Color.BLACK);
	    	p.setStyle(Paint.Style.FILL);
	    	p.setAntiAlias(true);
	    	p.setTextSize((int)PxToDp(23));
	    	String scnt=" "+cnt+" ";
	    	int tw = textWidth(scnt, p);
	    	c.drawText(scnt, (float)(left+(right-left)/2-tw/2), (float)(top+(bottom-top)/2+11), p);
	    	callImage.setImageBitmap(bmp);
	    }else{
	    	Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.nocall);
	    	callImage.setImageBitmap(bmp);
	    }
	}
	
	private void mailDraw(int cnt){
		double left=0;
		double top=0;
		double right= 60;
		double bottom = 60;
		left=PxToDp((float)left);
		top=PxToDp((float)top);
		right=PxToDp((float)right);
		bottom=PxToDp((float)bottom);		
	    if(cnt>0){		
			Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.mail);
			mailImage.setImageBitmap(bmp);	    	
	    	bmp = Bitmap.createBitmap(callImage.getWidth(), callImage.getHeight(), Config.ARGB_8888);;//Bitmap.createBitmap(callImage.getWidth(), callImage.getHeight(), Config.ARGB_8888);
	    	Canvas c = new Canvas(bmp);
	    	mailImage.draw(c);
	    	Paint p = new Paint();	
	    	p.setColor(Color.BLACK);
	    	p.setStyle(Paint.Style.FILL);
	    	p.setAntiAlias(true);
	    	p.setTextSize((int)PxToDp(23));
	    	String scnt=" "+cnt+" ";
	    	int tw = textWidth(scnt, p);
	    	c.drawText(scnt, (float)(left+(right-left)/2-tw/2), (float)(top+(bottom-top)/2+11), p);
	    	mailImage.setImageBitmap(bmp);
	    }else{
	    	Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.nomail);
	    	mailImage.setImageBitmap(bmp);
	    }
	}
	
	
	private void phoneDraw(int prc, int chg, int plg){
		double left=10;
		double top=16;
		double right= 68;
		double bottom = 112;
		left=PxToDp((float)left);
		top=PxToDp((float)top);
		right=PxToDp((float)right);
		bottom=PxToDp((float)bottom);
		String sprc;
		Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.phone);
		phoneImage.setImageBitmap(bmp);
		bmp = Bitmap.createBitmap(phoneImage.getWidth(), phoneImage.getHeight(), Config.ARGB_8888);
	    Canvas c = new Canvas(bmp);
	    phoneImage.draw(c);
	    Paint p = new Paint();
	    p.setColor(color.white);
	    c.drawRect((float)left, (float)top, (float)right, (float)bottom,  p);
	    
	    p.setColor(Color.GREEN);
	    if( chg==1 && plg==1){
	    	p.setColor(Color.BLUE);
	    }else{
	    	if (plg==1){
	    		p.setColor(Color.RED);
	    	}
	    }
	    double height= ((bottom-top)*prc/100);
	    
	    double rtop= bottom - height;
	    c.drawRect((float)left, (float)rtop, (float)right, (float)bottom,  p);
	    p.setColor(Color.BLACK);
	    p.setStyle(Paint.Style.FILL);
	    p.setAntiAlias(true);
	    p.setTextSize((int)PxToDp(23));
	    if (prc>0){
	    	sprc=prc+"%";
	    }else{
	    	sprc="%";
	    }
	    int tw = textWidth(sprc, p);
	    c.drawText(sprc,  (float)(left+(right-left)/2-tw/2), (float)(top+(bottom-top)/2+23/2), p);
//	    Log.d("MainActivity", "left = " + left + ", rtop = " + rtop +", right = "+right+ ", bottom = "+ bottom+", height = "+ height+ ", prc = "+prc);
	    phoneImage.setImageBitmap(bmp);
	}

	private void pebbleDraw(int prc, int chg, int plg){
		double left=17;
		double top=39;
		double right= 60;
		double bottom = 93;
		left=PxToDp((float)left);
		top=PxToDp((float)top);
		right=PxToDp((float)right);
		bottom=PxToDp((float)bottom);		
		String sprc;
		Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.pebble);
		pebbleImage.setImageBitmap(bmp);
		bmp = Bitmap.createBitmap(pebbleImage.getWidth(), pebbleImage.getHeight(), Config.ARGB_8888);
	    Canvas c = new Canvas(bmp);
	    pebbleImage.draw(c);
	    Paint p = new Paint();
	    p.setColor(color.white);
	    c.drawRect((float)left, (float)top, (float)right, (float)bottom,  p);
	    
	    p.setColor(Color.GREEN);
	    if( chg==1 && plg==1){
	    	p.setColor(Color.BLUE);
	    }else{
	    	if (plg==1){
	    		p.setColor(Color.RED);
	    	}
	    }
	    double height= ((bottom-top)*prc/100);
	    
	    double rtop= bottom - height;
	    c.drawRect((float)left, (float)rtop, (float)right, (float)bottom,  p);
	    p.setColor(Color.BLACK);
	    p.setStyle(Paint.Style.FILL);
	    p.setAntiAlias(true);
	    p.setTextSize((int)PxToDp(23));
	    if (prc>0){
	    	sprc=prc+"%";
	    }else{
	    	sprc="%";
	    }
	    int tw = textWidth(sprc, p);
	    c.drawText(sprc,  (float)(left+(right-left)/2-tw/2), (float)(top+(bottom-top)/2+23/2), p);
//	    Log.d("MainActivity", "left = " + left + ", rtop = " + rtop +", right = "+right+ ", bottom = "+ bottom+", height = "+ height+ ", prc = "+prc);
	    pebbleImage.setImageBitmap(bmp);
	}

	protected void SendReqToPebble(){
   	 	Intent intentStart = new Intent("TcwService");
		intentStart.putExtra("Start", 1);
        sendBroadcast(intentStart);
        Log.d("MainActivity", "SRTP");
	}
/*	@Override
	  protected void onResume() {
	    super.onResume();
	    Log.d("MA", "ActivityTwo: onResume()");
	    SendReqToPebble();
	  }*/
	
	Runnable SRTP = new Runnable() {
	    public void run() {
	      SendReqToPebble();
	    }
	  };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		phoneImage = (ImageView) findViewById(R.id.phoneImage);
		pebbleImage = (ImageView) findViewById(R.id.pebbleImage);
		smsImage = (ImageView) findViewById(R.id.smsImage);
		callImage = (ImageView) findViewById(R.id.callImage);
		inetImage = (ImageView) findViewById(R.id.inetImage);
		textFW = (TextView) findViewById(R.id.textFW);
		textTCWV = (TextView) findViewById(R.id.textTCWV);
		pbLoad = (ProgressBar) findViewById(R.id.loadPB);
//		TestButon = (Button) findViewById(R.id.button1);
//		viewUSSD = (TextView) findViewById(R.id.ViewUSSD);
		mailImage = (ImageView) findViewById(R.id.mailImage);
		textIOM = (TextView) findViewById(R.id.textIOM);
		Button bStat = (Button) findViewById(R.id.bStat);
		
		phoneImage.postDelayed(SRTP, 3000);
//		if(PebbleKit.isWatchConnected(getApplicationContext()))
//			textFW.setText(PebbleKit.getWatchFWVersion(getApplicationContext()).getTag());

//		try{
//			player.prepare();
//		} catch (IOException e) {
//            e.printStackTrace();
//        }
		br = new BroadcastReceiver() {
		      // �������� ��� ��������� ���������
			public void onReceive(Context context, Intent intent) {
				int pnbs = intent.getIntExtra(PnBS, -1);
				int pbbs = intent.getIntExtra(PbBS, -1);
				int pncr = intent.getIntExtra(PnCR, -1);
				int pbcr = intent.getIntExtra(PbCR, -1);
				int pcsms = intent.getIntExtra(PcSMS, -1);
				int pcmc = intent.getIntExtra(PcMC, -1);
				int pnpl = intent.getIntExtra(PhPl, -1);
				int pbpl = intent.getIntExtra(PbPl, -1);
				int isInet = intent.getIntExtra(IsInet, -1);
				int pcmail = intent.getIntExtra(PcMail, -1);
				int inm = intent.getIntExtra(IncomMess, 0);
				int oum = intent.getIntExtra(OutgoMess, 0);
				
				TCVversion = intent.getIntExtra(pTCWv, 0);
				if(PebbleKit.isWatchConnected(getApplicationContext()))
					textFW.setText("FW "+PebbleKit.getWatchFWVersion(getApplicationContext()).getTag());
				textTCWV.setText("TCW v."+TCVversion);
				phoneDraw(pnbs,pncr,pnpl);
				pebbleDraw(pbbs, pbcr, pbpl);
				smsDraw(pcsms);
				callDraw(pcmc);
				inetDraw(isInet);
				mailDraw(pcmail);
				if (pbbs<1){
					pbLoad.setVisibility(View.VISIBLE);
				}else{
					pbLoad.setVisibility(View.INVISIBLE);
				}
				textIOM.setText("in "+inm+"/out "+oum);
			}
		};
	    IntentFilter intFilt = new IntentFilter("com.alexsum.tcw");
	    registerReceiver(br, intFilt);
	   
//		PhoneBSpl  = (TextView) findViewById(R.id.PhoneBatteryPluged);
//		PebbleBSpl = (TextView) findViewById(R.id.PebbleBatteryPluged);		
		
		
		

//		PendingIntent pi;
		Intent intent;
//		pi = createPendingResult(1, null, 0);
		intent = new Intent(this, TcwService.class);//.putExtra("connect", pi);
		
		startService(intent);

/*	     OnClickListener oclTestButton = new OnClickListener() {
	         @Override
	         public void onClick(View v) {
	        	 if(player.isPlaying()){
	        		StopPlayRington(); 
	        	 }else{
	        		 PlayRington();
	        	 }
	        	 StartVibration();
	         }
	       };
	     TestButon.setOnClickListener(oclTestButton);*/  
	     OnClickListener oclSmsImage = new OnClickListener() {
	         @Override
	         public void onClick(View v) {
	           // ������ ����� � TextView (tvOut)
	        	 try{
	        		 Intent intent = new Intent(Intent.ACTION_MAIN);
	        		 intent.addCategory(Intent.CATEGORY_DEFAULT);
	        		 intent.setType("vnd.android-dir/mms-sms");
	        		 startActivity(intent);
	        	 }finally{;}
	         }
	       };
		smsImage.setOnClickListener(oclSmsImage);
		
	     OnClickListener oclcallImage = new OnClickListener() {
	         @Override
	         public void onClick(View v) {
	        	 try{
	        		 Intent intent = new Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI);
	        		 startActivity(intent);
	        	 }finally{;}
	         }
	       };
		callImage.setOnClickListener(oclcallImage);		
		
	     OnClickListener oclinetImage = new OnClickListener() {
	         @Override
	         public void onClick(View v) {
	        	 try{
	        		 startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
	        	 }finally{;}
	         }
	       };
		inetImage.setOnClickListener(oclinetImage);			

	     OnClickListener oclpebbleImage = new OnClickListener() {
	         @Override
	         public void onClick(View v) {
//	        	 Intent intent = new Intent(Intent.ACTION_MAIN);
	        	 
//	        	 intent.setClassName("com.getpebble.android", "com.getpebble.android.activity_main");
//	        	 startActivity(intent);
	        	 try{
	        		 PackageManager pm = getPackageManager();
	        		 Intent intent = pm.getLaunchIntentForPackage("com.getpebble.android");
	        		 startActivity(intent);
	        	 }finally{;}
	         }
	       };
		pebbleImage.setOnClickListener(oclpebbleImage);
		

		
	     OnClickListener oclphoneImage = new OnClickListener() {
	         @Override
	         public void onClick(View v) {
/*                 String encodedHash = Uri.encode("#");
                 call("*" + "101" + encodedHash);*/
	        	 SendReqToPebble();
	         }
	       };
        phoneImage.setOnClickListener(oclphoneImage);

        OnClickListener oclMailImage = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO ������������� ��������� �������� ������
                boolean mailIntentOpened = false;
                Intent gmailIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                if (gmailIntent != null) {
                    mailIntentOpened = true;
                    startActivity(gmailIntent);
                }
                if (!mailIntentOpened) {
                    Context appContext = getApplicationContext();
                    if (K9Helper.isK9Installed(appContext) && K9Helper.isK9Enabled(appContext)) {
                        Intent k9intent = K9Helper.getStartK9Intent(getApplicationContext());
                        if (k9intent != null) {
                            mailIntentOpened = true;
                            startActivity(k9intent);
                        }
                    }
                }
            }
        };
        mailImage.setOnClickListener(oclMailImage);
		
		OnClickListener oclbStat = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO ������������� ��������� �������� ������
				
//				Context context=getApplicationContext();
				Intent intent = new Intent(getApplicationContext(), aStatistic.class);
			     startActivity(intent);
			}
		};
		bStat.setOnClickListener(oclbStat);
		
//		SendReqToPebble();
/*		Intent intentStart = new Intent("com.alexsum.tcw");
		intentStart.putExtra("Start", 1);
        sendBroadcast(intentStart);	*/
	
	}
/*    protected void call(String phoneNumber) {
        try {
                startActivityForResult(
                                new Intent("android.intent.action.CALL", Uri.parse("tel:"
                                                + phoneNumber)), 1);
        } catch (Exception eExcept) {
//                this.view.append("\n\n " + "\n" + eExcept.toString());
        }
    }*/

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        UssdMessage ussd = new UssdMessage(4000,4000); // ���������� ��� ���������, �������� �� � ����� (ms) �������� ���������
        if (ussd.IsFound())
                this.view.append("\n"+ussd.getMsg());
        else
                this.view.append(""+R.string.error_ussd_msg);
    }*/
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // �������� ��� ���������� ������ ����
	    switch (item.getItemId()) 
		{
	    case R.id.menuInstallWatchface:
	        mInstallWathcface();
	        return true;
	    case R.id.menuInstallTools:
	    	mInstallTools();
	    	return true;
	    case R.id.menuLaunchWatchFace:
	    	PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void mInstallTools()
	{
		WatchappHandler.install(this,"tcwtools.pbw");
	}
	public void mInstallWathcface()
	{
		WatchappHandler.install(this,"tcw.pbw");
	}

}
