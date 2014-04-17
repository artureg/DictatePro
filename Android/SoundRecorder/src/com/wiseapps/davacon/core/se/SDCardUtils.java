package com.wiseapps.davacon.core.se;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import java.io.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.wiseapps.davacon.logging.LoggerFactory;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/13/14
 *         Time: 10:17 AM
 */
public class SDCardUtils {
    private static final String TAG = SDCardUtils.class.getSimpleName();

    private static final String APP_PATH = "/Android/data/";

    private static final String PROJECT_NAME = "project.plist";

    private static final String NAMESPACE = "";

    private static final String TAG_DIST = "dist";
    private static final String TAG_IS_CHANGED = "isChanged";
    private static final String TAG_RECORD = "record";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_SOUND_PATH = "soundPath";
    private static final String TAG_START = "start";

    private static final String NEWLINE = "\n";

//    <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
//    <dist>
//    <isChanged>false</isChanged>
//    <record>
//    <duration>1</duration>
//    <soundPath>/11111111/</soundPath>
//    <start>11</start>
//    </record>
//    <record>
//    <duration>2</duration>
//    <soundPath>/222222222/</soundPath>
//    <start>22</start>
//    </record>
//    </dist>

    public static void readProject(final SEProject project) {
        readProject(project, null);
    }

    public static void readProject(final SEProject project, String filename) {
        File file = new File(getProjectPath(project.context),
                filename != null ? filename : PROJECT_NAME);
        project.projectPath = file.getAbsolutePath();

        if (file.exists()) {
            XmlPullParser parser = getParser(file);
            if (parser == null) {
                throw new IllegalStateException();
            }

            try {
                while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                    switch (parser.getEventType()) {
                        case XmlPullParser.START_TAG: {
                            if (parser.getName().equals(TAG_IS_CHANGED)) {
                                parser.next();
                                project.isChanged = Boolean.valueOf(parser.getText());
                                break;
                            }

                            if (parser.getName().equals(TAG_RECORD)) {
                                SERecord record = parseRecord(project, parser);
                                project.addRecord(record);
                                break;
                            }

                            break;
                        }
                    }

                    parser.next();
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e("readProject#", e);
            }
        }
    }

    public static boolean writeProject(final SEProject project) {
        File file = new File(project.projectPath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e("writeProject#", e);

                return false;
            }
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);

            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setOutput(out, "UTF-8");
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.text(NEWLINE).startTag(NAMESPACE, TAG_DIST).text(NEWLINE);

            xmlSerializer.startTag(NAMESPACE, TAG_IS_CHANGED)
                    .text(String.valueOf(project.isChanged))
                    .endTag(NAMESPACE, TAG_IS_CHANGED)
                    .text(NEWLINE);

            for (SERecord record : project.getRecords()) {
                serializeRecord(xmlSerializer, record);
            }

