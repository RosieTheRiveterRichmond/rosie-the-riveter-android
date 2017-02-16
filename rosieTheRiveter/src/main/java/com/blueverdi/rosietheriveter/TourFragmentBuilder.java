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

import com.google.android.gms.maps.model.LatLng;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TourFragmentBuilder implements FragmentBuilder{
	private static boolean MAPVIEW_DEFAULT;
	private boolean mapView = MAPVIEW_DEFAULT;
	private TourFragment fragment = null;

	public String getName(Context context) {
		return context.getString(R.string.my_tour);
	}

	public void init(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mapView = savedInstanceState.getBoolean("TourFragmentBuilder.mapView", MAPVIEW_DEFAULT);
		}
		return;
	}
	
	public Fragment create() {
		fragment =  TourFragment.newInstance(mapView);	
		return (Fragment) fragment;
		
	}
	
	public void saveState(Bundle outState) {
		
		if (fragment != null) {
			try {
				mapView = fragment.getMapView();
			}
			catch (Exception e) {
				
			}
			fragment = null;
		}
		outState.putBoolean("TourFragmentBuilder.mapView", mapView);
	}

	public void saveState() {
		try {
			mapView = fragment.getMapView();
		}
		catch (Exception e) {
			
		}
		fragment = null;
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}			

	
}
