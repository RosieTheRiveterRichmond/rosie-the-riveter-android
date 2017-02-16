/* Copyright (c) 2014 - 2017 Bradley Justice
MIT LICENSE
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.blueverdi.rosietheriveter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.commons.io.FilenameUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AlbumPageFactory {
	// creates albumPages based on the image and audio directories passed in the call. Image in the albumPage
	// is taken from the file index in the directory. If there is a corresponding filename in the audio
	// directory with an mp3 extension the mp3 field contains the absolute path to that file.
	private final String TAG = "AlbumPageFactory";
	private String imageSource;
	private String audioSource;
	private String[] imageFiles;
	private Context context;
	
	
	public class AlbumPage { 
		public String BitmapName;
		public  Bitmap bitmap; // bitmap to display
		public String mp3;	  // absolute path to associated audio file, if any, else null	
	}
	
	public AlbumPageFactory (Context context, String imageSource, String audioSource){
		this.imageSource = imageSource; 
		this.audioSource = audioSource;
		this.context = context;
		try {
			imageFiles = context.getAssets().list(imageSource);
		}
		catch (Exception e) {
			MyLog.d(TAG, e.toString()); 
			e.printStackTrace();
			imageFiles = new String[0];
		}	
	}
	
	public int totalPages() {
		return imageFiles.length;
	}
	
	public AlbumPage getAlbumPage(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index < 0");
		}
		if (index >= imageFiles.length) {
			throw new IllegalArgumentException("index too large");			
		}
		AlbumPage ret = new AlbumPage();
		ret.BitmapName = getBitmapName(imageFiles[index]);
		ret.bitmap = getBitmapFromAssets(ret.BitmapName);
		String audioFileName =   FilenameUtils.concat(audioSource,FilenameUtils.getBaseName(imageFiles[index]) + ".mp3");
		ret.mp3 = null;
		try {
			context.getAssets().open(audioFileName);
			ret.mp3 = audioFileName;

		} catch (IOException ex) {

		}		
		return ret;
	}

	private String getBitmapName(String filename) {
		return FilenameUtils.concat(imageSource, filename);
	}
	
	 private  Bitmap getBitmapFromAssets(String fn) {
		   Bitmap bit = null;
		   try {
		        InputStream bitmap=context.getAssets().open(fn);
		        bit=BitmapFactory.decodeStream(bitmap);
		    } catch (IOException e1) {
		        e1.printStackTrace();
		    }		    
		    return bit;
		}	
	
}
