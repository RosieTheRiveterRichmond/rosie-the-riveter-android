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

import org.apache.commons.io.FilenameUtils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

	public class MoreFragment extends Fragment implements OnClickListener{
		private static final String TAG = "SitesFragment";
		private static final String IMAGE_SOURCE = "site_images";
		private static final String MAP_VIEW = "mapView";
		private static final String BIG_ROSIE_MAP = "big_rosie.map";
		private ArrayList<Site> sites;
		private View view;
		private int arrayLayout;
		private boolean portrait;
		private LinearLayout listLayout;
		private LinearLayout mapLayout;
		private LinearLayout viewContainer;
		private MapView mapView;
		private GoogleMap map;
	   	private Map<String, Site> markers;
	   	private static boolean startInMapView;
		private MySqliteHelperMyTour myTour;
		
	   	private static final double MIN_LAT = 37.7360;
	   	private static final double MAX_LAT = 37.9713;
	   	private static final double MIN_LONG = -122.5717;
	   	private static final double MAX_LONG = -122.2064;
	   	private LatLngBounds SITES = new LatLngBounds(
	   			  new LatLng(MIN_LAT, MIN_LONG), new LatLng(MAX_LAT, MAX_LONG));
		private boolean networkAvailable;

		private MoreFragment thisFragment;
		
		public static MoreFragment newInstance(boolean mapView) {
			startInMapView = mapView;
			return new MoreFragment();
		}
		
	   	private void setMap() {
			 viewContainer.removeAllViews();
			 viewContainer.addView(mapLayout);	   		
	   	}
	   	
	   	private void setGallery() {
			viewContainer.removeAllViews();
			viewContainer.addView(listLayout);	   		
	   	}

	   	public boolean getMapView() {
	         RadioButton rb = (RadioButton)view.findViewById(R.id.radioMapView);
	         return rb.isChecked();	   		
	   	}
	   	
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	    	setRetainInstance(false);
	    	thisFragment = this;
	    	myTour = new MySqliteHelperMyTour(getActivity());
	    	networkAvailable = Utils.isNetworkAvailable(getActivity());
			buildSitesList();
	    	view = inflater.inflate(R.layout.more_fragment, container, false);
	    	viewContainer = (LinearLayout) view.findViewById(R.id.view_container);
	    	RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup1);
	    	// implementation without nested fragments
	    	// first initialize the gallery view
	    	// ---------------------------------
	    	listLayout = (LinearLayout) inflater.inflate(R.layout.site_gallery_view, viewContainer, false);
	        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	        int rotation = display.getRotation();
	        switch (rotation) {
	        	case Surface.ROTATION_0:
	        	case Surface.ROTATION_180:
	        		portrait = true;
	        		arrayLayout = R.layout.site_portrait;
	        		break;
	        	case Surface.ROTATION_90:
	        	case Surface.ROTATION_270:
	        		portrait = false;
	        		arrayLayout = R.layout.site_landscape;
	        		break;
	        }
	        ListView listview = (ListView) listLayout.findViewById(R.id.siteListView);      

	        // now initialize the map view 
	        // --------------------------
	    	mapLayout = (LinearLayout) inflater.inflate(R.layout.site_map_view, viewContainer, false);
	        try {
	            MapsInitializer.initialize(this.getActivity());
	        } catch (Exception e) {
	            e.printStackTrace();
	            
	        }
	        mapView = (MapView) mapLayout.findViewById(R.id.siteMap);
	        mapView.onCreate(savedInstanceState);
	        mapView.getMapAsync(new OnMapReadyCallback() {
				@Override
				public void onMapReady(GoogleMap googleMap) {
					map = googleMap;
					map.getUiSettings().setMyLocationButtonEnabled(false);
					if (!networkAvailable) {
                        thisFragment.getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.internet_required), Toast.LENGTH_LONG).show();

                            }
                        });
                    }

					map.setOnCameraChangeListener(new OnCameraChangeListener() {

						@Override
						public void onCameraChange(CameraPosition arg0) {
							map.setOnCameraChangeListener(null);
							CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(SITES, 0);
							if (networkAvailable) {
								map.animateCamera(cameraUpdate);
							} else {
								map.moveCamera(cameraUpdate);
							}
						}
					});
					markers = new HashMap<String, Site>();
					for (Site s : sites) {
						LatLng ll = new LatLng(Double.parseDouble(s.getString(Site.LATITUDE)), Double.parseDouble(s.getString(Site.LONGITUDE)));
						Marker marker = map.addMarker(new MarkerOptions()
								.position(ll)
								.title(s.getString(Site.NAME)));
						markers.put(marker.getId(), s);
					}
					map.setOnMarkerClickListener(new OnMarkerClickListener() {
						@Override
						public boolean onMarkerClick(Marker marker) {
							Site s = markers.get(marker.getId());
							Intent i = new Intent(getActivity(), SiteActivity.class);
							i.putExtra(Site.PARCEL_NAME, s);
							startActivity(i);
							getActivity().overridePendingTransition(R.anim.zoom_in, 0);
							return true;
						}
					});
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
    			setMap();	        	
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
		                 setMap();
	    			}
	    		}
	    	});
			return view;
		}

		 @Override
	     public void onSaveInstanceState(Bundle outState){
	         super.onSaveInstanceState(outState);
	         boolean checked = false;
	         try {
		         RadioButton rb = (RadioButton)view.findViewById(R.id.radioMapView);
		         checked =  rb.isChecked();
	         }
	         catch (Exception e) {
	        	 MyLog.d(TAG, "onSaveInstanceState did not find radio button");
	         }
	         outState.putBoolean(MAP_VIEW,checked);
		 }
	    
		void buildSitesList() {
			sites = new ArrayList<Site>();
			
			Site s = new Site();
			s.setString(Site.NAME, getString(R.string.jeremiah_obrien));
			s.setString(Site.ADDRESS1, getString(R.string.jeremiah_address1));
			s.setString(Site.ADDRESS2, getString(R.string.jeremiah_address2));
			s.setString(Site.PHONE, getString(R.string.jeremiah_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.jeremiah_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.jeremiah_description2));
			s.setString(Site.HOURS, getString(R.string.jeremiah_hours));
			s.setString(Site.HOURS2, getString(R.string.jeremiah_hours2));
			s.setString(Site.WEBSITE, getString(R.string.jeremiah_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.jeremiah_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.jeremiah_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"obrien.png"));
			sites.add(s);
			
			s = new Site();
			s.setString(Site.NAME, getString(R.string.hornet));
			s.setString(Site.ADDRESS1, getString(R.string.hornet_address1));
			s.setString(Site.ADDRESS2, getString(R.string.hornet_address2));
			s.setString(Site.PHONE, getString(R.string.hornet_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.hornet_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.hornet_description2));
			s.setString(Site.HOURS, getString(R.string.hornet_hours));
			s.setString(Site.HOURS2, getString(R.string.hornet_hours2));
			s.setString(Site.WEBSITE, getString(R.string.hornet_website));
//			s.setString(Site.WEBSITE, "http://www.uss-hornet.org");
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.hornet_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.hornet_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"hornet.png"));
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.battery_townsley));
			s.setString(Site.ADDRESS1, getString(R.string.townsley_address1));
			s.setString(Site.ADDRESS2, getString(R.string.townsley_address2));
			s.setString(Site.PHONE, getString(R.string.townsley_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.townsley_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.townsley_description2));
			s.setString(Site.HOURS, getString(R.string.townsley_hours));
			s.setString(Site.HOURS2, getString(R.string.townsley_hours2));
			s.setString(Site.WEBSITE, getString(R.string.townsley_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.townsley_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.townsley_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"Battery Townsley.png"));
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.presidio_officers_club));
			s.setString(Site.ADDRESS1, getString(R.string.poc_address1));
			s.setString(Site.ADDRESS2, getString(R.string.poc_address2));
			s.setString(Site.PHONE, getString(R.string.poc_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.poc_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.poc_description2));
			s.setString(Site.HOURS, getString(R.string.poc_hours));
			s.setString(Site.HOURS2, getString(R.string.poc_hours2));
			s.setString(Site.WEBSITE, getString(R.string.poc_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.poc_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.poc_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"poc.png"));
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.bay_model));
			s.setString(Site.ADDRESS1, getString(R.string.bay_model_address1));
			s.setString(Site.ADDRESS2, getString(R.string.bay_model_address2));
			s.setString(Site.PHONE, getString(R.string.bay_model_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.bay_model_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.bay_model_description2));
			s.setString(Site.HOURS, getString(R.string.bay_model_hours));
			s.setString(Site.HOURS2, getString(R.string.bay_model_hours2));
			s.setString(Site.WEBSITE, getString(R.string.bay_model_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.bay_model_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.bay_model_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"bay model.png"));
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.uss_sf));
			s.setString(Site.ADDRESS1, getString(R.string.uss_sf_address1));
			s.setString(Site.ADDRESS2, getString(R.string.uss_sf_address2));
			s.setString(Site.PHONE, getString(R.string.uss_sf_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.uss_sf_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.uss_sf_description2));
			s.setString(Site.HOURS, getString(R.string.uss_sf_hours));
			s.setString(Site.HOURS2, getString(R.string.uss_sf_hours2));
			s.setString(Site.WEBSITE, getString(R.string.uss_sf_website));
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.uss_sf_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.uss_sf_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"uss sf.png"));
			sites.add(s);			

			s = new Site();
			s.setString(Site.NAME, getString(R.string.oakland_museum));
			s.setString(Site.ADDRESS1, getString(R.string.oakland_museum_address1));
			s.setString(Site.ADDRESS2, getString(R.string.oakland_museum_address2));
			s.setString(Site.PHONE, getString(R.string.oakland_museum_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.oakland_museum_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.oakland_museum_description2));
			s.setString(Site.HOURS, getString(R.string.oakland_museum_hours));
			s.setString(Site.HOURS2, getString(R.string.oakland_museum_hours2));
			s.setString(Site.WEBSITE, getString(R.string.oakland_museum_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.oakland_museum_latitude));		
			s.setString(Site.LONGITUDE, getString(R.string.oakland_museum_longitude));		
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE,"Oakland_Museum_cropped.png"));
			sites.add(s);			
			
		}
		
		// code to support gallery view
		// ----------------------------

	    public void onClick(View v) {
	    	switch(v.getId()) {
		    	case R.id.infoButton:
	                Intent i = new Intent(getActivity(), SiteActivity.class);
	                i.putExtra(Site.PARCEL_NAME, (Site)v.getTag());
	                startActivity(i); 
	                getActivity().overridePendingTransition(R.anim.zoom_in, 0);
	                break;
		    	case R.id.tourButton:
		    		Site s = (Site) v.getTag(); 
		    		if (myTour.getSiteByName(s.getString(Site.NAME)) == null) {
		    			myTour.addSite(s);
		    		}
		        	Toast.makeText(getActivity(), getString(R.string.added_to_tour), Toast.LENGTH_SHORT).show();		    				    				    		
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
	    	    LayoutInflater inflater = (LayoutInflater) getActivity()
	    	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	    View rowView = inflater.inflate(arrayLayout, parent, false);
	    	    TextView textView = (TextView) rowView.findViewById(R.id.siteName);
	    	    ImageView imageView = (ImageView) rowView.findViewById(R.id.siteImage);
	    	    textView.setText(values.get(position).getString(Site.NAME));
	    	    imageView.setImageBitmap(values.get(position).getBitmapFromAssets(getActivity())); 
	    	    Button b = (Button) rowView.findViewById(R.id.infoButton);
	    	    b.setOnClickListener(thisFragment);
	    	    b.setTag(values.get(position));
	    	    b = (Button) rowView.findViewById(R.id.tourButton);
	    	    b.setOnClickListener(thisFragment);
	    	    b.setTag(values.get(position));
	    	    return rowView;
	    	  }
	    	}   
	    
	    

	    @Override
	    public void onResume() {
	        super.onResume();
	        mapView.onResume();
	        ListView listview = (ListView) listLayout.findViewById(R.id.siteListView);      
	        SiteArrayAdapter saa = new SiteArrayAdapter(sites);
	        listview.setAdapter(saa);
	    }

	    @Override 
	    public void onPause() {
	    	super.onPause();
	    	mapView.onPause();
//	    	for (Site s : sites) {
//	    		s.releaseBitmap();
//	    	}
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        mapView.onDestroy();
	    	for (Site s : sites) {
	    		s.releaseBitmap();
	    	}
	    	sites.clear();
			sites = null;
			view = null;
			listLayout = null;
			mapLayout = null;
			viewContainer = null;
			mapView = null;
			map = null;
			if (markers != null) {
				markers.clear();
				markers = null;
			}
	    }

	    @Override
	    public void onLowMemory() {
	        super.onLowMemory();
	        MyLog.d(TAG, "onLowMemory");
	        mapView.onLowMemory();
	    }  	   	    
}
