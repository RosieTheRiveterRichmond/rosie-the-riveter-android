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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.blueverdi.sortablelist.DragSortController;
import com.blueverdi.sortablelist.DragSortListView;


public class TourFragment extends Fragment implements OnClickListener{
	private static final String TAG = "TourFragment";
	private static final String IMAGE_SOURCE = "site_images";
	private static final String MAP_VIEW = "mapView";
	private static final String BIG_ROSIE_MAP = "big_rosie.map";
	public static final String showHintPref = "hintShown";

	private ArrayList<Site> sites;
	private View view;
	private int arrayLayout;
	private boolean portrait;
	private LinearLayout mapLayout;
	private LinearLayout viewContainer;
	private RosieMapView mapView;
   	private Map<String, Site> markers;
   	private static boolean startInMapView;
	
   	private static final double MIN_LAT = 37.7360;
   	private static final double MAX_LAT = 37.9713;
   	private static final double MIN_LONG = -122.5717;
   	private static final double MAX_LONG = -122.2064;
   	
   	private LatLngBounds SITES = new LatLngBounds(
   			  new LatLng(MIN_LAT, MIN_LONG), new LatLng(MAX_LAT, MAX_LONG));
	private boolean networkAvailable;
    private SiteArrayAdapter adapter;
    private DragSortListView dslv;
    private DragSortController controller;

    private int dragStartMode = DragSortController.ON_DOWN;
    private boolean removeEnabled = true;
    private int removeMode = DragSortController.FLING_REMOVE;
    private boolean sortEnabled = true;
    private boolean dragEnabled = true;
    private boolean showHint;
	private AlertDialog hintDialog = null;
	private SharedPreferences mPrefs;     
    private TourFragment thisFragment;;

    MySqliteHelperMyTour helper;

    DriveCalculator calculator;
    
	public static TourFragment newInstance(boolean mapView) {
		startInMapView = mapView;
		return new TourFragment();
	}
	
