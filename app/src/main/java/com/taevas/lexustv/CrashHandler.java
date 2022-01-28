package com.taevas.lexustv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler {

	public CrashHandler()
	{
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		Thread.setDefaultUncaughtExceptionHandler(null);
		
		String stack = Log.getStackTraceString(ex);
		
		// Append to File.
		try {
			  /*FileOutputStream outputStream = ctx.openFileOutput("crash_report.txt", Context.MODE_APPEND);
			  outputStream.write(stack.getBytes());
			  outputStream.close();*/
			
			// Find the root of the external storage.
		    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

		    File root = android.os.Environment.getExternalStorageDirectory(); 

		    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

		    File file = new File(root, "looplayer_crash_report.txt");

		    try {
		        FileOutputStream f = new FileOutputStream(file);
		        PrintWriter pw = new PrintWriter(f);
		        pw.println(stack);
		        pw.flush();
		        pw.close();
		        f.close();
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }   
			
		} catch (Exception e) {
		  e.printStackTrace();
		}
		
		// Crashes the app like a boss.
		System.exit(1);
		
	}

}
