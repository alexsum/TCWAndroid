package com.alexsum.tcw;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class aStatistic extends Activity {
	DBHelper dbHelper;
	
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(new DrawView(this));
	    dbHelper = new DBHelper(this);
	  }

	class DrawView extends View {
		public DrawView(Context context) {
			super(context);

		}
		    
		@Override
		protected void onDraw(Canvas canvas) {
			int daycnt=10;
			
			Date dNow = new Date( );
			Long time=dNow.getTime();
			time = time + (60*60*24*1000*(-daycnt));
			Date cdNow = new Date(time);
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd HH");
			String olddatstr=ft.format(cdNow);
			
			
			
			canvas.drawColor(Color.WHITE);
			int w =canvas.getWidth();
			int h = canvas.getHeight();
			Paint p = new Paint();
			
			p.setColor(Color.BLUE);
			p.setTextSize(14);
			
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			String querym="select max(incom) as incom, max(outgo) as outgo from (select sum(incom) as incom,sum(outgo) as outgo from stat where date>'"+olddatstr+"'group by date)";
			Cursor m = db.rawQuery(querym, null);
			int minc=100;
			int mout=100;
			if (m.getCount()>0){
				m.moveToFirst();
				int mIncom=m.getColumnIndex("incom");
				int mOutgo=m.getColumnIndex("outgo");
				minc=m.getInt(mIncom);
				mout=m.getInt(mOutgo);
			}
			m.close();
			int maxy=minc;
			if (maxy<mout) maxy=mout;
			
			int nw= (daycnt)*24; int nh=100;
			
			
			float tp = (float) (h*0.05);
			float lf = (float) (w*0.05);
			float pw = (float)(w-w*0.1)/nw;
			float ph = (float)(h-h*0.15)/nh;
			
			float ioy= (float)(100*ph)/(float)(maxy+maxy*0.2);
			
			
			canvas.drawLine(lf+10*pw, tp+0*ph-5, lf+10*pw, tp+nh*ph+15,p);

			canvas.drawLine(lf+10*pw-5, tp+nh*ph, lf+nw*pw, tp+nh*ph,p);
			for(int i=0;i<10;i++){
				canvas.drawLine(lf+10*pw-5, tp+(i*10)*ph, lf+nw*pw, tp+(i*10)*ph,p);
				canvas.drawText(100-i*10+"%", lf+10*pw-textWidth(100-i*10+"%", p)-5, tp+(i*10)*ph+5, p);
			}
			
			Paint pin = new Paint();
			Paint pout = new Paint();
			Paint ppr = new Paint();
			Paint pprc = new Paint();
			pin.setColor(Color.RED);
			pout.setColor(Color.GREEN);
			pprc.setColor(Color.DKGRAY);
			pin.setTextSize(14);
			pout.setTextSize(14);
			pprc.setTextSize(14);
			ppr.setColor(Color.MAGENTA);
			ppr.setStrokeWidth(5);
			pin.setStrokeWidth(pw/2);
			pout.setStrokeWidth(pw/2);
			pin.setFlags(Paint.ANTI_ALIAS_FLAG);
			pout.setFlags(Paint.ANTI_ALIAS_FLAG);
			ppr.setFlags(Paint.ANTI_ALIAS_FLAG);
			

//			SQLiteDatabase db = dbHelper.getReadableDatabase();
			String query="select date,avg(prc) as prc,sum(incom) as incom,sum(outgo) as outgo from stat  where date>'"+olddatstr+"' group by date order by date desc";//date desc,prc desc";
			Cursor c = db.rawQuery(query, null);
			if (c.getCount() > 0){               
			    c.moveToFirst();
				int idIncom = c.getColumnIndex("incom");
				int idOutgo = c.getColumnIndex("outgo");
				int idDate = c.getColumnIndex("date");
				int idPrc = c.getColumnIndex("prc"); 
			    int cinc=0;
				int cout=0;
				int cprc=0;
				String cdate=c.getString(idDate);
				int curx=10;
				float bx=0;
				float by=0;
				int curhcnt=0;
				float curhbgn=0;
				int curhleft=0;
				int sumin=0;
				int sumout=0;
				float sumcurbgn=0;
				String[] tstr = cdate.split(" ");
				String tstr1=tstr[0];
				String[] dat00=tstr1.split("\\.");
				int oldd=Integer.parseInt(dat00[2]);
				int curd=oldd;
			    do {
			    	curx++;
				    cinc= c.getInt(idIncom);
					cout= c.getInt(idOutgo);
					cprc= c.getInt(idPrc);
					cdate = c.getString(idDate);
					tstr = cdate.split(" ");
					tstr1=tstr[0];
					dat00=tstr1.split("\\.");
					int curh=Integer.parseInt(tstr[1]);
					curd=Integer.parseInt(dat00[2]);
					if (/*curh==0 ||*/ curd!=oldd){
						canvas.drawLine(lf+curx*pw, tp+0*ph-5, lf+curx*pw, tp+nh*ph+10,p);
						if (curhcnt==0){
							curhcnt++;
							curhbgn=lf+curx*pw;
						}else{
							canvas.drawText(oldd+"/"+dat00[1], curhbgn+(lf+curx*pw-curhbgn- textWidth(oldd/*dat00[2]*/+"/"+dat00[1], p))/2, tp+nh*ph+15, p);
							sumcurbgn=curhbgn;
							curhbgn=lf+curx*pw;
							canvas.drawText(sumin+"", sumcurbgn+5, tp+nh*ph+15+15, pin);
							canvas.drawText(sumout+"", curhbgn-5- textWidth(sumout+"", pout), tp+nh*ph+15+15, pout);
						}
						sumin=cinc;
						sumout=cout;
					}else{
						sumin=sumin+cinc;
						sumout=sumout+cout;
					}
					oldd=Integer.parseInt(dat00[2]);
					canvas.drawLine(lf+curx*pw, tp+100*ph, lf+curx*pw, tp+100*ph-(cinc)*ioy, pin);
					canvas.drawLine(lf+curx*pw+1, tp+100*ph, lf+curx*pw+1, tp+100*ph-(cout)*ioy, pout);
					if(curx==11){
						bx=curx*pw;
						by=(100-cprc)*ph;//(cprc+10)*ph;
					}else{
						canvas.drawLine(lf+bx, tp+by, lf+curx*pw,tp+(100-cprc)*ph, ppr);
						bx=curx*pw;
						by=(100-cprc)*ph;
						
					}
//			    	Log.d("aStat","date="+cdate+"  prc="+cprc+"  in="+cinc+"   out="+cout );
//			    }
//			           id[i]=c.getInt(c.getColumnIndex("field_name"));
//			           i++;
			    } while (c.moveToNext());
			}
			pin.setTextSize(20);
			pin.setFakeBoldText(true);
			pout.setTextSize(20);
			pout.setFakeBoldText(true);
			ppr.setTextSize(20);
			ppr.setFakeBoldText(true);
			pprc.setTextSize(20);
			pprc.setFakeBoldText(true);
			
			canvas.drawText("Incoming", canvas.getWidth()-20-textWidth("Incoming", pin), 30, pin);
			canvas.drawText("Outgoing", canvas.getWidth()-20-textWidth("Outgoing", pin), 60, pout);
			canvas.drawText("Percent", canvas.getWidth()-20-textWidth("Percent", pin), 90, ppr);
			c.close();

			
			dNow = new Date( );
			ft = new SimpleDateFormat ("yyyy.MM.dd HH");
			String datstr=ft.format(dNow);			
			
			query="select date,avg(prc) as prc from stat group by date order by date";
			c = db.rawQuery(query, null);
			int remain=0;
			if (c.getCount() > 1){               
			    c.moveToFirst();
				int idDate = c.getColumnIndex("date");
				int idPrc = c.getColumnIndex("prc"); 	
				int cprc= c.getInt(idPrc);
				int prc= c.getInt(idPrc);

				String cdate = c.getString(idDate);
				int[] aprc;
				aprc=new int[101];
				c.moveToNext();
				int hcount=0;
				int incurhour=0;
				int colprc=0;
				do {
					cdate = c.getString(idDate);
					prc= c.getInt(idPrc);
					if(prc<=cprc) {
						colprc=colprc+cprc-prc;
						if (datstr.equals(cdate)) incurhour++; 
						aprc[(int)prc]++;
						hcount++;
					}
					cprc=prc;
				
					Log.d("aStat","date="+cdate+"  prc="+cprc);
				} while (c.moveToNext());
				int wh=0;
				for (int i=cprc;i>0;i--){
					wh=wh+aprc[i];
				}
				wh=wh-incurhour;
				int rpzb=wh*100/hcount;
				float prcinhour=(float)colprc/hcount;
				remain= (int)(rpzb/prcinhour);
			}
			c.close();
			if (remain>0 && remain<336){
				canvas.drawText("≈ "+remain+" h", canvas.getWidth()-20-textWidth("≈"+remain+" h", pin), 120, pprc);
			}
			dbHelper.close();			
			
			
			
		}
	}
	class DBHelper extends SQLiteOpenHelper {

	    public DBHelper(Context context) {
	      // конструктор суперкласса
	      super(context, "baseTCW", null, 1);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	    	// создаем таблицу с полями
//	      db.execSQL("create table stat (id integer primary key autoincrement,date text,incom integer, outgo integer, prc integer);");
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	    }
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
}