   	private void setMap(GoogleMap googleMap) {
    	googleMap.clear();
        if (!networkAvailable) {
            thisFragment.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getActivity(), getString(R.string.internet_required), Toast.LENGTH_LONG).show();

                }
            });

    	}

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        initMarkers(googleMap);
		if (helper.getSiteCount() == 0) {
	       	Toast.makeText(getActivity(), getString(R.string.no_sites), Toast.LENGTH_SHORT).show();			
		}
   	}
   	
   	private void setGallery() {
		viewContainer.removeAllViews();
		viewContainer.addView(dslv);
		if (helper.getSiteCount() == 0) {
	       	Toast.makeText(getActivity(), getString(R.string.no_sites), Toast.LENGTH_SHORT).show();			
		}
		else {
			hintScreen();
		}
   	}

   	public boolean getMapView() {
        RadioButton rb = (RadioButton)view.findViewById(R.id.radioMapView);
         return rb.isChecked();	   		
   	}

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        Site item = adapter.getItem(from);
                        adapter.remove(item);
                        adapter.insert(item, to);
                    }
                }
            };

    private DragSortListView.RemoveListener onRemove = 
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    adapter.remove(adapter.getItem(which));
                }
            };
     
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	setRetainInstance(false);
    	thisFragment = this;
    	networkAvailable = Utils.isNetworkAvailable(getActivity());
    	calculator = new DriveCalculator(getActivity());
		buildSitesList();
    	view = inflater.inflate(R.layout.tour_fragment, container, false);
    	viewContainer = (LinearLayout) view.findViewById(R.id.view_container);
    	RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup1);
    	// implementation without nested fragments
    	// first initialize the gallery view
    	// ---------------------------------
    	dslv = (DragSortListView) inflater.inflate(R.layout.tour_site_gallery_view, viewContainer, false);
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.click_remove);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        dslv.setFloatViewManager(controller);
        dslv.setOnTouchListener(controller);
        dslv.setDragEnabled(dragEnabled);
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        switch (rotation) {
        	case Surface.ROTATION_0:
        	case Surface.ROTATION_180:
        		portrait = true;
        		arrayLayout = R.layout.tour_site_portrait;
        		break;
        	case Surface.ROTATION_90:
        	case Surface.ROTATION_270:
        		portrait = false;
        		arrayLayout = R.layout.tour_site_landscape;
        		break;
        }
        dslv.setDropListener(onDrop);
        dslv.setRemoveListener(onRemove);
        adapter = new SiteArrayAdapter(sites);
        dslv.setAdapter(adapter);


        // now initialize the map view 
        // --------------------------
    	mapLayout = (LinearLayout) inflater.inflate(R.layout.site_map_view, viewContainer, false);
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        mapView = (RosieMapView) mapLayout.findViewById(R.id.siteMap);
        mapView.onCreate(savedInstanceState);
		if (startInMapView) {
			viewContainer.removeAllViews();
			viewContainer.addView(mapLayout);
			mapView.getMapAsync(new OnMapReadyCallback() {
				@Override
				public void onMapReady(GoogleMap googleMap) {
					setMap(googleMap);
				}
			});
		}
        mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				googleMap = googleMap;
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);

				//      		map.setMyLocationEnabled(true);
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(SITES, 0);
				if (networkAvailable) {
					googleMap.animateCamera(cameraUpdate);
				} else {
					googleMap.moveCamera(cameraUpdate);
				}


			}
		});
    	try{ 
    		container.removeAllViews();
    	} 
    	catch (Exception e) {
    		MyLog.d(TAG, "container evaporated inside onCreateView");
    		return view;
    	}
        if (startInMapView) {
	         	RadioButton rb = (RadioButton)view.findViewById(R.id.radioMapView);
			rb.setChecked(true);
		}
        else {
        	setGallery();
        }
    	radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    		@Override
    		public void onCheckedChanged(RadioGroup group, int checkedId) {
    			if (checkedId == R.id.radioListView) {
    				setGallery();
    			}
    			else {
	                 Toast.makeText(getActivity(), getString(R.string.getting_map), Toast.LENGTH_LONG).show();
					viewContainer.removeAllViews();
					viewContainer.addView(mapLayout);
					mapView.getMapAsync(new OnMapReadyCallback() {
						@Override
						public void onMapReady(GoogleMap googleMap) {
							setMap(googleMap);
						}
					});
    			}
    		}
    	});
		return view;
	}

   
	 @Override
     public void onSaveInstanceState(Bundle outState){
         super.onSaveInstanceState(outState);
         RadioButton rb = (RadioButton)view.findViewById(R.id.radioMapView);
         outState.putBoolean(MAP_VIEW, rb.isChecked());
	 }
    
	void buildSitesList() {
		helper = new MySqliteHelperMyTour(getActivity());
		sites = helper.getSites();
	}
	
	// code to support gallery view
	// ----------------------------

    public void onClick(View v) {
    	switch(v.getId()) {
	    	case R.id.infoButton:
	    		int position = (Integer) v.getTag();
                Intent i = new Intent(getActivity(), SiteActivity.class);
                i.putExtra(Site.PARCEL_NAME, sites.get(position));
                SiteDetails sd = sites.get(position).getDetails();
                if (sd != null) {
                	i.putExtra(SiteDetails.PARCEL_NAME, sd);
                }
                startActivity(i); 
                getActivity().overridePendingTransition(R.anim.zoom_in, 0);
                break;
/*                
	    	case R.id.directionsButton:
	        	if (Utils.isNetworkAvailable(getActivity())) {
	        		position = (Integer)v.getTag();
	        		Site s = sites.get(position);
	        		if (s.getString(Site.LATITUDE) != null)  {
	        			String url;
	        			  if (position == 0) {
	        				  url = "http://maps.google.com/maps?daddr="+s.getString(Site.LATITUDE)+ ","+s.getString(Site.LONGITUDE);
	        			  }
	        			  else {
	        				  Site start = sites.get(position - 1);
	        				  url = "http://maps.google.com/maps?saddr="+start.getString(Site.LATITUDE)+ ","+start.getString(Site.LONGITUDE) + "?daddr="+s.getString(Site.LATITUDE)+ ","+s.getString(Site.LONGITUDE);		        				  
	        			  }
	        			  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
			        	  intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			        	  try {
			        		  startActivity(intent);
			        		  getActivity().overridePendingTransition(R.anim.zoom_in, 0);
			        	  }
			              catch(ActivityNotFoundException innerEx)
			              {
			                  Toast.makeText(getActivity(), getString(R.string.requires_google_apps), Toast.LENGTH_LONG).show();
			              }	        	  
			          }
		        	}
		        	else {
		            	Toast.makeText(getActivity(), getString(R.string.requires_network), Toast.LENGTH_LONG).show();
		        	}
		          break;
*/
	    	case R.id.exitHintButton:
	    		SharedPreferences.Editor editor = mPrefs.edit();
	    		editor.putBoolean(showHintPref, showHint);
	    		editor.commit(); // Very important to save the preference 
	    		hintDialog.dismiss();  
	    		hintDialog = null;	    		
	    		break;
	    		
    	}
    }
    
    public class SiteArrayAdapter extends ArrayAdapter<Site> {
    	  private final ArrayList<Site> values;

    	  public SiteArrayAdapter(ArrayList<Site> values) {
    	    super(getActivity(), arrayLayout, values);
    	    this.values = values;
    	  }

    	  @Override
    	  public View getView(int position, View convertView, ViewGroup parent) {
    		MyLog.d(TAG, "getView " + Integer.toString(position));
    	    LayoutInflater inflater = (LayoutInflater) getActivity()
    	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	    View rowView = inflater.inflate(arrayLayout, parent, false);
    	    TextView textView = (TextView) rowView.findViewById(R.id.siteName);
    	    ImageView imageView = (ImageView) rowView.findViewById(R.id.siteImage);
    	    textView.setText(values.get(position).getString(Site.NAME));
    	    imageView.setImageBitmap(values.get(position).getBitmapFromAssets(getActivity())); 
    	    TextView dt = (TextView) rowView.findViewById(R.id.drivingTime);
    	    TextView dd = (TextView) rowView.findViewById(R.id.drivingDistance);
    	    if (position == 0) {
    	    	dt.setText("");
    	    	dd.setText("");    	    	
    	    }
    	    else {    	    	
    	    	new DrivingDistanceRunnable(thisFragment, rowView, dd,dt,values.get(position - 1), values.get(position));
    	    }
    	    Button b = (Button) rowView.findViewById(R.id.infoButton);
    	    b.setOnClickListener(thisFragment);
    	    b.setTag(position);
//    	    b = (Button) rowView.findViewById(R.id.directionsButton);
//    	    b.setOnClickListener(thisFragment);
//    	    b.setTag(position);
    	    return rowView;
    	  }
    	}   
    

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override 
    public void onPause() {
    	super.onPause();
    	mapView.onPause();
    } 

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
		updateDataBase();
    	for (Site s : sites) {
    		s.releaseBitmap();
    	}
    	sites.clear();
		sites = null;
		view = null;
		dslv = null;
		mapLayout = null;
		viewContainer = null;
		mapView = null;
