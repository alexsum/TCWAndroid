package com.alexsum.tcw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TcwBroadcastReceiver extends BroadcastReceiver {
	final String LOG_TAG = "myLogs";
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Автоматически созданная заглушка метода 
		arg0.startService(new Intent(arg0, TcwService.class));

	}

}
