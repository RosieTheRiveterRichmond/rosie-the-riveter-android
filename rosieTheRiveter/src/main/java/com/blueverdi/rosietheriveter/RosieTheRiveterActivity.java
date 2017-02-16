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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class RosieTheRiveterActivity extends Activity {

	private static final String TAG = "RosieTheRiveterActivity";
	private static final String SELECTED_TAB = "selectedTab";
	private static final String SITE_MAP_VIEW = "siteMapView";
	private static final String GALLERY_PAGE = "galleryPage";
	
	private TabManager tabManager;

    private RosieHelpMenu rosieHelpMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
//		android.os.Debug.waitForDebugger();	 	// debug!!	
		super.onCreate(savedInstanceState);
//		super.onCreate(null);
		setContentView(R.layout.activity_rosie_the_riveter);
		tabManager = new TabManager(this, savedInstanceState);
		ActionBar actionbar = getActionBar();
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getActionBar().removeAllTabs();
//        LinearLayout container = (LinearLayout) findViewById(R.id.fragment_container);
        // create new tabs and set up the titles of the tabs
		tabManager.addTab(new SitesFragmentBuilder());
		tabManager.addTab(new ShipyardMapFragmentBuilder());
		tabManager.addTab(new GalleryFragmentBuilder());
		tabManager.addTab(new MoreFragmentBuilder());
		tabManager.addTab(new TourFragmentBuilder());
		tabManager.addTab(new AboutFragmentBuilder());
        if (savedInstanceState != null)
        {
        	tabManager.startEngine(savedInstanceState.getInt(SELECTED_TAB,0));        	
        }
        else {
        	tabManager.startEngine(0);
        }

        rosieHelpMenu = new RosieHelpMenu(this);
        
	}

	 @Override
     public void onSaveInstanceState(Bundle outState){		 
         outState.putInt(SELECTED_TAB, getActionBar().getSelectedTab().getPosition());
         tabManager.terminate(outState);
         super.onSaveInstanceState(outState);
	 }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return rosieHelpMenu.createOptionsMenu(menu);
	}

	@Override
	public void onPause() {
		super.onPause();
		tabManager.resetBackPressed();
	}

	@Override
	public void onResume() {
		super.onResume();
		tabManager.onResume();
		tabManager.resetBackPressed();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		tabManager.resetBackPressed();
		return rosieHelpMenu.processMenuItem(item);
	}
	
	private class TabManager implements ActionBar.TabListener {
		private Context context;
		private List<FragmentBuilder> builders = new ArrayList<FragmentBuilder>();
		private int currentTab = -1;
		private Fragment currentFragment = null;
		private boolean strikeOne = false;
		private Stack backStack = new Stack<Integer>();
		private boolean backPressed = false;
		private boolean terminateCalled = false;
		
		Bundle savedInstanceState;
        public TabManager(Context context, Bundle savedInstanceState) {
                    this.context = context;
                    this.savedInstanceState = savedInstanceState;
        }
 
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        	for (FragmentBuilder fb : builders) {
        		fb.onActivityResult(requestCode, resultCode, data);
        	}
         }
        public void startEngine(int initialTab) {
        	currentTab = initialTab;
        	if (initialTab == 0) {
               	currentFragment = builders.get(currentTab).create();
               	FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.fragment_container, currentFragment);
               	ft.commit();       		
        	}
        	else {
        		getActionBar().selectTab(getActionBar().getTabAt(currentTab)); 
        	}
       	
        }
        public void addTab(FragmentBuilder builder) {       	
            ActionBar.Tab tab = getActionBar().newTab().setText(builder.getName(context));
            tab.setTabListener(this);
            getActionBar().addTab(tab);
            builders.add(builder);
            builder.init(savedInstanceState);
        }


        public void terminate(Bundle outState) {
        	for (FragmentBuilder fb : builders) {
        		fb.saveState(outState);
        	}
        	if (currentFragment != null) {
//               	currentFragment = builders.get(currentTab).create();
               	FragmentTransaction ft = getFragmentManager().beginTransaction().remove(currentFragment);
               	ft.commitAllowingStateLoss();       		
        	}
        	terminateCalled = true;
        }

        public void onResume() {
        	// onSaveInstanceState is called when the app is paused but we do not necessarily terminate. We
        	// disconnect the current fragment at that time so we need to restore.
        	if (terminateCalled) {
        		currentFragment = builders.get(currentTab).create();
              	FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.fragment_container,currentFragment);
              	ft.commit();
              	terminateCalled = false;
        	}
        }
        
        public void onBackPressed() {
        	if (strikeOne) {
        		finish();
        	}
        	
        	if (backStack.empty()) {
               	Toast.makeText(context, getString(R.string.	press_back), Toast.LENGTH_SHORT).show();
        		strikeOne = true;
        	}
        	else {
        		backPressed = true;
        		Integer tab = (Integer) backStack.pop();
              	getActionBar().selectTab(getActionBar().getTabAt(tab));              	   		
        		
        	}
        }
        
        public void resetBackPressed() {
        	strikeOne = false;
        }
        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        	MyLog.d(TAG, "onTabReselected");

        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	if (currentTab == -1) {
        		return;
         	}
        	if (!backPressed) {
	        	backStack.push((Integer.valueOf(currentTab)));
	        	strikeOne = false;
        	}
        	else {
        		backPressed = false;
        	}
       		builders.get(currentTab).saveState();
       		int oldTab = currentTab;
        	currentTab = tab.getPosition();
        	currentFragment = builders.get(currentTab).create();
        	if (currentTab > oldTab) {
        		ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);       		
        	}
        	else {
           		ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right);       		           	         		
        	}
       	ft.replace(R.id.fragment_container, currentFragment);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        	if (currentTab == -1) {
        		return;
         	}
        	if (currentFragment != null)
        		builders.get(currentTab).saveState();
	        	currentFragment = null;
        	}
	}

	@Override
	public void onBackPressed() {
		tabManager.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		tabManager.onActivityResult(requestCode, resultCode, data);
	}
    
}	



