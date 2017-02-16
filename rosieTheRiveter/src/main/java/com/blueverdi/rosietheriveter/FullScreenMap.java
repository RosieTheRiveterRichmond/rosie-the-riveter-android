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

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class FullScreenMap extends Activity {
	private static final String TAG = "FullScreenMapActivity";
	public static final String MAP_FILE = "MapFile";
	public static final String MAP_CENTER = "MapCenter";
	public static final String MAP_ZOOM = "MapZoom";
	public static final String MAP_BEARING = "MapBearing";
	public static final String SHOW_LOCATION = "ShowLocation";	
	public static final String OVERLAY_LATLNG = "OverlayLatLng";
	public static final String OVERLAY_WIDTH = "OverlayWidth";
	public static final String OVERLAY_TILT = "OverlayTilt";
	public static final String OVERLAY_TRANSPARENCY = "OverlayTransparency";
	public static final int RETURN_MAP_STATE = 1;
	
	private RosieHelpMenu rosieHelpMenu;
	private LatLng center;
	private float zoom;
	private boolean showLocation;
	
	private LatLng imageAnchor;
	private float imageWidth;
	private float imageTilt;
	private float imageTransparency;
	private float bearing;
	private BitmapDescriptor imageBitmap;
	private GoogleMap map;
	private Activity activity;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		int image = b.getInt(MAP_FILE,0);
		activity = this;
		if (image == 0) {
			MyLog.d(TAG, "no map file passed passed");
			finish();
		}
	    imageBitmap = BitmapDescriptorFactory.fromResource(image);     	   

		zoom = b.getFloat(MAP_ZOOM);
		center = b.getParcelable(MAP_CENTER);
		showLocation = b.getBoolean(SHOW_LOCATION);
		imageWidth = b.getFloat(OVERLAY_WIDTH);
		imageTilt = b.getFloat(OVERLAY_TILT);
		imageTransparency = b.getFloat(OVERLAY_TRANSPARENCY);
		imageAnchor = b.getParcelable(OVERLAY_LATLNG);
		bearing = b.getFloat(MAP_BEARING);
		setContentView(R.layout.full_screen_map_activity);
		MapFragment mf = (MapFragment)getFragmentManager().findFragmentById(R.id.fullScreenMap);
        mf.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
                    if (!Utils.isNetworkAvailable(activity)) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(activity, activity.getString(R.string.internet_required), Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				GroundOverlayOptions options = new GroundOverlayOptions()
						.image(imageBitmap)
						.position(imageAnchor, imageWidth)
						.bearing(imageTilt)
						.transparency(imageTransparency)
						.zIndex(ShipyardMapFragment.OVERLAY_ZINDEX);
				map.addGroundOverlay(options);
				CameraPosition defaultPosition = new CameraPosition(center, zoom, 0.0f, bearing);
				CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultPosition);

				map.moveCamera(cameraUpdate);
				if (showLocation) {
					map.setMyLocationEnabled(true);
				}
			}
		});
	    rosieHelpMenu = new RosieHelpMenu(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return rosieHelpMenu.createOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return rosieHelpMenu.processMenuItem(item);
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
        	returnStatus();
        	finish();
            return true;
        }
        return false;
    }
	
	private void returnStatus() {
    	Intent returnIntent = new Intent();
    	returnIntent.putExtra(MAP_CENTER,map.getCameraPosition().target);
    	returnIntent.putExtra(MAP_ZOOM,map.getCameraPosition().zoom);
    	returnIntent.putExtra(MAP_BEARING, map.getCameraPosition().bearing);
    	setResult(RESULT_OK,returnIntent);		
	}

}
