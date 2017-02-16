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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;

public class ShipyardMapFragmentBuilder implements FragmentBuilder{
	private static final int SHIPYARD_INITIAL = 2;
	
	private ShipyardMapFragment fragment = null;
	private int seekbar = ShipyardMapFragment.INITIAL_SEEKBAR;
	private boolean tracking = false;
	private int shipyard = SHIPYARD_INITIAL;

	
	private static final double DEFAULT_TARGET_LATITUDE = 37.917769;
	private static final double DEFAULT_TARGET_LONGITUDE = -122.3568;
	private static final float DEFAULT_TARGET_ZOOM = 13f;
	private static final float DEFAULT_BEARING = 0.0f;
//	private static final float DEFAULT_TARGET_ZOOM = 13.21f;
	
	public static final double[] DEFAULT_TARGET = {DEFAULT_TARGET_LATITUDE, DEFAULT_TARGET_LONGITUDE};
	private static double[] initialTarget = {DEFAULT_TARGET_LATITUDE, DEFAULT_TARGET_LONGITUDE};
	private static float initialZoom = DEFAULT_TARGET_ZOOM;
	private static float initialBearing = DEFAULT_BEARING;
	
	public String getName(Context context) {
		return context.getString(R.string.shipyard_map);
	}

	public void init(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			double[] temp = savedInstanceState.getDoubleArray("ShipyardMapBuilder.initialTarget");
			if (temp != null) {
				initialTarget = temp;
			}
			initialBearing = savedInstanceState.getFloat("ShipyardMapFragment.initialBearing",DEFAULT_BEARING);
			initialZoom = savedInstanceState.getFloat("ShipyardMapFragment.initialZoom",DEFAULT_TARGET_ZOOM);
			seekbar = savedInstanceState.getInt("ShipyardMapFragment.seekbar",ShipyardMapFragment.INITIAL_SEEKBAR);
			tracking = savedInstanceState.getBoolean("ShipyardMapFragment.tracking",false);
			shipyard = savedInstanceState.getInt("ShipyardMapFragment.shipyard", SHIPYARD_INITIAL);
		}
		return;
	}
	
	public Fragment create() {
		fragment =  ShipyardMapFragment.newInstance(initialTarget,initialZoom, initialBearing, seekbar, tracking, DEFAULT_TARGET, DEFAULT_TARGET_ZOOM, shipyard);
		return (Fragment) fragment;		
	}
	
	public void saveState(Bundle outState) {
		
		if (fragment != null) {
			try {
				initialTarget = fragment.getTarget();
				initialZoom = fragment.getZoom();
				initialBearing = fragment.getBearing();
				seekbar = fragment.getSeekBar();
				tracking = fragment.getTracking();
				shipyard = fragment.getShipyard();
			}
			catch (Exception e) {
				
			}
			fragment = null;
		}
		outState.putDoubleArray("ShipyardMapBuilder.initialTarget", initialTarget);
		outState.putFloat("ShipyardMapFragment.initialZoom", initialZoom);
		outState.putFloat("ShipyardMapFragment.initialBearing", initialBearing);
		outState.putInt("ShipyardMapFragment.seekbar",seekbar);
		outState.putBoolean("ShipyardMapFragment.tracking",tracking);
		outState.putInt("ShipyardMapFragment.shipyard", shipyard);
		fragment = null;

	}

	public void saveState() {
		if (fragment != null) {
			try {
				initialTarget = fragment.getTarget();
				initialZoom = fragment.getZoom();
				initialBearing = fragment.getBearing();
				seekbar = fragment.getSeekBar();
				tracking = fragment.getTracking();
				shipyard = fragment.getShipyard();
			}
			catch (Exception e) {
				
			}
			fragment = null;
		}
		fragment = null;		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if (requestCode == FullScreenMap.RETURN_MAP_STATE) {
				Bundle b = data.getExtras();	    	
		    	LatLng ll = b.getParcelable(FullScreenMap.MAP_CENTER);
		    	initialTarget[0] = ll.latitude;
		    	initialTarget[1] = ll.longitude;
		    	initialZoom = b.getFloat(FullScreenMap.MAP_ZOOM);
		    	initialBearing = b.getFloat(FullScreenMap.MAP_BEARING);		    	
		    }
		}			
	
}
