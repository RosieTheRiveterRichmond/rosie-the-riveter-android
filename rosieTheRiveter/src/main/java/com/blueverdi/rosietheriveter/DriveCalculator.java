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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class DriveCalculator {
	
	// DriveCalculator internal classes
	// ================================
	public static class DrivingInfo {
		public final String distance;
		public final String drivingTime;
		public DrivingInfo(String distance, String drivingTime) {
			this.distance = distance;
			this.drivingTime = drivingTime;
		}
	}

	private class DrivingDistance {
		
		private static final String queryBase = "https://maps.googleapis.com/maps/api/distancematrix/json?";
		public final double startLong;
		public final double startLat;
		public final double endLong;
		public final double endLat;
		private String distance;
		private String time;
								
		public DrivingDistance(double startLong, double startLat, double endLong, double endLat) {
			this.startLong = startLong;
			this.startLat = startLat;
			this.endLong = endLong;
			this.endLat = endLat;
			this.distance = distance;
			this.time = time;
			DrivingInfo di = askGoogle(startLat, startLong, endLat,endLong);
			if (di == null) {
				this.distance = activity.getString(R.string.driving_information);
				this.time= activity.getString(R.string.not_available);
			}
			else {
				this.distance = di.distance;
				this.time = di.drivingTime;
			}
		}
		
		public DrivingInfo toDrivingInfo() {
			return new DrivingInfo(this.distance,this.time);
		}

		
		public  boolean equals(DrivingDistance d) {
			if ((d.startLong == this.startLong) && (d.startLat == this.startLat) && (d.endLong == this.endLong) && (d.endLat == this.endLat)) {
				return true;
			}
			else {
				return false;
			}
		}
				
		private DrivingInfo askGoogle(double startLat, double startLong, double endLat, double endLong) {		
		    StringBuilder query = new StringBuilder();
		    query.append(queryBase);
		    query.append("origins=");//from
		    query.append(String.valueOf(startLat));
		    query.append(",");
		    query.append( String.valueOf(startLong));
		    query.append("&destinations=");
		    query.append(String.valueOf(endLat));
		    query.append(",");
		    query.append(String.valueOf(endLong));
		    query.append("&units=imperial");
			
//			String query = queryBase+"&origin="+ String.valueOf(startLat) + "," + String.valueOf(startLong) + "&destinations=" 
//					+ String.valueOf(endLat) + "," + String.valueOf(endLong) + "&units=imperial";

			try {
			    ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
			    Bundle bundle = ai.metaData;
			    String myApiKey = bundle.getString("com.google.android.maps.v2.API_KEY");
			    query.append("&key=");
			    query.append(myApiKey);
//			    query.append("invalid key"); // debug!!
			} catch (NameNotFoundException e) {
			    MyLog.d(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
			} catch (NullPointerException e) {
			    MyLog.d(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());        
			}	
		    MyLog.d(TAG," distance query: "+ query.toString());
			
		    HttpURLConnection urlConnection= null;
		    URL url = null;
		    StringBuilder response = new StringBuilder();;
		    try {
			    url = new URL(query.toString());
			    urlConnection=(HttpURLConnection)url.openConnection();
			    urlConnection.setRequestMethod("GET");
			    urlConnection.setDoOutput(true);
			    urlConnection.setDoInput(true);
			    urlConnection.connect();
	
			    InputStream inStream = urlConnection.getInputStream();
			    BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));	
			    String temp;
			    while((temp = bReader.readLine()) != null){
			        //Parse data
			        response.append(temp);
			    }
			    MyLog.d(TAG, "RESPONSE");
			    MyLog.d(TAG, response.toString());
			    //Close the reader, stream & connection
			    bReader.close();
			    inStream.close();
			    urlConnection.disconnect();		    
			}
			catch (Exception e) {
				MyLog.d(TAG, "query exception");
				e.printStackTrace();
				return null;
			}
		    try {
			    JSONObject object = (JSONObject) new JSONTokener(response.toString()).nextValue();
			    JSONArray array = object.getJSONArray("rows");
			    JSONObject routes = array.getJSONObject(0);
			    JSONArray legs = routes.getJSONArray("elements");
			    JSONObject steps = legs.getJSONObject(0);
			    JSONObject distance = steps.getJSONObject("distance");
			    String sDistance = distance.getString("text");
			    JSONObject duration = steps.getJSONObject("duration");	
		        String sDuration = duration.getString("text");	
		        return new DrivingInfo(sDistance,sDuration);
		    }
		    catch (Exception e) {
		    	MyLog.d(TAG, "exception parsing result");
		    	return null;
		    }

		}
	}

	// DriveCalculator fields and methods
	// ==================================
	private static final String TAG = "DriveCalculator";
	private final Activity activity;
	private Map<Integer, DrivingDistance> distanceCache = new HashMap<Integer, DrivingDistance>();
	
	private Integer calculateHash(double startLong, double startLat, double endLong, double endLat) {
		return ((int) (startLong * 1000)) + ((int) (startLat * 2000)) + ((int) (endLong * 3000)) + ((int) (endLat * 4000));
	}
	public DriveCalculator(Activity activity) {
		this.activity = activity;
	}

	public DrivingInfo getDrivingInfo(double startLong, double startLat, double endLong, double endLat) {
		Integer key = calculateHash(startLat, startLong, endLat, endLong);
		DrivingDistance d = distanceCache.get(key);
		if (d == null) {
			d = new DrivingDistance(startLong,startLat,endLong, endLat);
			distanceCache.put(key, d);			
		}
		if (d == null) {
			return null;
		}
		else {
			return d.toDrivingInfo();
		}
		
	}
}
