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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MySqliteHelperMyTour extends SQLiteOpenHelper {

	private static final String TAG = "MySQLiteHelperMyTour";

	private  static final String dbName="MyTourDB";
	private static final String myTourTable="MyTour";
	
	private static final String colId = "COL_ID";   
	private static final String colImageFile = "IMAGE_FILE";
	private static final String colName = "NAME";
	private static final String colPhone = "PHONE";
	private static final String colAddress1 = "ADDRESS1";
	private static final String colAddress2 = "ADDRESS2";
	private static final String colDescription1 = "DESCRIPTION1";
	private static final String colDescription2 = "DESCRIPTION2";
	private static final String colHours = "HOURS";
	private static final String colHours2 = "HOURS2";
	private static final String colWebsite = "WEBSITE";
	private static final String colEtc = "ETC";
	private static final String colLatitude = "LATITUDE";
	private static final String colLongitude = "LONGITUDE";
	private static final String colDetails = "DETAILS";
	private static final String colDetailsName = "DETAILS_NAME";
	private static final String colDetailsTextId = "DETAILS_TEXTID";
	private static final String colDetailsCaptionArrayId = "DETAILS_CAPTIONARRAYID";
	private static final String colDetailsWebsite = "DETAILS_WEBSITE";
	private static final String colDetailsPhotoDirectory = "DETAILS_PHOTODIRECTORY";			

	private static final int DATABASE_VERSION = 1;

	
	public MySqliteHelperMyTour(Context context) {
		super(context, dbName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		  MyLog.d(TAG,"onCreate called");
		  database.execSQL("CREATE TABLE " + myTourTable +
				   "("+ BaseColumns._ID +  " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   colImageFile  + " TEXT, " +
				        colName     + " TEXT, " + 
				        colPhone      + " TEXT, " + 
				        colAddress1     + " TEXT, " + 
				        colAddress2 + " TEXT, " + 
				        colDescription1   + " TEXT, " +
				        colDescription2   + " TEXT, " +
				        colHours   + " TEXT, " +
				        colHours2   + " TEXT, " +
				        colWebsite   + " TEXT, " +
				        colEtc   + " TEXT, " +
				        colLatitude   + " TEXT, " +
				        colLongitude + " TEXT, " +
				        colDetails + " INTEGER," + 
				        colDetailsName + " TEXT," + 
				        colDetailsTextId + " INTEGER, " + 
				        colDetailsCaptionArrayId + " INTEGER," + 
				        colDetailsWebsite + " TEXT," + 
				        colDetailsPhotoDirectory + " TEXT);");	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		MyLog.w(TAG,
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS" + dbName);
		onCreate(db);
	}

	
	public void addSite(Site site) {
		SQLiteDatabase db= this.getWritableDatabase();			
		ContentValues cv=new ContentValues();

   
		cv.put(colImageFile,site.getString(Site.IMAGE_FILE));
		cv.put(colName,site.getString(Site.NAME));
		cv.put(colPhone,site.getString(Site.PHONE));
		cv.put(colAddress1,site.getString(Site.ADDRESS1));
		cv.put(colAddress2,site.getString(Site.ADDRESS2));
		cv.put(colDescription1,site.getString(Site.DESCRIPTION1));
		cv.put(colDescription2,site.getString(Site.ADDRESS2));
		cv.put(colHours,site.getString(Site.HOURS));
		cv.put(colHours2,site.getString(Site.HOURS2));
		if (site.getString(Site.WEBSITE) == null) {
			cv.put(colWebsite,"");
			
		}
		else {
			cv.put(colWebsite,site.getString(Site.WEBSITE));
		}
		if (site.getString(Site.ETC) == null) {
			cv.put(colEtc,"");
			
		}
		else {
			cv.put(colEtc,site.getString(Site.ETC));
		}
		
		cv.put(colLatitude,site.getString(Site.LATITUDE));
		cv.put(colLongitude,site.getString(Site.LONGITUDE));

		SiteDetails sd = site.getDetails();
		if (sd == null) {
			cv.put(colDetails, 0);
		}
		else {
			cv.put(colDetails, 1);
			cv.put(colDetailsName, sd.name);
			cv.put(colDetailsTextId, sd.textID);
			cv.put(colDetailsCaptionArrayId, sd.captionArrayID);
			cv.put(colDetailsWebsite, sd.website);
			cv.put(colDetailsPhotoDirectory, sd.photoDirectory);
		}
		db.insert(myTourTable, null, cv);
		db.close();
		
	}
		
	public int getSiteCount() {
		
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cur= db.rawQuery("Select * from "+ myTourTable, null);
		int x= cur.getCount();
		cur.close();
		db.close();
		return x;
		
	}
	
	public void deleteSiteByName(String name) {

	    SQLiteDatabase db=this.getReadableDatabase();
	    
		db.delete(myTourTable,BaseColumns._ID + "=?", new String [] {name});
		db.close();
		return;

	}

	public void deleteAllSites() {
	    SQLiteDatabase db=this.getWritableDatabase();
	    db.delete(myTourTable, null, null);
	    db.close();
	}
	
	public ArrayList<Site> getSites() {
	    SQLiteDatabase db=this.getReadableDatabase();
	    ArrayList<Site> ret = new ArrayList<Site>();
		Cursor c= db.rawQuery("Select * from "+ myTourTable,null);
		c.moveToFirst();
		while (c.isAfterLast() == false) 
		{
	    	Site s = new Site();
	    	s.setString(Site.IMAGE_FILE,c.getString(c.getColumnIndex(colImageFile)));
	    	s.setString(Site.NAME,c.getString(c.getColumnIndex(colName)));
	    	s.setString(Site.PHONE,c.getString(c.getColumnIndex(colPhone)));
	    	s.setString(Site.ADDRESS1,c.getString(c.getColumnIndex(colAddress1)));
	    	s.setString(Site.ADDRESS2,c.getString(c.getColumnIndex(colAddress2)));
	    	s.setString(Site.DESCRIPTION1,c.getString(c.getColumnIndex(colDescription1)));
	    	s.setString(Site.DESCRIPTION2,c.getString(c.getColumnIndex(colDescription2)));
	    	s.setString(Site.HOURS,c.getString(c.getColumnIndex(colHours)));
	    	s.setString(Site.HOURS2,c.getString(c.getColumnIndex(colHours2)));
	    	s.setString(Site.WEBSITE,c.getString(c.getColumnIndex(colWebsite)));
	    	s.setString(Site.ETC,c.getString(c.getColumnIndex(colEtc)));
	    	s.setString(Site.LATITUDE,c.getString(c.getColumnIndex(colLatitude)));
	    	s.setString(Site.LONGITUDE,c.getString(c.getColumnIndex(colLongitude)));
	    	if (s.getString(Site.WEBSITE).equals("")) {
	    		s.setString(Site.WEBSITE, null);
	    	}
	    	if (s.getString(Site.ETC).equals("")) {
	    		s.setString(Site.ETC, null);
	    	}
	    	if (c.getInt(c.getColumnIndex(colDetails)) != 0) {
	    		SiteDetails sd = new SiteDetails(c.getString(c.getColumnIndex(colDetailsName)),
	    										 c.getInt(c.getColumnIndex(colDetailsTextId)),
	    										 c.getInt(c.getColumnIndex(colDetailsCaptionArrayId)),
	    										 c.getString(c.getColumnIndex(colWebsite)),
	    										 c.getString(c.getColumnIndex(colDetailsPhotoDirectory)));
	    		s.setDetails(sd);
	    	}
	    	ret.add(s);
	    	c.moveToNext(); 
	    } 

	    c.close();
	    db.close();
	    return ret;
	}
	
	Site getSiteByName(String name) {
		Site ret;
	
	    SQLiteDatabase db=this.getReadableDatabase();
	    String where = colName + "=?"; // the condition for the row(s) you want returned.
	    String[] whereArgs = new String[] { // The value of the column specified above for the rows to be included in the response
	    		name };
		String [] COLUMNS=new String[]{colImageFile, colName, colPhone, colAddress1, colAddress2, colDescription1, 
				colDescription2, colHours, colHours2, colWebsite, colEtc, colLatitude, colLongitude};	    
		Cursor c=db.query(myTourTable, COLUMNS, where, 
			whereArgs, null, null, null);
		if (c.getCount() == 0) {
			ret = null;
		}
		else {
			c.moveToFirst();
	    	ret = new Site();
	    	ret.setString(Site.IMAGE_FILE,c.getString(c.getColumnIndex(colImageFile)));
	    	ret.setString(Site.NAME,c.getString(c.getColumnIndex(colName)));
	    	ret.setString(Site.PHONE,c.getString(c.getColumnIndex(colPhone)));
	    	ret.setString(Site.ADDRESS1,c.getString(c.getColumnIndex(colAddress1)));
	    	ret.setString(Site.ADDRESS2,c.getString(c.getColumnIndex(colAddress2)));
	    	ret.setString(Site.DESCRIPTION1,c.getString(c.getColumnIndex(colDescription1)));
	    	ret.setString(Site.DESCRIPTION2,c.getString(c.getColumnIndex(colDescription2)));
	    	ret.setString(Site.HOURS,c.getString(c.getColumnIndex(colHours)));
	    	ret.setString(Site.HOURS2,c.getString(c.getColumnIndex(colHours2)));
	    	ret.setString(Site.WEBSITE,c.getString(c.getColumnIndex(colWebsite)));
	    	ret.setString(Site.ETC,c.getString(c.getColumnIndex(colEtc)));
	    	ret.setString(Site.LATITUDE,c.getString(c.getColumnIndex(colLatitude)));
	    	ret.setString(Site.LONGITUDE,c.getString(c.getColumnIndex(colLongitude)));
	    	if (ret.getString(Site.WEBSITE).equals("")) {
	    		ret.setString(Site.WEBSITE, null);
	    	}
	    	if (ret.getString(Site.ETC).equals("")) {
	    		ret.setString(Site.ETC, null);
	    	}
		}

	    c.close();
	    db.close();
	    return ret;
		
	}
}
