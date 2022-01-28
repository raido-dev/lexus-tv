package com.taevas.lexustv;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			Toast.makeText(context, context.getString(R.string.app_name) + " will start soon...", Toast.LENGTH_LONG).show();
			Intent myIntent = new Intent(context, MainActivity.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(myIntent);
		}

		//final Context ctx = context;
		
		// Shows a welcome toast.
		//Toast.makeText(context, context.getString(R.string.app_name) + " will start in 5 seconds...", Toast.LENGTH_LONG).show();

		// Waits a bit then the main activity. 
		/*new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				// Auto start main activity when device starts-up.
				Intent myIntent = new Intent(ctx, MainActivity.class);
			    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    ctx.startActivity(myIntent);
				
			}
		}, 5000);*/
		


	}

}
