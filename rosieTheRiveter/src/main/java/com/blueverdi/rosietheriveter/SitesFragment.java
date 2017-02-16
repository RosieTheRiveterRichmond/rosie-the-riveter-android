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

	public class SitesFragment extends Fragment implements OnClickListener {
		private static final String TAG = "SitesFragment";
		private static final String IMAGE_SOURCE = "site_images";
		private static final String MAP_VIEW = "mapView";
		private static final String ROSIE_MAP = "rosie.map";
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
	   	private static boolean startInMapView = false;
		
	   	private static final double MIN_LAT = 37.9041000;
	   	private static final double MAX_LAT = 37.9707000;
	   	private static final double MIN_LONG = -122.3952000;
	   	private static final double MAX_LONG = -122.3290000;
	   	private LatLngBounds SITES = new LatLngBounds(
	   			  new LatLng(MIN_LAT, MIN_LONG), new LatLng(MAX_LAT, MAX_LONG));
		private boolean networkAvailable;
		private SitesFragment thisFragment;
		private MySqliteHelperMyTour myTour;
		public static final SitesFragment newInstance(boolean mapView) {
			startInMapView = mapView;
			return new SitesFragment();
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
	    	thisFragment = this;
	    	setRetainInstance(false);
	    	myTour = new MySqliteHelperMyTour(getActivity());
	    	networkAvailable = Utils.isNetworkAvailable(getActivity());
			buildSitesList();
	    	view = inflater.inflate(R.layout.sites_fragment, container, false);
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
                        Toast.makeText(thisFragment.getActivity(),thisFragment.getActivity().getString(R.string.internet_required),
                                Toast.LENGTH_LONG).show();
					}

					//      map.setMyLocationEnabled(true);
//	        		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(SITES, 0);
//	        		map.animateCamera(cameraUpdate);

					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(SITES, 0);
					if (networkAvailable) {
						map.animateCamera(cameraUpdate);
					} else {
						map.moveCamera(cameraUpdate);

					}
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
			});
			try {
				container.removeAllViews();
			} catch (Exception e) {
				MyLog.d(TAG, "container evaporated inside onCreateView");
				return view;
			}
			if (startInMapView) {
				RadioButton rb = (RadioButton) view.findViewById(R.id.radioMapView);
				rb.setChecked(true);
				setMap();
			} else {
				setGallery();
			}
			radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (checkedId == R.id.radioListView) {
						setGallery();
					} else {
						Toast.makeText(getActivity(), getString(R.string.getting_map), Toast.LENGTH_LONG).show();
						setMap();
					}
				}
			});
			return view;
		}

		void buildSitesList() {
			sites = new ArrayList<Site>();
			Site s = new Site();
			s.setString(Site.NAME, getString(R.string.visitor_center));
			s.setString(Site.ADDRESS1, getString(R.string.visitor_center_address1));
			s.setString(Site.ADDRESS2, getString(R.string.visitor_center_address2));
			s.setString(Site.PHONE, getString(R.string.visitor_center_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.visitor_center_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.visitor_center_description2));
			s.setString(Site.HOURS, getString(R.string.visitor_center_hours));
			s.setString(Site.HOURS2, getString(R.string.visitor_center_hours2));
			s.setString(Site.WEBSITE, getString(R.string.visitor_center_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.visitor_center_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.visitor_center_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "visitor_center.jpg"));
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.red_oak));
			s.setString(Site.ADDRESS1, getString(R.string.red_oak_address1));
			s.setString(Site.ADDRESS2, getString(R.string.red_oak_address2));
			s.setString(Site.PHONE, getString(R.string.red_oak_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.red_oak_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.red_oak_description2));
			s.setString(Site.HOURS, getString(R.string.red_oak_hours));
			s.setString(Site.HOURS2, getString(R.string.red_oak_hours2));
			s.setString(Site.WEBSITE, getString(R.string.red_oak_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.red_oak_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.red_oak_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "RedOakPhoto1.jpg"));
			SiteDetails sd = new SiteDetails(getString(R.string.red_oak), R.string.red_oak_text, R.array.red_oak_captions,
					getString(R.string.red_oak_website), "red_oak");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.monument));
			s.setString(Site.ADDRESS1, getString(R.string.monument_address1));
			s.setString(Site.ADDRESS2, getString(R.string.monument_address2));
			s.setString(Site.PHONE, getString(R.string.monument_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.monument_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.monument_description2));
			s.setString(Site.HOURS, getString(R.string.monument_hours));
			s.setString(Site.HOURS2, null);
			s.setString(Site.WEBSITE, null);
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.monument_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.monument_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "Rosie_the_Riveter_Memorial.jpg"));
			sd = new SiteDetails(getString(R.string.monument), R.string.monument_text, R.array.monument_captions, null, "monument");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.richmond_museum));
			s.setString(Site.ADDRESS1, getString(R.string.museum_address1));
			s.setString(Site.ADDRESS2, getString(R.string.museum_address2));
			s.setString(Site.PHONE, getString(R.string.museum_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.museum_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.museum_description2));
			s.setString(Site.HOURS, getString(R.string.museum_hours));
			s.setString(Site.HOURS2, getString(R.string.museum_hours2));
			s.setString(Site.WEBSITE, getString(R.string.museum_website));
			s.setString(Site.ETC, getString(R.string.time_warning));
			s.setString(Site.LATITUDE, getString(R.string.museum_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.museum_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "richmond_history.jpg"));
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.atchison_village));
			s.setString(Site.ADDRESS1, getString(R.string.atchison_address1));
			s.setString(Site.ADDRESS2, getString(R.string.atchison_address2));
			s.setString(Site.PHONE, getString(R.string.atchison_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.atchison_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.atchison_description2));
			s.setString(Site.HOURS, getString(R.string.atchison_hours));
			s.setString(Site.HOURS2, getString(R.string.atchison_hours2));
			s.setString(Site.WEBSITE, null);
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.atchison_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.atchison_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "atchison_village.png"));
			sd = new SiteDetails(getString(R.string.atchison_village), R.string.atchison_text, R.array.atchison_captions, null, "atchison");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.kaiser_3));
			s.setString(Site.ADDRESS1, getString(R.string.kaiser_3_address1));
			s.setString(Site.ADDRESS2, getString(R.string.kaiser_3_address2));
			s.setString(Site.PHONE, getString(R.string.kaiser_3_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.kaiser_3_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.kaiser_3_description2));
			s.setString(Site.HOURS, getString(R.string.kaiser_3_hours));
			s.setString(Site.HOURS2, getString(R.string.kaiser_3_hours2));
			s.setString(Site.WEBSITE, getString(R.string.kaiser_3_website));
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.kaiser_3_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.kaiser_3_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "kaiser3.png"));
			sd = new SiteDetails(getString(R.string.kaiser_3), R.string.shipyard3_text, R.array.shipyard3_captions, null, "shipyard3");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.ford));
			s.setString(Site.ADDRESS1, getString(R.string.ford_address1));
			s.setString(Site.ADDRESS2, getString(R.string.ford_address2));
			s.setString(Site.PHONE, getString(R.string.ford_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.ford_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.ford_description2));
			s.setString(Site.HOURS, getString(R.string.ford_hours));
			s.setString(Site.HOURS2, getString(R.string.ford_hours2));
			s.setString(Site.WEBSITE, null);
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.ford_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.ford_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "Ford.png"));sd = new SiteDetails(getString(R.string.ford), R.string.ford_plant_text, R.array.ford_plant_captions, null, "ford_plant");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.kaiser_hospital));
			s.setString(Site.ADDRESS1, getString(R.string.kaiser_hospital_address1));
			s.setString(Site.ADDRESS2, getString(R.string.kaiser_hospital_address2));
			s.setString(Site.PHONE, getString(R.string.kaiser_hospital_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.kaiser_hospital_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.kaiser_hospital_description2));
			s.setString(Site.HOURS, getString(R.string.kaiser_hospital_hours));
			s.setString(Site.HOURS2, null);
			s.setString(Site.WEBSITE, null);
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.kaiser_hospital_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.kaiser_hospital_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "1_kaiser_hospital.png"));
			sd = new SiteDetails(getString(R.string.kaiser_hospital), R.string.kaiser_hospital_text, R.array.kaiser_hospital_captions, null, "kaiser_hospital");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.child_center));
			s.setString(Site.ADDRESS1, getString(R.string.child_center_address1));
			s.setString(Site.ADDRESS2, getString(R.string.child_center_address2));
			s.setString(Site.PHONE, getString(R.string.child_center_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.child_center_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.child_center_description2));
			s.setString(Site.HOURS, getString(R.string.child_center_hours));
			s.setString(Site.HOURS2, null);
			s.setString(Site.WEBSITE, null);
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.child_center_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.child_center_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "child_center500.png"));
			sd = new SiteDetails(getString(R.string.child_center), R.string.child_center_text, R.array.child_center_captions, null, "child_center");
			s.setDetails(sd);
			sites.add(s);

			s = new Site();
			s.setString(Site.NAME, getString(R.string.firehouse67a));
			s.setString(Site.ADDRESS1, getString(R.string.firehouse67a_address1));
			s.setString(Site.ADDRESS2, getString(R.string.firehouse67a_address2));
			s.setString(Site.PHONE, getString(R.string.firehouse67a_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.firehouse67a_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.firehouse67a_description2));
			s.setString(Site.HOURS, getString(R.string.firehouse67a_hours));
			s.setString(Site.HOURS2, getString(R.string.firehouse67a_hours2));
			s.setString(Site.WEBSITE, null);
			s.setString(Site.ETC, null);
			s.setString(Site.LATITUDE, getString(R.string.firehouse67a_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.firehouse67a_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "firehouse67a.png"));
			sd = new SiteDetails(getString(R.string.firehouse67a), R.string.firehouse67a_text, R.array.firehouse67a_captions, null, "firehouse67a");
			s.setDetails(sd);
			sites.add(s);


			s = new Site();
			s.setString(Site.NAME, getString(R.string.port_chicago));
			s.setString(Site.ADDRESS1, getString(R.string.port_chicago_address1));
			s.setString(Site.ADDRESS2, getString(R.string.port_chicago_address2));
			s.setString(Site.PHONE, getString(R.string.port_chicago_phone));
			s.setString(Site.DESCRIPTION1, getString(R.string.port_chicago_description1));
			s.setString(Site.DESCRIPTION2, getString(R.string.port_chicago_description2));
			s.setString(Site.HOURS, getString(R.string.port_chicago_hours));
			s.setString(Site.HOURS2, getString(R.string.port_chicago_hours2));
			s.setString(Site.WEBSITE, getString(R.string.port_chicago_website));
			s.setString(Site.ETC, getString(R.string.no_entry));
			s.setString(Site.LATITUDE, getString(R.string.port_chicago_latitude));
			s.setString(Site.LONGITUDE, getString(R.string.port_chicago_longitude));
			s.setString(Site.IMAGE_FILE, FilenameUtils.concat(IMAGE_SOURCE, "port_chicago.png"));
			sd = new SiteDetails(getString(R.string.port_chicago), R.string.port_chicago_text, R.array.port_chicago_captions,
					getString(R.string.port_chicago_website), "port_chicago");
			s.setDetails(sd);
			sites.add(s);

		}

		// code to support gallery view
		// ----------------------------

		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.infoButton:
					Intent i = new Intent(getActivity(), SiteActivity.class);
					Site s = (Site) v.getTag();
					i.putExtra(Site.PARCEL_NAME, s);
					SiteDetails sd = s.getDetails();
					if (sd != null) {
						i.putExtra(SiteDetails.PARCEL_NAME, sd);
					}
					startActivity(i);
					getActivity().overridePendingTransition(R.anim.zoom_in, 0);
					break;
				case R.id.tourButton:
					s = (Site) v.getTag();
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

		// code to support map view
		@Override
		public void onResume() {
			super.onResume();
			mapView.onResume();
			ListView listview = (ListView) listLayout.findViewById(R.id.siteListView);
			SiteArrayAdapter saa = new SiteArrayAdapter(sites);
			listview.setAdapter(saa);
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
			}
			markers = null;
			myTour.close();
		}

		@Override
		public void onPause() {
					super.onPause();
				}

		@Override
		public void onLowMemory() {
			super.onLowMemory();
			mapView.onLowMemory();
		}

		@Override
		public void onDetach() {
			super.onDetach();
			MyLog.d(TAG, "onDetach");
		}


	}