            xmlSerializer.endTag(NAMESPACE, TAG_DIST).text(NEWLINE);
            xmlSerializer.endDocument();
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("getSerializer#", e);

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    LoggerFactory.obtainLogger(TAG).
                            e("getSerializer#", ioe);
                }
            }
        }

        return true;
    }

    private static void serializeRecord(XmlSerializer serializer, final SERecord record) throws Exception {
        serializer.startTag(NAMESPACE, TAG_RECORD)
                .text(NEWLINE);

        serializer.startTag(NAMESPACE, TAG_DURATION)
                .text(String.valueOf(record.duration))
                .endTag(NAMESPACE, TAG_DURATION)
                .text(NEWLINE);

        serializer.startTag(NAMESPACE, TAG_SOUND_PATH)
                .text(record.soundPath)
                .endTag(NAMESPACE, TAG_SOUND_PATH)
                .text(NEWLINE);

        serializer.startTag(NAMESPACE, TAG_START)
                .text(String.valueOf(record.start))
                .endTag(NAMESPACE, TAG_START)
                .text(NEWLINE);

        serializer.endTag(NAMESPACE, TAG_RECORD)
                .text(NEWLINE);
    }

    private static SERecord parseRecord(SEProject project, XmlPullParser parser) throws Exception {
        SERecord record = new SERecord(project);

        OUTER: while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (parser.getEventType()) {
                case XmlPullParser.START_TAG: {
                    if (parser.getName().equals(TAG_DURATION)) {
                        parser.next();
                        record.duration = Double.valueOf(parser.getText());
                        break;
                    }

                    if (parser.getName().equals(TAG_SOUND_PATH)) {
                        parser.next();
                        record.soundPath = parser.getText();
                        break;
                    }

                    if (parser.getName().equals(TAG_START)) {
                        parser.next();
                        record.start = Double.parseDouble(parser.getText());
                        break;
                    }

                    break;
                }
                case XmlPullParser.END_TAG: {
                    if (parser.getName().equals(TAG_RECORD)) {
                        break OUTER;
                    }
                }
            }

            parser.next();
        }

        return record;
    }

    private static XmlPullParser getParser(File file) {
        XmlPullParser parser = null;

        InputStream in = null;
        try {
            in = new FileInputStream(file);

            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("getParser#", e);

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    LoggerFactory.obtainLogger(TAG).
                            e("getParser#", ioe);
                }
            }
        }

        return parser;
    }

    private static String getProjectPath(Context context) {
        if (context == null || context.getApplicationContext() == null) {
            throw new IllegalArgumentException();
        }

        return Environment.getExternalStorageDirectory() +
                APP_PATH + context.getApplicationContext().getPackageName();
    }

    public static String getSoundPath(Context context) {
        if (context == null || context.getApplicationContext() == null) {
            throw new IllegalArgumentException();
        }

        return Environment.getExternalStorageDirectory() +
                APP_PATH + context.getApplicationContext().getPackageName() + "/Records/" + System.currentTimeMillis();
    }
