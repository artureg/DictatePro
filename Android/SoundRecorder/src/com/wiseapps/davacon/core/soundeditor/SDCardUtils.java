package com.wiseapps.davacon.core.soundeditor;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.xml.sax.Parser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/13/14
 *         Time: 10:17 AM
 */
public class SDCardUtils {
    private static final String TAG = SDCardUtils.class.getSimpleName();

    private static final String APP_PATH = "Android/data/";
    
    private static final String FILE_NAME = "project.xml";
    
    private static final String NAMESPACE = ""; 
    
    private static final String TAG_DIST = "dist"; 
    private static final String TAG_IS_CHANGED = "isChanged";
    private static final String TAG_RECORDS = "records";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_SOUND_PATH = "soundPath";
    private static final String TAG_START = "start";
    
   /*
    <dict>
	<key>isChanged</key>
	<false/>
	<key>records</key>
	<array>
		<dict>
			<key>duration</key>
			<real>2.2977500000000002</real>
			<key>soundPath</key>
			<string>/Users/timofey/Library/Application Support/iPhone Simulator/7.1-64/Applications/9509A2BC-3152-4DDC-A242-23E06DEE85C6/Documents/Sounds/1396453391.wav</string>
			<key>start</key>
			<real>0.0</real>
		</dict>
		<dict>
			<key>duration</key>
			<real>1.2338270391151309</real>
			<key>soundPath</key>
			<string>/Users/timofey/Library/Application Support/iPhone Simulator/7.1-64/Applications/9509A2BC-3152-4DDC-A242-23E06DEE85C6/Documents/Sounds/1396453391.wav</string>
			<key>start</key>
			<real>1.0639229608848693</real>
		</dict>
		<dict>
			<key>duration</key>
			<real>2.3432499999999998</real>
			<key>soundPath</key>
			<string>/Users/timofey/Library/Application Support/iPhone Simulator/7.1-64/Applications/9509A2BC-3152-4DDC-A242-23E06DEE85C6/Documents/Sounds/1396453403.wav</string>
			<key>start</key>
			<real>0.0</real>
		</dict>
	</array>
</dict>
</plist>
    */

    /**
     *
     * @param context context
     * @param project project to save
     * @return true if project saved successfully, false otherwise
     */
    public static boolean saveProjectToSDCard(Context context, final SEProject project) {
       
    	if (context == null || project == null) {
            throw new IllegalArgumentException();
        }
    	
    	deleteRootFile(context);
    	
    	
    	File file = getFile(context);
    	if( !file.exists() || !file.canWrite()) {
    		return false;
		} 
		
	    FileOutputStream file_out = null;
	    try{
	    	file_out = new FileOutputStream(file);

	    } catch(FileNotFoundException e) {
	    	e.printStackTrace();
	    }
		
    	XmlSerializer xmlSerializer = Xml.newSerializer();
        try {
			xmlSerializer.setOutput(file_out, "UTF-8");
	        xmlSerializer.startDocument("UTF-8", true); 
	        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	        xmlSerializer.startTag("", "file");
	        
	        // start root tag
	        xmlSerializer.startTag(NAMESPACE, TAG_DIST);
	        
	        // project
	        xmlSerializer.startTag(NAMESPACE, TAG_IS_CHANGED);
	        xmlSerializer.text( String.valueOf(project.isChanged()) );
	        
      	    // array of records
	        xmlSerializer.startTag(NAMESPACE, TAG_RECORDS);
	        
	        for(SERecord record : project.getRecords()) {
	
		        xmlSerializer.startTag(NAMESPACE, TAG_DIST);
		        
		        xmlSerializer.startTag(NAMESPACE, TAG_DURATION);
		       
		        xmlSerializer.endTag(NAMESPACE, TAG_DURATION);
		         
		        xmlSerializer.endTag(NAMESPACE, TAG_DIST);
	                
	        }
	        
    	    // end of records
	        xmlSerializer.startTag(NAMESPACE, TAG_RECORDS);
	                
	        // end of project
	        xmlSerializer.endTag(NAMESPACE, TAG_IS_CHANGED);
	        
	        // end of root tag
	        
        
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	
        return false;
    }

    /**
     *
     * @param context context
     * @param project project to delete
     * @return true if project deleted successfully, false otherwise
     */
    public static boolean deleteProjectFromSDCard(Context context, final SEProject project) {
       
    	if ( project != null ) {
    		return deleteRootFile(context);
    		
    	}
    	
        return false;
    }

    public static List<SERecord> getRecordsFromSDCard(Context context, String projectFilename) {
        
//    	File file = getFile(context);
//    	if( !file.exists() || !file.canWrite()) {
//    		return false;
//		} 
//    	
//    	InputStream in;
//		try {
//			in = new FileInputStream(file);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return false;
//		}
//		
//		XmlPullParser parser;
//    	try {
//    		
//            parser = Xml.newPullParser();
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
//            parser.setInput(in, null);
//            parser.nextTag();
//            
//        } catch (Exception e) {
//        	e.printStackTrace();
//        	return false;
//        }
    	
    	
    	
    	
    	
    	
        return null;
    }

    /**
     *
     * @param context context
     * @param record record to save
     * @return true if record saved successfully, false otherwise
     */
    public static boolean saveRecordToSDCard(Context context, SERecord record) {
        // TODO create file on sd card to contain record's contents
        return false;
    }

    /**
     *
     * @param context context
     * @param record record to delete
     * @return true if record deleted successfully, false otherwise
     */
    public static boolean deleteRecordFromSDCard(Context context, SERecord record) {
        // TODO implement
        return false;
    }

    /**
     *
     * @param context context
     * @param record record to get the file for
     * @return file in case it exists, null otherwise
     */
    public static File getRecordedFile(Context context, SERecord record) {
        // TODO implement
        return null;
    }

    /**
     * Method to return the root of the tracks hierarchy.
     *
     * @param context context
     * @return root directory project stuff will be kept in
     */
    private static File getRoot(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }

        File root = new File(Environment.getExternalStorageDirectory(),
                APP_PATH + context.getApplicationContext().getPackageName());

        if (!root.exists()) {
            root.mkdirs();
        }

        return root;
    }
    
    private static File getFile(Context context) {
    	
    	File file = new File(getRoot(context), FILE_NAME);
    	
    	return file;
    	
    }
    
    private static boolean deleteRootFile(Context context) {
    	
    	if (context == null) {
            throw new IllegalArgumentException();
        }
    	
    	File file = new File(Environment.getExternalStorageDirectory(),
                APP_PATH + context.getApplicationContext().getPackageName() + FILE_NAME );
    	if(file.exists()) {
    		return file.delete();
    	}
    	
    	return false;
    	
    }
}
