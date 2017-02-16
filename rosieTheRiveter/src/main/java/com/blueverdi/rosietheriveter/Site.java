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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

public class Site implements Parcelable{
	private static final String TAG = "Site";
	public static final String PARCEL_NAME = "Site Parcel";
	public static final String PARCEL_ARRAY_NAME = "Site Array Parcel";
	
	public static final int IMAGE_FILE = 0;
	public static final int NAME = 1;
	public static final int PHONE = 2;
	public static final int ADDRESS1 = 3;
	public static final int ADDRESS2 = 4;
	public static final int DESCRIPTION1 = 5;
	public static final int DESCRIPTION2 = 6;
	public static final int HOURS = 7;
	public static final int HOURS2 = 8;
	public static final int WEBSITE = 9;
	public static final int ETC = 10;
	public static final int LATITUDE = 11;
	public static final int LONGITUDE = 12;
	public static final int DETAILS = 13;
	public static final int SIZE = 14;
	
	private String[] data;
	private Bitmap myBitmap;
	private SiteDetails details;
	
	public Site() {
		data = new String[SIZE];
		myBitmap = null;
		details = null;
		
	}
	
	public String getString(int id) throws ArrayIndexOutOfBoundsException{
		if ((id < 0) || (id >= SIZE)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return data[id];
	}

	public void setString(int id, String s) throws ArrayIndexOutOfBoundsException{
		if ((id < 0) || (id >= SIZE)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		data[id] = s;
	}
	

	public void setDetails(SiteDetails details) {
		this.details = new SiteDetails(details.name,details.textID, details.captionArrayID, details.website, details.photoDirectory);
	}

	public SiteDetails getDetails() {
		if (details == null) {
			return null;
		}
		else {
			return new SiteDetails(details.name, details.textID, details.captionArrayID, details.website, details.photoDirectory);
		}

	}
	
	public synchronized Bitmap getBitmapFromAssets(Context context) {
		 	if (data[IMAGE_FILE] == null) {
		 		return null;
		 	}
		 	if (myBitmap != null) {
		 		return myBitmap;
		 	}
		   Bitmap bit = null;
		   try {
		        InputStream bitmap=context.getAssets().open(data[IMAGE_FILE]);
		        bit=BitmapFactory.decodeStream(bitmap);
		        myBitmap = bit;
		    } catch (IOException e1) {
		    	MyLog.d(TAG, "error opening file " + data[IMAGE_FILE]);
		        e1.printStackTrace();
		    }	
		    return bit;
		}	
 
	@Override
	public void writeToParcel(Parcel out, int flags) {
	    out.writeStringArray(data);
		
	}
	
	public Site(Parcel in) {
		readFromParcel(in);
	}	
		 
	public void readFromParcel(Parcel in){
		data = in.createStringArray();
	}	
		 
	public static final Parcelable.Creator<Site> CREATOR = new Parcelable.Creator<Site>() {
		public Site createFromParcel(Parcel in) {
			return new Site(in);
		}
		 
		public Site[] newArray(int size) {
			return new Site[size];
		}
	};
	    
	    
	 @Override
	    public int describeContents() {
	        return 0;
	    }
	 
	 public synchronized void releaseBitmap() {
		 if (myBitmap != null) {
			 myBitmap.recycle();
			 myBitmap = null;
		 }
	 }
	
}