//
//    /**
//     * Update project to xml on sd card
//     *
//     * @param context context
//     * @param project project to save
//     * @return true if project saved successfully, false otherwise
//     */
//    public static boolean updateProjectToSDCard(Context context, final SEProject project) {
//        LoggerFactory.obtainLogger(TAG).d("updateProjectToSDCard# " + project.projectPath);
//
//        if (context == null || project == null) {
//            throw new IllegalArgumentException();
//        }
//
////        deleteRootFile(context, project.projectPath);
//
//        File file = getFile(context, project.getProjectPath()); // FIXME null
//
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (Exception e) {
//                LoggerFactory.obtainLogger(TAG).e("updateProjectToSDCard# ", e);
//
//                return false;
//            }
//        }
//
//        FileOutputStream file_out = null;
//        try {
//            file_out = new FileOutputStream(file);
//
//        } catch (FileNotFoundException e) {
//            LoggerFactory.obtainLogger(TAG).e("error 222 updateProjectToSDCard()", e);
//            return false;
//        }
//
//        XmlSerializer xmlSerializer = Xml.newSerializer();
//        try {
//            xmlSerializer.setOutput(file_out, "UTF-8");
//            xmlSerializer.startDocument("UTF-8", true);
//            // FIXME Uncomment the following code for production version !!!
//            // xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
//
//            // start root tag
//            xmlSerializer.text(NEWLINE).startTag(NAMESPACE, TAG_DIST).text(NEWLINE);
//
//            // project, value isChanged
//            xmlSerializer.startTag(NAMESPACE, TAG_IS_CHANGED)
//                    .text(String.valueOf(project.isChanged()))
//                    .endTag(NAMESPACE, TAG_IS_CHANGED)
//                    .text(NEWLINE);
//
//            for (SERecord record : project.getRecords()) {
//
//                // record
//                xmlSerializer.startTag(NAMESPACE, TAG_RECORD)
//                        .text(NEWLINE);
//
//                xmlSerializer.startTag(NAMESPACE, TAG_DURATION)
//                        .text(String.valueOf(record.duration))
//                        .endTag(NAMESPACE, TAG_DURATION)
//                        .text(NEWLINE);
//
//                xmlSerializer.startTag(NAMESPACE, TAG_SOUND_PATH)
//                        .text(record.soundPath)
//                        .endTag(NAMESPACE, TAG_SOUND_PATH)
//                        .text(NEWLINE);
//
//                xmlSerializer.startTag(NAMESPACE, TAG_START)
//                        .text(String.valueOf(record.start))
//                        .endTag(NAMESPACE, TAG_START)
//                        .text(NEWLINE);
//
//                // end of record
//                xmlSerializer.endTag(NAMESPACE, TAG_RECORD)
//                        .text(NEWLINE);
//
//            }
//
//            // end of project
//            xmlSerializer.endTag(NAMESPACE, TAG_DIST).text(NEWLINE);
//
//            // end of document
//            xmlSerializer.endDocument();
//
//            file_out.flush();
//            file_out.close();
//
////	        FileInputStream fis = new FileInputStream(file);
////			int i = 0;
////			char c;
////			while((i=fis.read())!=-1) {
////				c=(char)i;
////				System.out.print(c);
////			}
//
//        } catch (IllegalArgumentException e) {
//            LoggerFactory.obtainLogger(TAG).e("error updateProjectToSDCard()", e);
//        } catch (IllegalStateException e) {
//            LoggerFactory.obtainLogger(TAG).e("error updateProjectToSDCard()", e);
//        } catch (IOException e) {
//            LoggerFactory.obtainLogger(TAG).e("error updateProjectToSDCard()", e);
//        }
//        try {
//            file_out.close();
//        } catch (IOException e) {
//            LoggerFactory.obtainLogger(TAG).e("error saveProjectToSDCard()", e);
//        }
//
//        return false;
//    }
//
//    public static boolean getIsChangeFromSDCard(Context context, final String projectPath) {
//
//        LoggerFactory.obtainLogger(TAG).d("getIsChangeFromSDCard()" + projectPath);
//
//        boolean isChange = false;
//
//        XmlPullParser parser = getParser(context, projectPath);
//        if (parser == null) {
//            return isChange;
//        }
//
//        try {
//
//            parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_DIST);
//            while (parser.next() != XmlPullParser.END_TAG) {
//                if (parser.getEventType() != XmlPullParser.START_TAG) {
//                    continue;
//                }
//                String name = parser.getName();
//
//                LoggerFactory.obtainLogger(TAG).d(" getIsChangeFromSDCard = " + name);
//
//                if (name.equals(TAG_IS_CHANGED)) {
//                    isChange = Boolean.valueOf(parser.getText());
//                    return isChange;
//                } else {
//                    skip(parser);
//                }
//            }
//
//
//        } catch (XmlPullParserException e) {
//            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
//        } catch (Exception e) {
//            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
//        }
//        return isChange;
//
//    }
//
//    /**
//     * @param context context
//     * @param project project to delete
//     * @return true if project deleted successfully, false otherwise
//     */
//    public static boolean deleteProjectFromSDCard(Context context, final String projectPath) {
//
//        if (context == null) {
//            throw new IllegalArgumentException();
//        }
//
//        return deleteRootFile(context, projectPath);
//
//    }
//
//    public static List<SERecord> getRecordsFromSDCard(Context context, String projectPath) {
//
//        if (context == null) {
//            throw new IllegalArgumentException();
//        }
//
//        LoggerFactory.obtainLogger(TAG).d("getRecordsFromSDCard()" + projectPath);
//
//        List<SERecord> records = new ArrayList<SERecord>();
//
//        XmlPullParser parser = getParser(context, projectPath);
//        if (parser == null) {
//            return records;
//        }
//
//        try {
//
//            records = new ArrayList<SERecord>();
//
//            parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_DIST);
//            while (parser.next() != XmlPullParser.END_TAG) {
//                if (parser.getEventType() != XmlPullParser.START_TAG) {
//                    continue;
//                }
//                String name = parser.getName();
//
//                LoggerFactory.obtainLogger(TAG).d(" plist tag = " + name);
//
//                if (name.equals(TAG_RECORD)) {
//                    records.add(readRecord(parser));
//                } else {
//                    skip(parser);
//                }
//            }
//
//
//        } catch (XmlPullParserException e) {
//            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
//        } catch (Exception e) {
//            LoggerFactory.obtainLogger(TAG).e("error getRecordsFromSDCard()", e);
//        }
//
//        return records;
//    }
//
//    /**
//     * Read record from xml
//     *
//     * @param parser
//     * @param project
//     * @return
//     * @throws XmlPullParserException
//     * @throws IOException
//     */
//    private static SERecord readRecord(XmlPullParser parser) throws XmlPullParserException, IOException {
//
//        long duration = 0;
//        String soundPath = null;
//        long start = 0;
//
//        parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_RECORD);
//        while (parser.next() != XmlPullParser.END_TAG) {
//            if (parser.getEventType() != XmlPullParser.START_TAG) {
//                continue;
//            }
//            String name = parser.getName();
//
//            LoggerFactory.obtainLogger(TAG).d(" - plist records tag = " + name);
//
//            if (name.equals(TAG_DURATION)) {
//
//                parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_DURATION);
//                if (parser.next() == XmlPullParser.TEXT) {
//                    duration = Long.parseLong(parser.getText());
//                    parser.nextTag();
//                }
//                parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_DURATION);
//
//            } else if (name.equals(TAG_SOUND_PATH)) {
//
//                parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_SOUND_PATH);
//                if (parser.next() == XmlPullParser.TEXT) {
//                    soundPath = parser.getText();
//                    parser.nextTag();
//                }
//                parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_SOUND_PATH);
//
//            } else if (name.equals(TAG_START)) {
//
//                parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_START);
//                if (parser.next() == XmlPullParser.TEXT) {
//                    start = Long.parseLong(parser.getText());
//                    parser.nextTag();
//                }
//                parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_START);
//
//            }
//
//        }
//
//        // TODO read correcpondent sound file to define the record (PCMRecord, SPEEXRecord, etc.)
////        SERecord record = new SERecord(project);
////        record.duration = duration;
////        record.soundPath = soundPath;
////        record.start = start;
////
////        return record;
//
//        return null;
//    }
//
//    /**
//     * Skip Tags You Don't Care About
//     *
//     * @param parser
//     * @throws XmlPullParserException
//     * @throws IOException
//     */
//    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
//        if (parser.getEventType() != XmlPullParser.START_TAG) {
//            throw new IllegalStateException();
//        }
//        int depth = 1;
//        while (depth != 0) {
//            switch (parser.next()) {
//                case XmlPullParser.END_TAG:
//                    depth--;
//                    break;
//                case XmlPullParser.START_TAG:
//                    depth++;
//                    break;
//            }
//        }
//    }
//
////    /**
////     * @param context context
////     * @param record  record to save
////     * @return true if record saved successfully, false otherwise
////     */
////    public static boolean saveRecordToSDCard(Context context, SERecord record) {
////        // TODO create file on sd card to contain record's contents
////
////        // TODO use saveProjectToSDCard()
////        return false;
////    }
////
////    /**
////     * @param context context
////     * @param record  record to delete
////     * @return true if record deleted successfully, false otherwise
////     */
////    public static boolean deleteRecordFromSDCard(Context context, SERecord record) {
////        // TODO implement
////
////        // TODO use saveProjectToSDCard()
////        return false;
////    }
////
////    /**
////     * @param context context
////     * @param record  record to get the file for
////     * @return file in case it exists, null otherwise
////     */
////    public static File getRecordedFile(Context context, SERecord record) {
////
//////    	List<SERecord> record = getRecordsFromSDCard(context, projectFilename, project)
////
////
////        // TODO need discuss
////
////
////        return null;
////    }
//
//    /**
//     * Method to return the root of the tracks hierarchy.
//     *
//     * @param context context
//     * @return root directory project stuff will be kept in
//     */
//    private static File getRoot(Context context) {
//        if (context == null) {
//            throw new IllegalArgumentException();
//        }
//
//        File root = new File(Environment.getExternalStorageDirectory(),
//                APP_PATH + context.getApplicationContext().getPackageName());
//
//        if (!root.exists()) {
//            root.mkdirs();
//        }
//
//        return root;
//    }
//
//    private static File getFile(Context context, String projectPath) {
//
//        File file = new File(projectPath, PROJECT_NAME);
//
//        return file;
//
//    }
//
//    private static boolean deleteRootFile(Context context, String projectPath) {
//        File file;
//
//        if (projectPath != null) {
//            file = new File(projectPath + PROJECT_NAME);
//        } else {
//            file = new File(Environment.getExternalStorageDirectory(),
//                    APP_PATH + context.getApplicationContext().getPackageName() + PROJECT_NAME);
//        }
//
//        if (file.exists()) {
//            return file.delete();
//        }
//
//        return false;
//    }
}
