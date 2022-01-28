package com.taevas.lexustv;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Playlist extends ArrayList<String> {
	
	private static final long serialVersionUID = 1L;
	public int currentItem = -1;
	
	/**
	 * Creates a Playlist from a directory.
	 * @param directoryPath Path of the directory.
	 */
	public Playlist(String directoryPath, Boolean online)
	{
		// loads all media items from the directory.
		if (online) {
			this.clear();
			currentItem = -1;
			this.addAll(getMediaList(new File(directoryPath)));
		} else {
			this.clear();
			currentItem = -1;
			this.addAll(getMediaList(new File(directoryPath)));
		}
	}
	
	private List<String> getMediaList(File parentDir) 
	{
		ArrayList<String> inFiles = null;
		inFiles = new ArrayList<String>();

	    File[] files = parentDir.listFiles();
	    
	    for (File file : files) {
	        if (file.isDirectory()) {
	            inFiles.addAll(getMediaList(file));
	        } else {
	        	String fileName = file.getName();
	            if(isImage(fileName) || isVideo(fileName)){
	                inFiles.add(file.getAbsolutePath());
					//Log.d("PLOP", "Added: " + file.getAbsolutePath());
	            }
	        }
	    }
	    return inFiles;
	}
	
	public String getCurrent()
	{
		if (currentItem >= 0 && currentItem < size())
		{
			return get(currentItem);
		} 
		else 
		{
			return null;
		}
	}
	
	public String goToNext() {
		// Nothing to do if there's no media!
		if (size() < 1) 
		{
			return null;
		}

		// Gets the next item to play.
		currentItem++;
		if (currentItem >= size()) 
		{
			// No more media in the playlist, restart from the first.
			currentItem = 0;
		}
		
		return get(currentItem);
	}
	
	public static boolean isImage(String filename) {
		return filename.toLowerCase(Locale.ROOT).endsWith(".jpg");
	}

	public static boolean isVideo(String filename) {
		String lowerFilename = filename.toLowerCase(Locale.ROOT);
		return lowerFilename.endsWith(".mp4") || lowerFilename.endsWith(".mov");
	}
}
