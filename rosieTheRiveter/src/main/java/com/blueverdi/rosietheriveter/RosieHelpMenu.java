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
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RosieHelpMenu implements OnClickListener{
	private AlertDialog hintEnableDialog = null;
	private AlertDialog helpDialog = null;
	private Activity activity;
	
	RosieHelpMenu(Activity activity) {
		this.activity = activity;
	}

	public boolean createOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		activity.getMenuInflater().inflate(R.menu.rosie_the_riveter, menu);
		MenuItem lb = menu.findItem(R.id.useLargeBitmaps);
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean checked = mPrefs.getBoolean(ShipyardMapFragment.useLargeBitmapsPref, false);
		lb.setChecked(checked);		
		return true;
	}
	
    @Override
    public void onClick(View v) {
    	switch(v.getId()) {
	    	case R.id.exitHintEnableButton:
	    		hintEnableDialog.dismiss();  
	    		hintEnableDialog = null;	    		
	    		break;
	    		
	    	case R.id.exitHelpButton:
	    		helpDialog.dismiss();  
	    		helpDialog = null;	    			    		
	    		break;
	   	
    	}
    }

    public boolean processMenuItem(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.enableHints:
	    		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
	    		editor.putBoolean(TourFragment.showHintPref, true);
	    		editor.putBoolean(GalleryFragment.showHintPref, true);
	    		editor.putBoolean(ShipyardMapFragment.showHintPref, true);
	    		editor.commit(); // Very important to save the preference 
				LayoutInflater li = LayoutInflater.from(activity) ;
				View view = li.inflate(R.layout.hints_re_enabled_screen,null);
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				Button b = (Button) view.findViewById(R.id.exitHintEnableButton);
				b.setOnClickListener(this);
				builder.setTitle(null);
				builder.setView(view);
				hintEnableDialog = builder.show();	
				return true;
			case R.id.useLargeBitmaps:
				if (item.isChecked()) {
					item.setChecked(false);
		    		editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
		    		editor.putBoolean(ShipyardMapFragment.useLargeBitmapsPref, false);
		    		editor.commit(); // Very important to save the preference 					
				}
				else {				
					item.setChecked(true);
		    		editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
		    		editor.putBoolean(ShipyardMapFragment.useLargeBitmapsPref, true);
		    		editor.commit(); // Very important to save the preference 
				}
				return true;
				
			case R.id.help:
				li = LayoutInflater.from(activity);				
				view = li.inflate(R.layout.help_dialog,null);
				b = (Button) view.findViewById(R.id.exitHelpButton);
				b.setOnClickListener(this);

				builder = new AlertDialog.Builder(activity);
				builder.setTitle(null);
				builder.setView(view);
				helpDialog = builder.show();
				
				return true;
				
		}
		return activity.onOptionsItemSelected(item);   	
    }
    
}
