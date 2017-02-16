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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GalleryFragmentBuilder implements FragmentBuilder{

	private GalleryFragment fragment = null;
	private int currentPage = 0;
	
	public void init(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			currentPage = savedInstanceState.getInt("GalleryFragmentBuilder.currentPage", 0);
		}
		return;
	}
	
	public void saveState(Bundle outState) {
		if (fragment != null) {
			try {
				currentPage = fragment.currentPage();
			}
			catch (Exception e) {
				
			}
			fragment = null;
		}
		outState.putInt("GalleryFragmentBuilder.currentPage", currentPage);
		
	}
	
	public Fragment create() {
		fragment = GalleryFragment.newInstance(currentPage);
		return (Fragment) fragment;
		
	}
	
	public void saveState() {
		if (fragment != null) {
			try {
				currentPage = fragment.currentPage();
			}
			catch (Exception e) {
				
			}
			fragment.freeMemory();
			fragment = null;
		}
	}
	
	public String getName(Context context) {
		return context.getString(R.string.photo_gallery);		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	
}
