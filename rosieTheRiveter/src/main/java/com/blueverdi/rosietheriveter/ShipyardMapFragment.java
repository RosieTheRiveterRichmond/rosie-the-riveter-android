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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class ShipyardMapFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener{
	private static final String TAG = "ShipyardMapFragment";
	public static final String useLargeBitmapsPref = "useLargeBitmaps";
	public static final String showHintPref = "showFullScreenHint";
	
	public static final String ROSIE_MAP = "rosie.map";
	private static final String THEME_ZIP = "elevate.zip";
	private static final String THEME_FILE = "elevate/theme.xml";
	private static final int SEEKBAR_MAX = 100;
	private CheckBox trackMe;
	private SeekBar seekBar;
	private static boolean tracking = false;
//	private GoogleMap map;
	private RosieMapView mapView;
	
    private static final float TILT = -6.0f;
    private static final LatLng ANCHOR = new LatLng(37.913600, -122.351264);
    private static final float WIDTH = 1410f;
    public static final float TILE_PRIVIDER_ZINDEX = 2;
    public static final float OVERLAY_ZINDEX = TILE_PRIVIDER_ZINDEX + 2;
	public static final boolean SUPPORT_OFFLINE = true;
   		
//    private static final float TILT2 = -26.85f;
    private static final float TILT2 = -26.85f;
    private static final LatLng ANCHOR2 = new LatLng(37.9220, -122.36415);
//    private static final float WIDTH2 = 780f;
    private static final float WIDTH2 = 790f;


//  private static final float TILT3 = -110.77f;
//  private static final LatLng ANCHOR3 = new LatLng(37.90842, -122.368778);
//  private static final float WIDTH3 = 1109f;
  private static final float TILT3 = -110.77f;
  private static final LatLng ANCHOR3 = new LatLng(37.90842, -122.36879);
  private static final float WIDTH3 = 1109f;
   
    
//    private static final float TILT4 = -10.85f;
    private static final float TILT4 = -10.35f;
    private static final LatLng ANCHOR4 = new LatLng(37.919882, -122.372308);
//    private static final float WIDTH2 = 780f;
    private static final float WIDTH4 = 725f;
    
    private static final LatLng SHIPYARD1 = new LatLng(37.919323, -122.364825);
    private static final LatLng SHIPYARD2 = new LatLng(37.915304, -122.350305);
    private static final LatLng SHIPYARD3 = new LatLng(37.905524, -122.366559);
    private static final LatLng SHIPYARD4 = new LatLng(37.921912, -122.374006);
    private static final int MARKER_TRIGGER = SEEKBAR_MAX/2;
    public static final int INITIAL_SEEKBAR = MARKER_TRIGGER + (SEEKBAR_MAX/20);
        
    private static CameraPosition defaultPosition = null;
	private static  CameraPosition initialPosition = null;
	private static float defaultZoom;
	private static float zoom;
	private static int seekBarSetting = INITIAL_SEEKBAR;
	private static int shipyard;
	private float minZoomLevel = 0;
	private static CameraPosition lastPosition;
	private MyMapsforgeTheme theme;
	private boolean networkAvailable;
	private boolean useLargeBitmaps = false;
	private SharedPreferences mPrefs;   
	private boolean markersDisplayed = false;
	private Marker sy1;
	private Marker sy2;
	private Marker sy3;
	private Marker sy4;

	private GroundOverlay imageOverlay = null;
	private BitmapDescriptor imageBitmap = null;
	private View myView;
	
	private int bitmapID;
	private static boolean FULL_SCREEN_SUPPORTED = true;
    private boolean showHint;
	private AlertDialog hintDialog = null;	
	private ShipyardMapFragment thisFragment;
//	private GoogleMap map;
	
	public static ShipyardMapFragment newInstance(double[] initialLatLng, float initialZoom, float initialBearing, int seekbar, boolean initTracking, double[] defaultLatLng, float defaultZoom, int initialShipyard) {
		LatLng initial = new LatLng(initialLatLng[0], initialLatLng[1]);
		initialPosition = new CameraPosition(initial,initialZoom,0.0f, initialBearing);
		LatLng defaultP = new LatLng(defaultLatLng[0], defaultLatLng[1]);
		defaultPosition = new CameraPosition(defaultP,defaultZoom,0.0f,0.0f);
//		seekBarSetting = seekbar;
		lastPosition = initialPosition;
		tracking = initTracking;
		shipyard = initialShipyard;
		return new ShipyardMapFragment();
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	thisFragment = this;
       	Toast.makeText(getActivity(), getActivity().getString(R.string.getting_map), Toast.LENGTH_SHORT).show();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		useLargeBitmaps = mPrefs.getBoolean(useLargeBitmapsPref, false);
		
  	    if (useLargeBitmaps)	 {
  	    	MyLog.d(TAG, "USING LARGE BITMAPS!");
  	    }
  	    else {
 	    	MyLog.d(TAG, "using small bitmaps");
 	    	
  	    }
    	// Inflate the layout for this fragment
    	setRetainInstance(false);
    	networkAvailable = Utils.isNetworkAvailable(getActivity());
    	try  {
    		myView = inflater.inflate(R.layout.shipyard_map_fragment, container, false);
    	}
    	catch (Exception e) {
    		MyLog.d(TAG, e.getMessage());
    		MyLog.d(TAG, e.toString());
    		e.printStackTrace();
    	}

        trackMe = (CheckBox) myView.findViewById(R.id.track_me);
        trackMe.setChecked(tracking);
        trackMe.setOnClickListener(this);
        seekBar = (SeekBar) myView.findViewById(R.id.seekbar);
        seekBar.setMax(SEEKBAR_MAX);
        seekBar.setProgress(seekBarSetting);
        seekBar.setOnSeekBarChangeListener(this); 
        seekBar.setProgress(seekBarSetting);
        mapView = (RosieMapView) myView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
                final GoogleMap map = googleMap;
                if (FULL_SCREEN_SUPPORTED) {
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng point) {
                            if (!networkAvailable) {
                                thisFragment.getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), getString(R.string.internet_required), Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                            launchFullScreenMap();
                        }
                    });
                }
                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
                try {
                    MapsInitializer.initialize(thisFragment.getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!networkAvailable) {
                    thisFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.internet_required), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(initialPosition);
                map.animateCamera(cameraUpdate);
                initializeButtonsAndShipyard();
                if (tracking) {
                    googleMap.setMyLocationEnabled(true);
                }
            }

		});
        hintScreen();
    	return myView;
	}


    void initializeButtonsAndShipyard() {
        RadioButton rb = (RadioButton)myView.findViewById(R.id.radioShipyard1);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSeekbarToDefault();
                setButtonStates(v);
            }
        });
        if (shipyard == 1) {
            setButtonStates(rb);
        }

        rb = (RadioButton)myView.findViewById(R.id.radioShipyard2);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSeekbarToDefault();
                setButtonStates(v);
            }
        });
        if (shipyard == 2) {
            setButtonStates(rb);
        }

        rb = (RadioButton)myView.findViewById(R.id.radioShipyard3);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSeekbarToDefault();
                setButtonStates(v);
            }
        });
        if (shipyard == 3) {
            setButtonStates(rb);
        }


        rb = (RadioButton)myView.findViewById(R.id.radioShipyard4);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSeekbarToDefault();
                setButtonStates(v);
            }
        });
        if (shipyard == 4) {
            setButtonStates(rb);
        }

    }

    public double[] getTarget() { 
    	double ret[] = new double[] {lastPosition.target.latitude, lastPosition.target.longitude};
         return ret;    
    }

    public float getBearing() {
    	return lastPosition.bearing;
    }
    
    public float getZoom() {
    	return lastPosition.zoom;
    }
   
    public int getSeekBar() {
    	return seekBarSetting;
    }
 
    public boolean getTracking() {
    	return tracking;
    }
 
    public int getShipyard() {
    	return shipyard;
    }
    
    public void onClick(View v) {
    	switch (v.getId()) {
    		case R.id.track_me:
		    	if (((CheckBox) v).isChecked() ){
		    		tracking = true;
		    	}
		    	else {
		    		tracking = false;
		   		
		    	}
				mapView.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap googleMap) {
						googleMap.setMyLocationEnabled(tracking);
					}
				});
				break;
				
	    	case R.id.exitHintButton:
	    		SharedPreferences.Editor editor = mPrefs.edit();
	    		editor.putBoolean(showHintPref, showHint);
	    		editor.commit(); // Very important to save the preference 
	    		hintDialog.dismiss();  
	    		hintDialog = null;	    		
	    		break;
				
    	}	
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) 
	{
    	if (fromTouch) {
            try {
                imageOverlay.setTransparency(((float) progress) / ((float) SEEKBAR_MAX));
            }
            catch (Exception e) {
                MyLog.d(TAG,"exception setting map transparency");
            }
			seekBarSetting = progress;
			updateMarkers(seekBarSetting);
    	}
	}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
                                 
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    	
    }
 
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    	imageBitmap = null;
       
    }
    
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    	trackMe = null;
    	seekBar = null;
