package com.taevas.lexustv;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlaybackController implements MediaPlayer.OnCompletionListener {

	private final VideoView wVideoView;
	private final ImageView wImageView;
	private final MainActivity mainActivity;
	
	public Playlist currentPlaylist;
	
	public String currentFilename;

	private final ScheduledExecutorService executor;
	
	public PlaybackController(MainActivity activity, Playlist playlist) {
		mainActivity = activity;
		
		wVideoView = activity.wVideoView;
		wVideoView.setOnCompletionListener(this);
		
		wImageView = activity.wImageView;
		
		currentPlaylist = playlist;
		//currentPlaylist = mainActivity.currentActivePlaylist;
		
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mainActivity.startPlaying();
		//playNextMedia();
	}

	public void pause() {
		if(currentFilename != null){
	        wVideoView.pause();
	    }
	}

	public void callPlay() {
		mainActivity.startPlaying();
	}

	public void playNextMedia()
	{
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
		// A video has finished playing.
		if (currentPlaylist == null)
		{
			//Toast.makeText(mainActivity.getBaseContext(), "No more videos to play.", Toast.LENGTH_SHORT).show();
			return;
		}
		// Plays next video.
		currentFilename = currentPlaylist.goToNext();
		if (currentFilename != null) {
			Log.d("PLOP", "next: " + currentFilename);

			if (Playlist.isVideo(currentFilename)) {

				// Plays video.
				Uri video = Uri.fromFile(new File(currentFilename));
				try {
					wVideoView.setVideoURI(video);
				} catch (Exception ex) {
					// nothing to do - this was not a valid time string.
					//Log.d("PLOP", ex + " :videoSetURI");
				}
				//wVideoView.setVideoURI(video);
				//Log.d("PLOP", "Video path: " + video);
				wVideoView.setVisibility(View.VISIBLE);
				wVideoView.start();

				// Hide image.
				wImageView.setVisibility(View.INVISIBLE);

			} else if (Playlist.isImage(currentFilename)) {

				//Log.d("PLOP", "is image: " + currentFilename);

				// Plays image.
				wImageView.setImageURI(Uri.parse(currentFilename));
				wImageView.setVisibility(View.VISIBLE);
				int playbackTimeSecs = getPlaybackTimeFromFilename(currentFilename);
				//Log.d("PLOP", playbackTimeSecs + "");
				//Toast.makeText(mainActivity.getBaseContext(), playbackTimeSecs + " seconds", Toast.LENGTH_SHORT).show();
				if (playbackTimeSecs > 0) {
					startPlaybackTimer(playbackTimeSecs);
				}

				// Hides video.
				wVideoView.setVisibility(View.INVISIBLE);

			}
		}

		// Toast
		//Toast.makeText(getBaseContext(),"Now playing " + currentFilename, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void startPlaybackTimer(int playbackTimeSecs) {
		//final Runnable r = this::playNextMedia;
		final Runnable r = this::callPlay;

		executor.schedule(r, (long)playbackTimeSecs, TimeUnit.SECONDS);
	}

	private int getPlaybackTimeFromFilename(String filename) {
		int ret = 15; // default playback time is 15 seconds.

		String[] elements = filename.split("_");

		if (elements != null) {
			for (String s : elements) {
				//Log.d("PLOP", Integer.parseInt(s.replaceAll("[\\D]", "")) + " :getPlaybackTimeFromFilenameARRAY");
				if (Integer.parseInt(s.replaceAll("[\\D]", "")) != 0) {
					ret = Integer.parseInt(s.replaceAll("[\\D]", ""));
				}
				if (s.endsWith("s.")) {
					try {
						ret = Integer.parseInt(s.substring(0, s.length() - 1));
						if (ret < 1) {
							ret = -1;
						}
					} catch (Exception ex) {
						// nothing to do - this was not a valid time string.
						//Log.d("PLOP", ex + " :getPlaybackTimeFromFilenameERROR");
					}
				}
			}
		}
		Log.d("PLOP", ret + " :getPlaybackTimeFromFilename");
		return ret;
	}

}
