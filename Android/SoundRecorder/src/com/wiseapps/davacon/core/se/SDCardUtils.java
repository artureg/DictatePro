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

    private static final String PROLECT_FILE_SUFFIX = "_project.wav";

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
        File file = new File(getProjectPath(project.context), PROJECT_NAME);
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
//                            if (parser.getName().equals(TAG_IS_CHANGED)) {
//                                parser.next();
//                                project.isChanged = Boolean.valueOf(parser.getText());
//                                break;
//                            }

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
                    .text(String.valueOf(false))
                    .endTag(NAMESPACE, TAG_IS_CHANGED)
                    .text(NEWLINE);
//            xmlSerializer.startTag(NAMESPACE, TAG_IS_CHANGED)
//                    .text(String.valueOf(project.isChanged))
//                    .endTag(NAMESPACE, TAG_IS_CHANGED)
//                    .text(NEWLINE);

            for (SERecord record : project.getRecords()) {
                serializeRecord(xmlSerializer, record);
            }

            xmlSerializer.endTag(NAMESPACE, TAG_DIST).text(NEWLINE);
            xmlSerializer.endDocument();
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("getSerializer#", e);
        } finally {
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

    public static boolean deleteProject(final SEProject project) {
        boolean projectDeleted = true;

        File projectFile = new File(project.projectPath);
        if (projectFile.exists()) {
            projectDeleted = projectFile.delete();
        }

        boolean recordsDeleted = true;

        File recordFile;
        for (SERecord record : project.getRecords()) {
            recordFile = new File(record.soundPath);
            if (recordFile.exists()) {
                recordsDeleted = recordsDeleted && recordFile.delete();
            }
        }

        return projectDeleted && recordsDeleted;
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
                        record.duration = Long.valueOf(parser.getText());
                        break;
                    }

                    if (parser.getName().equals(TAG_SOUND_PATH)) {
                        parser.next();
                        record.soundPath = parser.getText();
                        break;
                    }

                    if (parser.getName().equals(TAG_START)) {
                        parser.next();
                        record.start = Long.valueOf(parser.getText());
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

        return getRoot(context).getAbsolutePath();
    }

    public static String getPathToSave(Context context) {
        return getProjectPath(context) +
                "/" + System.currentTimeMillis() + PROLECT_FILE_SUFFIX;
    }

    public static String getSoundPath(Context context) {
        if (context == null || context.getApplicationContext() == null) {
            throw new IllegalArgumentException();
        }

        return getRecordsPath(context) + "/" + System.currentTimeMillis();
    }

    private static String getRecordsPath(Context context) {
        if (context == null || context.getApplicationContext() == null) {
            throw new IllegalArgumentException();
        }

        File records = new File(getRoot(context), "Records");
        if (!records.exists()) {
            records.mkdirs();
        }

        return records.getAbsolutePath();
    }

    /**
     * Method to return the root of the tracks hierarchy.
     *
     * @param context Application context
     * @return root of the tracks hierarchy
     */
    public static File getRoot(Context context) {
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
}