//    	map = null;
    	mapView = null;
    	
        defaultPosition = null;
    	initialPosition = null;
    	lastPosition = null;
    	theme = null;
    	sy1 = null;
    	sy2 = null;
    	sy3 = null;
    	sy4 = null;
    	
    	undisplayShipyard();
    	imageBitmap = null;
    	myView = null;
    	thisFragment = null;

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }    
 
    private void updateMarkers(int newSeekbar) {
    	if (newSeekbar > MARKER_TRIGGER) {
    		if (!markersDisplayed) {
				mapView.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap googleMap) {
						sy1 = googleMap.addMarker(new MarkerOptions()
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1))
								.position(SHIPYARD1)
								.title(getString(R.string.kaiser_shipyard_1)));

						sy2 = googleMap.addMarker(new MarkerOptions()
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2))
								.position(SHIPYARD2)
								.title(getString(R.string.kaiser_shipyard_2)));

						sy3 = googleMap.addMarker(new MarkerOptions()
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3))
								.position(SHIPYARD3)
								.title(getString(R.string.kaiser_shipyard_3)));

						sy4 = googleMap.addMarker(new MarkerOptions()
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker4))
								.position(SHIPYARD4)
								.title(getString(R.string.kaiser_shipyard_4)));
						markersDisplayed = true;
					}
				});

    		}
    	}
    	else {
    		if (markersDisplayed) {
    			try {
    				sy1.remove();
    				sy2.remove();
    				sy3.remove();
    				sy4.remove();
    			}
    			catch (Exception e) {
    				MyLog.d(TAG, "exception clearing markers");
    				
    			}
    			markersDisplayed = false;
    		}
    	}
    }

    void undisplayShipyard() {
        try {
            imageOverlay.remove();
        }
        catch (Exception e) {
            MyLog.d(TAG, "error removing overlay");
        }
        imageOverlay = null;
    }

    void displayShipyard(int s) {
        shipyard = s;
        undisplayShipyard();
        final float transparency = ((float) seekBarSetting)/((float)SEEKBAR_MAX);
    	switch(s) {
    		case 2:
    	        if (useLargeBitmaps) {
    	        	bitmapID = R.drawable.sy2_cropped2048;
    	        }
    	        else {
    	        	bitmapID = R.drawable.sy2_display_1024;
     	        }
	        	 imageBitmap = BitmapDescriptorFactory.fromResource(bitmapID);

				mapView.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap googleMap) {
						GroundOverlayOptions options;
						options = new GroundOverlayOptions()
								.image(imageBitmap)
								.position(ANCHOR, WIDTH)
								.bearing(TILT)
								.transparency(transparency)
								.zIndex(OVERLAY_ZINDEX);

						imageOverlay = googleMap.addGroundOverlay(options);
						if (!isVisible(SHIPYARD2, googleMap)) {
							CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultPosition);
							googleMap.moveCamera(cameraUpdate);
						}
					}
				});
    			break;
    	
    		case 1:
    		       if (useLargeBitmaps) {
    		    	 bitmapID = R.drawable.s1_2048;
     		       }
    		       else {
    		    	   bitmapID = R.drawable.sy1;
    		       }
     		       imageBitmap = BitmapDescriptorFactory.fromResource(bitmapID);
					mapView.getMapAsync(new OnMapReadyCallback() {
						@Override
						public void onMapReady(GoogleMap googleMap) {
							GroundOverlayOptions options;

							options = new GroundOverlayOptions()
									.image(imageBitmap)
									.position(ANCHOR2, WIDTH2)
									.bearing(TILT2)
									.transparency(transparency)
									.zIndex(OVERLAY_ZINDEX);
							imageOverlay = googleMap.addGroundOverlay(options);
							if (!isVisible(SHIPYARD1, googleMap)) {
								CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultPosition);
								googleMap.moveCamera(cameraUpdate);
							}
						}
					});
        	        break;

       		case 3:
  		       if (useLargeBitmaps) {
  		    	 bitmapID = R.drawable.s3_cropped_2048;
   		       }
  		       else {
  	 		     bitmapID = R.drawable.s3_cropped_1024;
  		       }
 	           imageBitmap = BitmapDescriptorFactory.fromResource(bitmapID);
				mapView.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap googleMap) {
						GroundOverlayOptions options;
						options = new GroundOverlayOptions()
								.image(imageBitmap)
								.position(ANCHOR3, WIDTH3)
								.bearing(TILT3)
								.transparency(transparency)
								.zIndex(OVERLAY_ZINDEX);
						imageOverlay = googleMap.addGroundOverlay(options);
						if (!isVisible(SHIPYARD3, googleMap)) {
							CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultPosition);
							googleMap.moveCamera(cameraUpdate);
						}
					}
				});
      	        break;    	        	        
        	        
    		case 4:
 		       if (useLargeBitmaps) {
 		    	 bitmapID = R.drawable.shipyard_4_1024;
  		       }
 		       else {
 	 		     bitmapID = R.drawable.shipyard_4_1024;
 		       }
	           imageBitmap = BitmapDescriptorFactory.fromResource(bitmapID);
				mapView.getMapAsync(new OnMapReadyCallback() {
					@Override
					public void onMapReady(GoogleMap googleMap) {
						GroundOverlayOptions options;
						options = new GroundOverlayOptions()
								.image(imageBitmap)
								.position(ANCHOR4, WIDTH4)
								.bearing(TILT4)
								.transparency(transparency)
								.zIndex(OVERLAY_ZINDEX);
						imageOverlay = googleMap.addGroundOverlay(options);
						if (!isVisible(SHIPYARD4, googleMap)) {
							CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(defaultPosition);
							googleMap.moveCamera(cameraUpdate);
						}
					}
				});
     	        break;    	
    	}
        setSeekbarToDefault();
    }
    
    private boolean isVisible(LatLng point, GoogleMap googleMap) {
        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        if (bounds.contains(point)) {
        	return true;
        }
        else {
        	return false;
        }
    }
    
    private void setSeekbarToDefault() {
    	seekBarSetting = INITIAL_SEEKBAR;
    	seekBar.setProgress(seekBarSetting);
        try {
            imageOverlay.setTransparency(((float) seekBarSetting) / ((float) SEEKBAR_MAX));
        }
        catch (Exception e) {
            MyLog.d(TAG,"exception setting map transparency");
        }
    	updateMarkers(seekBarSetting);
   	
    }
    
    
    void setButtonStates(View checkedButton) {
    	   	
    	RadioButton s1 = (RadioButton) myView.findViewById(R.id.radioShipyard1);
    	RadioButton s2 = (RadioButton) myView.findViewById(R.id.radioShipyard2);
    	RadioButton s3 = (RadioButton) myView.findViewById(R.id.radioShipyard3);
    	RadioButton s4 = (RadioButton) myView.findViewById(R.id.radioShipyard4);
   	
    	switch (checkedButton.getId()) {
    		case R.id.radioShipyard1:
    			s1.setChecked(true);
    			s1 = null;
    			displayShipyard(1);
    			break;

    		case R.id.radioShipyard2:
    			s2.setChecked(true);
    			s2 = null;
    			displayShipyard(2);
    			break;

    		case R.id.radioShipyard3:
    			s3.setChecked(true);
    			s3 = null;
    			displayShipyard(3);
    			break;   			
    			
    		case R.id.radioShipyard4:
    			s4.setChecked(true);
    			s4 = null;
    			displayShipyard(4);
    			break;
    	}
    	
    	if (s1 != null) {
    		s1.setChecked(false);
    	}
    	if (s2 != null) {
    		s2.setChecked(false);
    	}
    	if (s3 != null) {
    		s3.setChecked(false);
    	}
    	if (s4 != null) {
    		s4.setChecked(false);
    	}
   	
    }
    
    void launchFullScreenMap() {
    	try {
			mapView.getMapAsync(new OnMapReadyCallback() {
				@Override
				public void onMapReady(GoogleMap googleMap) {
					Intent i = new Intent(getActivity(), FullScreenMap.class);
					i.putExtra(FullScreenMap.MAP_FILE, bitmapID);
					i.putExtra(FullScreenMap.MAP_CENTER, googleMap.getCameraPosition().target);
					i.putExtra(FullScreenMap.MAP_ZOOM, googleMap.getCameraPosition().zoom);
					i.putExtra(FullScreenMap.OVERLAY_TRANSPARENCY, imageOverlay.getTransparency());
					i.putExtra(FullScreenMap.OVERLAY_WIDTH, imageOverlay.getWidth());
					i.putExtra(FullScreenMap.OVERLAY_LATLNG, imageOverlay.getPosition());
					i.putExtra(FullScreenMap.OVERLAY_TILT, imageOverlay.getBearing());
					i.putExtra(FullScreenMap.SHOW_LOCATION, tracking);
					i.putExtra(FullScreenMap.MAP_BEARING, googleMap.getCameraPosition().bearing);
					getActivity().startActivityForResult(i, FullScreenMap.RETURN_MAP_STATE);
				}
			});
    	}
    	catch (Exception e) {
    		MyLog.d(TAG, "error launching full screen map");
    	}
    }
	private void hintScreen() {
		if (!FULL_SCREEN_SUPPORTED) return;
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		showHint = mPrefs.getBoolean(showHintPref, true);
		if (showHint) {
			LayoutInflater li = LayoutInflater.from(getActivity()) ;
			View view = li.inflate(R.layout.full_screen_hint_screen,null);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			CheckBox cb = (CheckBox) view.findViewById(R.id.chkWelcome);
			cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            		if (((CheckBox) buttonView).isChecked()) {
            			showHint = false;
            		}
            		else {
            			showHint = true;			
            		}
               	
                }
				
			});
			Button b = (Button) view.findViewById(R.id.exitHintButton);
			b.setOnClickListener(thisFragment);
			builder.setTitle(null);
			builder.setView(view);
			hintDialog = builder.show();
	    }			
	}

}