// debug!!	   	markers.clear();
	   	markers = null;
		helper.close();
	   	
	    adapter =  null;
	    dslv = null;
	    controller = null;
		hintDialog = null;
		mPrefs = null;     

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MyLog.d(TAG, "onLowMemory");
        mapView.onLowMemory();
    }  

    private void updateDataBase() {
    	helper.deleteAllSites();
    	for (Site s: sites) {
    		helper.addSite(s);
    	}
    }
    
    private void initMarkers(GoogleMap googleMap) {
        markers = new HashMap<String, Site>();
        for (Site s : sites) {
     	   LatLng ll = new LatLng(Double.parseDouble(s.getString(Site.LATITUDE)),Double.parseDouble(s.getString(Site.LONGITUDE)));    	   
     	   Marker marker = googleMap.addMarker(new MarkerOptions()
            .position(ll)
            .title(s.getString(Site.NAME)));
     	   switch (sites.indexOf(s)) {
	     	   case 0:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker1));
	     		   break;
	     	   case 1:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker2));
	     		   break;
	     	   case 2:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker3));
	     		   break;
	     	   case 3:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker4));
	     		   break;
	     	   case 4:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker5));
	     		   break;
	     	   case 5:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker6));
	     		   break;
	     	   case 6:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker7));
	     		   break;
	     	   case 7:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker8));
	     		   break;
	     	   case 8:
	     		   marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker9));
	     		   break;
	     	   default:
	     		   // use standard marker
     	   }
     	   markers.put(marker.getId(), s);    	   
        }
        googleMap.setOnMarkerClickListener(new OnMarkerClickListener (){
     	    @Override
     	    public boolean onMarkerClick(Marker marker) {
     	    	Site s = markers.get(marker.getId());
                Intent i = new Intent(getActivity(), SiteActivity.class);
                i.putExtra(Site.PARCEL_NAME, s);
	             SiteDetails sd = s.getDetails();
	             if (sd != null) {
	            	 i.putExtra(SiteDetails.PARCEL_NAME, sd);
	             }
                 startActivity(i);   		                 	                 
                 getActivity().overridePendingTransition(R.anim.zoom_in, 0);
     	    	return true;
     	    }
     	});	    	
    }
    

	private class DrivingDistanceRunnable implements Runnable {
		TextView distanceView;
		TextView timeView;
		Site lastSite;
		Site thisSite;
		Fragment context;
		View row;
		public DrivingDistanceRunnable(Fragment context, View row, TextView distanceView, TextView timeView, Site start, Site end) {
			this.context = context;
			this.distanceView = distanceView;
			this.timeView = timeView;
			this.lastSite = start;
			this.thisSite = end;
			this.row = row;
			new Thread(this).start();
		}
		public void run() {
			String distance;
			String time;
			try {
		    	DriveCalculator.DrivingInfo di = calculator.getDrivingInfo(Double.parseDouble(lastSite.getString(Site.LONGITUDE)), 
						   Double.parseDouble(lastSite.getString(Site.LATITUDE)), 
						   Double.parseDouble(thisSite.getString(Site.LONGITUDE)), 
						   Double.parseDouble(thisSite.getString(Site.LATITUDE)));
		    	if (di == null) {
		    		distance = getActivity().getString(R.string.driving_information);
		    		time = getActivity().getString(R.string.not_available);
		    		distanceView.setText(getActivity().getString(R.string.driving_information));
		    		timeView.setText(getActivity().getString(R.string.not_available));    	    		
		    	}
		    	else {
		    		StringBuilder sb = new StringBuilder();
		    		sb.append(getActivity().getString(R.string.driving_distance));
		    		sb.append(" ");
		    		sb.append(lastSite.getString(Site.NAME));
		    		sb.append(": ");
		    		sb.append(di.distance);
		    		distance = sb.toString();
		    		sb = new StringBuilder();
		    		sb.append(getActivity().getString(R.string.driving_time));
		    		sb.append(" ");
		    		sb.append(di.drivingTime);
		    		time = sb.toString();
		    	}	
		    	context.getActivity().runOnUiThread(new DrivingDistanceDisplayRunnable(row, distanceView, timeView, distance, time));
			}
			catch (Exception e) {
				// we are running while fragment is shutting down - do nothing
			}
		}
	}
	
	private class DrivingDistanceDisplayRunnable implements Runnable{
		
		TextView distanceView;
		TextView timeView;
		String distanceString;
		String timeString;
		View row;
		
		public DrivingDistanceDisplayRunnable(View row, TextView distanceView, TextView timeView, String distanceString, String timeString) {
			this.distanceView = distanceView;
			this.timeView = timeView;
			this.distanceString = distanceString;
			this.timeString = timeString;
			this.row = row;
		}
		
		public void run() {
			try {
	    		distanceView.setText(distanceString);
	    		timeView.setText(timeString);   
	 //   		row.invalidate();
			}
			catch (Exception e) {
				// we are running while activity is shutting down - do nothing
			}
			
		}
	}

	private void hintScreen() {
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		showHint = mPrefs.getBoolean(showHintPref, true);
		if (showHint) {
			LayoutInflater li = LayoutInflater.from(getActivity()) ;
			View view = li.inflate(R.layout.hint_screen,null);
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
