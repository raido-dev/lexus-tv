package com.taevas.lexustv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private PlaybackController playbackController;
	
	public VideoView wVideoView;
	public ImageView wImageView;
	
	private WakeLock wakeLock;

	private boolean donwloadingContent = false;

	// Progress Dialog
	private ProgressDialog pDialog;
	public static final int progress_bar_type = 0;

	public Playlist currentActivePlaylist;
	public Playlist newPlaylist;

	@SuppressLint("InvalidWakeLockTag")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!Settings.canDrawOverlays(getApplicationContext())) { startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)); }
		// Register for crashes.
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
		
		// Gets the version.
		PackageInfo pInfo;
		String dummyVersion;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			dummyVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			dummyVersion = "r1-c";
		}
		
		// Removes the windows decorations and goes fullscreen.
		//getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Gets the wake lock.
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyMediaPlayer");

	    // Shows the content.
		setContentView(R.layout.activity_main);
		
		// Inits the widgets.
		wVideoView = (VideoView) findViewById(R.id.videoView);
		wImageView = (ImageView) findViewById(R.id.imageView);

		// Loads the playlist to play.

		startPlaying();
		//playbackController.playNextMedia();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		
		super.onPause();
		
		wakeLock.release();
		
		playbackController.pause();
	}

	@SuppressLint("WakelockTimeout")
	@Override
	protected void onResume() {

		super.onResume();
		
		wakeLock.acquire();
	}

	public boolean isOnline() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void startPlaying() {
		if(isOnline()) {
			//Playlist currentPlaylist = new Playlist(Environment.getExternalStorageDirectory().toString()+"/Movies", false);
			//currentActivePlaylist = new Playlist(Environment.getExternalStorageDirectory().toString()+"/Movies", false);
			//playbackController = new PlaybackController(this, currentActivePlaylist);
			//currentActivePlaylist.currentItem = 0;
			//playbackController.currentFilename = null;
			//Log.d("PLOP",  " :calledStart");
			new Thread(new Runnable()
			{
				public void run()
				{
					final List<String> addressList = getTextFromWeb("https://klient.taevas.com/raido/lexus/lexustv-content/Movies/playlist.txt"); // format your URL
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							//update ui
							List<String> localList = null;
							try {
								localList = Files.readAllLines(Paths.get(Environment.getExternalStorageDirectory().toString()+"/Movies/playlist.txt"));
							} catch (IOException e) {
								e.printStackTrace();
							}
							 if (localList != null) Log.d("PLOP", "online: " + addressList + " local: " + localList + " " +  addressList.containsAll(localList) + " :isSame");
							if (!addressList.equals(localList) || localList == null) {
								if (playbackController != null) playbackController.pause();
								List<String> newList = new ArrayList<>();
								for (String temp : addressList) {
									newList.add("https://klient.taevas.com/raido/lexus/lexustv-content/Movies/" + temp);
								}
								newList.add("https://klient.taevas.com/raido/lexus/lexustv-content/Movies/playlist.txt");
								//Log.d("PLOP",  newList + " :listName");
								File dir = new File(Environment.getExternalStorageDirectory()+"/Movies");
								if (dir.isDirectory())
								{
									String[] children = dir.list();
									for (int i = 0; i < children.length; i++)
									{
										new File(dir, children[i]).delete();
									}
								}
								startDownload(newList.toArray(new String[0]));
								donwloadingContent = true;

							} else if (!donwloadingContent) {
								if (playbackController == null) {
									if (currentActivePlaylist != null) {currentActivePlaylist.clear();currentActivePlaylist.currentItem = -1;currentActivePlaylist = null;}
									if (newPlaylist != null) {newPlaylist.clear();newPlaylist.currentItem = -1;}
									currentActivePlaylist = new Playlist(Environment.getExternalStorageDirectory().toString() + "/Movies", true);
									playbackController = new PlaybackController(com.taevas.lexustv.MainActivity.this, currentActivePlaylist);
									if (playbackController != null) playbackController.currentFilename = null;
									playbackController.playNextMedia();
								} else {
									playbackController.playNextMedia();
									Log.d("PLOP",  currentActivePlaylist.size()+" :getsCalled");
								}
							}
						}
					});
				}
			}).start();

		} else {
			//Playlist currentPlaylist = new Playlist(Environment.getExternalStorageDirectory().toString()+"/Movies", false);
			if (currentActivePlaylist == null) {
				newPlaylist = null;
				playbackController = null;
				if (currentActivePlaylist != null) {currentActivePlaylist.clear();currentActivePlaylist.currentItem = -1;}
				if (newPlaylist != null) {newPlaylist.clear();newPlaylist.currentItem = -1;}
				currentActivePlaylist = new Playlist(Environment.getExternalStorageDirectory().toString() + "/Movies", false);
				playbackController = new PlaybackController(this, currentActivePlaylist);
				playbackController.currentFilename = null;
				Handler pauser = new Handler();
				pauser.postDelayed(new Runnable() {
					public void run() {
						playbackController.playNextMedia();
					}
				}, 1000);
			} else {
				playbackController.playNextMedia();
			}
		}
		//Toast.makeText(getBaseContext(), getString(R.string.app_name) + " " + dummyVersion + " - " + currentPlaylist.size() + " videos will now be played in loop.", Toast.LENGTH_LONG).show();

	}

	private void startDownload(String[] url) {
		DownloadFileAsync dloadFAsync = new DownloadFileAsync(url);
		dloadFAsync.execute(url);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case progress_bar_type: // we set this to 0
				pDialog = new ProgressDialog(this);
				pDialog.setMessage("Updating Lexus content. Please wait...");
				pDialog.setIndeterminate(false);
				pDialog.setMax(100);
				pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pDialog.setCancelable(true);
				pDialog.show();
				return pDialog;
			default:
				return null;
		}
	}

	public void reload() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	public List<String> getTextFromWeb(String urlString)
	{
		URLConnection feedUrl;
		List<String> placeAddress = new ArrayList<>();

		try
		{
			feedUrl = new URL(urlString).openConnection();
			InputStream is = feedUrl.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;

			while ((line = reader.readLine()) != null) // read line by line
			{
				placeAddress.add(line); // add line to list
			}
			is.close(); // close input stream

			return placeAddress; // return whatever you need
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileAsync extends AsyncTask<String, String, String> {

		int current=0;
		String[] paths;
		String fpath;
		boolean show = false;

		public DownloadFileAsync(String[] paths) {
			super();
			this.paths = paths;
			for(int i=0; i<paths.length; i++)
				System.out.println((i+1)+":  "+paths[i]);
			return;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(progress_bar_type);
		}

		@Override
		protected String doInBackground(String... aurl) {
			int rows = aurl.length;
			while(current < rows)
			{
				int count;
				try {
					System.out.println("Current:  "+current+"\t\tRows: "+rows);
					fpath = getFileName(this.paths[current]);
					URL url = new URL(this.paths[current]);
					URLConnection conexion = url.openConnection();
					conexion.connect();
					int lenghtOfFile = conexion.getContentLength();
					InputStream input = new BufferedInputStream(url.openStream(), 512);
					OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/Movies/"+fpath);
					byte data[] = new byte[512];
					long total = 0;
					while ((count = input.read(data)) != -1) {
						total += count;
						publishProgress(""+(int)((total*100)/lenghtOfFile));
						output.write(data, 0, count);
					}
					show = true;
					output.flush();
					output.close();
					input.close();
					current++;
				} catch (Exception e) {}
			}   //  while end
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			pDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			System.out.println("unused: "+unused);
			// dismiss the dialog after the file was downloaded
			dismissDialog(progress_bar_type);

			playbackController = null;
			if (currentActivePlaylist != null) {currentActivePlaylist.clear();currentActivePlaylist.currentItem = -1;}
			if (newPlaylist != null) {newPlaylist.clear();newPlaylist.currentItem = -1;}
			newPlaylist = new Playlist(Environment.getExternalStorageDirectory().toString()+"/Movies", true);
			playbackController = new PlaybackController(com.taevas.lexustv.MainActivity.this, newPlaylist);
			currentActivePlaylist = newPlaylist;
			playbackController.currentFilename = null;
			Handler pauser = new Handler();
			pauser.postDelayed (new Runnable() {
				public void run() {
					donwloadingContent = false;
					com.taevas.lexustv.MainActivity.this.reload();
					//playbackController.playNextMedia();
				}
			}, 1000);
		}

	}

	public String getFileName(String wholePath)
	{
		String name=null;
		int start,end;
		start=wholePath.lastIndexOf('/');
		end=wholePath.length();     //lastIndexOf('.');
		name=wholePath.substring((start+1),end);
		name = ""+name;
		System.out.println("Start:"+start+"\t\tEnd:"+end+"\t\tName:"+name);
		return name;
	}

}
