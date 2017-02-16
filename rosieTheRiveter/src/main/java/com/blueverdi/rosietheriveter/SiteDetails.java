package com.blueverdi.rosietheriveter;

import android.os.Parcel;
import android.os.Parcelable;
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

public class SiteDetails implements Parcelable{
	public static final String PARCEL_NAME = "Site Details parcel";
	public final String name;
	public final int textID;
	public final int captionArrayID;
	public final String website;
	public final String photoDirectory;
	
	public SiteDetails (String name, int textID, int captionArrayID, String website, String photoDirectory) {
		this.name = name;
		this.textID = textID;
		this.captionArrayID = captionArrayID;
		this.website = website;
		this.photoDirectory = photoDirectory;
	}
	

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeInt(textID);
		out.writeInt(captionArrayID);
		if (website == null) {
			out.writeString("");
		}
		else {
			out.writeString(website);
		}
		out.writeString(photoDirectory);
			
	}
		
	public SiteDetails(Parcel in) {
		name = in.readString();
		textID = in.readInt();
		captionArrayID = in.readInt();
		String s = in.readString();
		if (s.equals("")) {
			website = null;
		}
		else {
			website = s;
		}
		photoDirectory = in.readString();
	}	
			 
	public static final Parcelable.Creator<SiteDetails> CREATOR = new Parcelable.Creator<SiteDetails>() {
		public SiteDetails createFromParcel(Parcel in) {
			return new SiteDetails(in);
		}
			 
		public SiteDetails[] newArray(int size) {
			return new SiteDetails[size];
		}
};
		    
	
	
}