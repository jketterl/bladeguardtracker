package net.djmacgyver.bgt;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public static String gcmId;
	
	public GCMIntentService() {
		super(gcmId);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		System.out.println("message received!");
		System.out.println(arg1.getExtras().getString("command"));
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		System.out.println("registered; id = " + arg1);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
