package com.wiseapps.davacon.core.se;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.wiseapps.davacon.logging.LoggerFactory;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/13/14
 *         Time: 10:17 AM
 */
public class SDCardUtils {
    private static final String TAG = SDCardUtils.class.getSimpleName();

    private static final String APP_PATH = "Android/data/";

    private static final String FILE_NAME = "project.xml"; // FIXME it should be project name

    private static final String NAMESPACE = "";

    private static final String TAG_DIST = "dist";
    private static final String TAG_IS_CHANGED = "isChanged";
    private static final String TAG_RECORDS = "records";
    private static final String TAG_RECORD = "record";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_SOUND_PATH = "soundPath";
    private static final String TAG_START = "start";

    private static final String NEWLINE = "\n";

    /**
     * Save project to xml on sd card
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

        FileOutputStream file_out = null;
        try {
            file_out = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
            return false;
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
                try {
                    file_out.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                return false;
            }
        }

        XmlSerializer xmlSerializer = Xml.newSerializer();
        try {
            xmlSerializer.setOutput(file_out, "UTF-8");
            xmlSerializer.startDocument("UTF-8", true);
            // FIXME Uncomment the following code for production version !!!
            // xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            // start root tag
            xmlSerializer.text(NEWLINE).startTag(NAMESPACE, TAG_DIST).text(NEWLINE);

            // project, value isChanged
            xmlSerializer.startTag(NAMESPACE, TAG_IS_CHANGED)
                    .text(String.valueOf(project.isChanged()))
                    .endTag(NAMESPACE, TAG_IS_CHANGED)
                    .text(NEWLINE);

            for (SERecord record : project.getRecords()) {

                // record
                xmlSerializer.startTag(NAMESPACE, TAG_RECORD)
                        .text(NEWLINE);

                xmlSerializer.startTag(NAMESPACE, TAG_DURATION)
                        .text(String.valueOf(record.duration))
                        .endTag(NAMESPACE, TAG_DURATION)
                        .text(NEWLINE);

                xmlSerializer.startTag(NAMESPACE, TAG_SOUND_PATH)
                        .text(record.soundPath)
                        .endTag(NAMESPACE, TAG_SOUND_PATH)
                        .text(NEWLINE);

                xmlSerializer.startTag(NAMESPACE, TAG_START)
                        .text(String.valueOf(record.start))
                        .endTag(NAMESPACE, TAG_START)
                        .text(NEWLINE);

                // end of record
                xmlSerializer.endTag(NAMESPACE, TAG_RECORD)
                        .text(NEWLINE);

            }

            // end of project
            xmlSerializer.endTag(NAMESPACE, TAG_DIST).text(NEWLINE);

            // end of document
            xmlSerializer.endDocument();

            file_out.flush();
            file_out.close();

//	        FileInputStream fis = new FileInputStream(file);
//			int i = 0;
//			char c;
//			while((i=fis.read())!=-1) {
//				c=(char)i;
//				System.out.print(c);
//			}

        } catch (IllegalArgumentException e) {
            LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
        } catch (IllegalStateException e) {
            LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
        } catch (IOException e) {
            LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
        }
        try {
            file_out.close();
        } catch (IOException e) {
            LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
        }

        return false;
    }

    /**
     * @param context context
     * @param project project to delete
     * @return true if project deleted successfully, false otherwise
     */
    public static boolean deleteProjectFromSDCard(Context context, final SEProject project) {

        if (context == null || project == null) {
            throw new IllegalArgumentException();
        }

        return deleteRootFile(context);

    }

    public static List<SERecord> getRecordsFromSDCard(Context context, SEProject project) {

        File file = getFile(context);
        if (!file.exists() || !file.canWrite()) {
            return null;
        }

        InputStream in;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
            return null;
        }

        XmlPullParser parser;
        try {

            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
            return null;
        }
        List<SERecord> records = null;
        try {

            records = new ArrayList<SERecord>();

            parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_DIST);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                LoggerFactory.obtainLogger(TAG).d(" plist tag = " + name);

                if (name.equals(TAG_RECORD)) {
                    records.add(readRecord(parser, project));
                } else {
                    skip(parser);
                }
            }


        } catch (XmlPullParserException e) {
            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
        }

        return records;
    }

    /**
     * Read record from xml
     *
     * @param parser
     * @param project
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static SERecord readRecord(XmlPullParser parser, SEProject project) throws XmlPullParserException, IOException {

        long duration = 0;
        String soundPath = null;
        long start = 0;

        parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_RECORD);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            LoggerFactory.obtainLogger(TAG).d(" - plist records tag = " + name);

            if (name.equals(TAG_DURATION)) {

                parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_DURATION);
                if (parser.next() == XmlPullParser.TEXT) {
                    duration = Long.parseLong(parser.getText());
                    parser.nextTag();
                }
                parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_DURATION);

            } else if (name.equals(TAG_SOUND_PATH)) {

                parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_SOUND_PATH);
                if (parser.next() == XmlPullParser.TEXT) {
                    soundPath = parser.getText();
                    parser.nextTag();
                }
                parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_SOUND_PATH);

            } else if (name.equals(TAG_START)) {

                parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_START);
                if (parser.next() == XmlPullParser.TEXT) {
                    start = Long.parseLong(parser.getText());
                    parser.nextTag();
                }
                parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_START);

            }

        }

        // TODO read correcpondent sound file to define the record (PCMRecord, SPEEXRecord, etc.)
//        SERecord record = new SERecord(project);
//        record.duration = duration;
//        record.soundPath = soundPath;
//        record.start = start;
//
//        return record;

        return null;
    }

    /**
     * Skip Tags You Don't Care About
     *
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * @param context context
     * @param record  record to save
     * @return true if record saved successfully, false otherwise
     */
    public static boolean saveRecordToSDCard(Context context, SERecord record) {
        // TODO create file on sd card to contain record's contents

        // TODO use saveProjectToSDCard()
        return false;
    }

    /**
     * @param context context
     * @param record  record to delete
     * @return true if record deleted successfully, false otherwise
     */
    public static boolean deleteRecordFromSDCard(Context context, SERecord record) {
        // TODO implement

        // TODO use saveProjectToSDCard()
        return false;
    }

    /**
     * @param context context
     * @param record  record to get the file for
     * @return file in case it exists, null otherwise
     */
    public static File getRecordedFile(Context context, SERecord record) {

//    	List<SERecord> record = getRecordsFromSDCard(context, projectFilename, project)


        // TODO need discuss


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

        File file = new File(Environment.getExternalStorageDirectory(),
                APP_PATH + context.getApplicationContext().getPackageName() + FILE_NAME);
        if (file.exists()) {
            return file.delete();
        }

        return false;

    }
}
