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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class SiteActivity extends Activity {
	private static final String TAG = "SiteActivity";
	
	private Site site = null;
	private SiteDetails details = null;
	private Activity context;
	private RosieHelpMenu rosieHelpMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site);
		Utils.forcePhonePortrait(this);
		context = this;
		Bundle b = getIntent().getExtras();
		try {
			site = b.getParcelable(Site.PARCEL_NAME);
		}
		catch (Exception e) {
			MyLog.d(TAG, "exception retrieving site info");
		}
		if (site == null) {
			MyLog.d(TAG, "no site info");
			finish();
			context.overridePendingTransition(0, R.anim.zoom_in);
		}
		try {
			details = b.getParcelable(SiteDetails.PARCEL_NAME);
		}
		catch (Exception e) {
			MyLog.d(TAG, "exception retrieving site info");
		}
		
		setContentView(R.layout.activity_site);
		TextView tv = (TextView)findViewById(R.id.name);
		tv.setText(site.getString(Site.NAME));
		tv = (TextView)findViewById(R.id.address1);
		tv.setText(site.getString(Site.ADDRESS1));
		tv = (TextView)findViewById(R.id.address2);
		tv.setText(site.getString(Site.ADDRESS2));
		tv = (TextView)findViewById(R.id.phone);
		tv.setText(site.getString(Site.PHONE));
		tv = (TextView)findViewById(R.id.description1);
		tv.setText(site.getString(Site.DESCRIPTION1));
		tv = (TextView)findViewById(R.id.description2);
		tv.setText(site.getString(Site.DESCRIPTION2));
		tv = (TextView)findViewById(R.id.hours);
		tv.setText(site.getString(Site.HOURS));
		if (site.getString(Site.HOURS2) != null) {
			tv = (TextView)findViewById(R.id.hours2);
			tv.setText(site.getString(Site.HOURS2));			
		}
		if (site.getString(Site.ETC) != null) {
			tv = (TextView)findViewById(R.id.warning);
			tv.setText(site.getString(Site.ETC));
			
		}

		if (details != null) {
			LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
//			LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			Button db = new Button(this);
			db.setText(getString(R.string.title_activity_site_details));
			db.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT,1));
			db.setBackgroundColor(Color.WHITE);
			db.setTextAppearance(this,android.R.style.TextAppearance_Small);
//			db.setTextAppearance(this, resid)
			db.setOnClickListener(new OnClickListener()
			{
			     @Override
			     public void onClick(View v) {
			    	 Intent i = new Intent(context, SiteDetailsActivity.class);
		             i.putExtra(SiteDetails.PARCEL_NAME, details);
		             startActivity(i); 
		             context.overridePendingTransition(R.anim.zoom_in, 0);			    	 
			     }
			});		
			buttons.addView(db);			
		}
		else {
			if (site.getString(Site.WEBSITE) != null) {
				LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
	//			LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
				Button wb = new Button(this);
				wb.setText(getString(R.string.website));
				wb.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT,1));
				wb.setBackgroundColor(Color.WHITE);
				wb.setTextAppearance(this,android.R.style.TextAppearance_Small);
				wb.setOnClickListener(new OnClickListener()
				{
				     @Override
				     public void onClick(View v) {
				          if (site.getString(Site.WEBSITE) != null)  {
				        	  if (Utils.isNetworkAvailable(context)) {
					        	  String url = site.getString(Site.WEBSITE);
					        	  Intent i = new Intent(Intent.ACTION_VIEW);
					        	  i.setData(Uri.parse(url));
					        	  startActivity(i);	
					        	  context.overridePendingTransition(R.anim.zoom_in, 0);
					           }				        	  
				          }
				          else {
				            	Toast.makeText(context, getString(R.string.requires_network), Toast.LENGTH_LONG).show();
				          }
				     }
				});		
				buttons.addView(wb);
			}
		}
		if (site.getString(Site.IMAGE_FILE) != null) {
			ImageView iv = (ImageView) findViewById(R.id.siteImageView);
			iv.setImageBitmap(site.getBitmapFromAssets(this));
//			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		}
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
	
	public void doClick(View v) {
	     switch(v.getId()) {
	        case R.id.diectionsButton:
	        	if (Utils.isNetworkAvailable(this)) {
		          if (site.getString(Site.LATITUDE) != null)  {
		        	  String url = "http://maps.google.com/maps?daddr="+site.getString(Site.LATITUDE)+ ","+site.getString(Site.LONGITUDE);
		        	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		        	  intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		        	  try {
		        		  startActivity(intent);
		        		  context.overridePendingTransition(R.anim.zoom_in, 0);
		        	  }
		              catch(ActivityNotFoundException innerEx)
		              {
		                  Toast.makeText(this, getString(R.string.requires_google_apps), Toast.LENGTH_LONG).show();
		              }	        	  
		          }
	        	}
	        	else {
	            	Toast.makeText(this, getString(R.string.requires_network), Toast.LENGTH_LONG).show();
	        	}
	          break;

	      }
		
	}
	
	@Override
	public void onBackPressed()
	{
	    finish();  
        overridePendingTransition(0,R.anim.zoom_out);
	}
	
}
