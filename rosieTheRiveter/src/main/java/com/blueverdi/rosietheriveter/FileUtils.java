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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class FileUtils {
	public static final String TAG = "FileUtils";
	
	public static final  void copyFileFromAssets(String filename, Context context) {
	    AssetManager assetManager = context.getAssets();
	    File sdCard = Environment.getExternalStorageDirectory();

	    InputStream in = null;
	    OutputStream out = null;
	    try {
	        String newFileName = sdCard.getAbsolutePath() + File.separator + filename;
	        File f = new File(newFileName);
	        if (f.exists()) {
	        	return;
	        }
	        in = assetManager.open(filename);
	        //String newFileName = "/data/data/" + this.getPackageName() + "/" + filename;//path for storing internally to data/data
	        out = new FileOutputStream(newFileName);
	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        MyLog.d(TAG, "error copying file " + filename);
	    }

	}
	private FileUtils() {
		
	}
	
	public static final File getSdCardFile(String filename) throws IOException{
		File ret = null;
		String fqfn = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filename;
		ret = new File(fqfn);
		if (!ret.exists()) {
			throw new IOException(filename + " does not exist");
		}
		return ret;
	}
	

	  public static boolean unzip(Context context, String zipFile) { 
		  // assumes xxx.zip unzips into directory xxx
	    try  { 
		  AssetManager assetManager = context.getAssets();
		  File storage = context.getFilesDir();
//		  String zipDir = storage.getAbsolutePath() + File.separator + zipFile.replaceFirst("[.][^.]+$", "");		  
	      File zipDir = new File(storage, zipFile.replaceFirst("[.][^.]+$", ""));
	      if (zipDir.exists()) {
	    	  return true;
	      }
		  zipDir.mkdir();
	      ZipInputStream zin = new ZipInputStream(assetManager.open(zipFile)); 
	      ZipEntry ze = null; 
	      byte[] buffer = new byte[1024];
	      while ((ze = zin.getNextEntry()) != null) { 
	    	  MyLog.d("Decompress", "Unzipping " + ze.getName());  
	          File zipOut = new File(zipDir, ze.getName());
//	          zipOut.createNewFile();
	          FileOutputStream fout = new FileOutputStream(zipOut); 
	          for (int c = zin.read(); c != -1; c = zin.read()) { 
	            fout.write(c); 
	          } 	          
	          zin.closeEntry(); 
	          fout.close();        
	      } 
	      zin.close(); 
	    } 
	    catch(Exception e) { 
	      MyLog.d(TAG,"exception unzipping " + zipFile ); 
	      e.printStackTrace();
	      return false;
	    } 
	    return true;
	    
	  } 
	 
}
