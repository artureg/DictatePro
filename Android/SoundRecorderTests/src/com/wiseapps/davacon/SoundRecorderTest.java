package com.wiseapps.davacon;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import com.wiseapps.davacon.core.se.*;

import java.io.*;
import java.util.concurrent.Semaphore;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/1/14
 *         Time: 3:53 PM
 */
public class SoundRecorderTest extends ActivityInstrumentationTestCase2 {

    private Semaphore semaphore;

    public SoundRecorderTest() {
        super(SoundRecorderActivity.class);
    }

    public void testPlay() throws Exception {
        SoundRecorderActivity activity = (SoundRecorderActivity) getActivity();
        activity.getEngine().addPlayerStateListener(new SEPlayerStateAdapter() {
            @Override
            public void playingPaused() {
                semaphore.release();
            }
        });

        semaphore = new Semaphore(0);
        activity.play(activity.findViewById(R.id.play));
        semaphore.acquire();

        long currentTime = activity.getEngine().getCurrentTime();
        long duration = activity.getEngine().getDuration();

        assertTrue(currentTime == duration);
    }

    public void testRecord() {
        SoundRecorderActivity activity = (SoundRecorderActivity) getActivity();

        SEProject project = activity.getProject();
        int numRecords = project.getRecords().size();

        activity.record(activity.findViewById(R.id.record));

        assertTrue(project.getRecords().size() - numRecords == 1);

    }

    public void testSaveAll() {
        SoundRecorderActivity activity = (SoundRecorderActivity) getActivity();
        activity.save(activity.findViewById(R.id.save));

        SEProject project = new SEProject(getTargetContext());
        SDCardUtils.readProject(project);

        assertTrue(project.getRecords().size() == 1);
    }

    public void testDelete() {
        SoundRecorderActivity activity = (SoundRecorderActivity) getActivity();
        activity.delete(null);

        SEProject project = new SEProject(getTargetContext());
        SDCardUtils.readProject(project);

        assertTrue(project.getRecords().size() == 0);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        File root = SDCardUtils.getRoot(getTargetContext());
        if (root.listFiles().length == 0) {
            // save project.plist to sdcard
            saveProject(root);

            // save records to sd card
            saveRecords(root);
        }
    }

    private void saveProject(File root) {
        // save project.plist to sdcard
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getContext().getAssets().open("data/project.plist");
            out = new FileOutputStream(new File(root, "project.plist"));

            byte[] data = new byte[1024];
            while (in.read(data) != -1) {
                out.write(data);
            }
        } catch(Exception e) {
            assertNull("setUp failed: project.plist couldn't be saved to sd card!", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void saveRecords(File root) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getContext().getAssets().open("data/Records/1398328501793");
            out = new FileOutputStream(new File(getRecordsPath(root), "1398328501793"));

            byte[] data = new byte[1024];
            while (in.read(data) != -1) {
                out.write(data);
            }
        } catch(Exception e) {
            assertNull("setUp failed: record couldn't be saved to sd card!", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private static String getRecordsPath(File root) {
        File records = new File(root, "Records");
        if (!records.exists()) {
            records.mkdirs();
        }

        return records.getAbsolutePath();
    }

    /**
     * @return context of instrumentation (test runner)
     */
    private Context getContext() {
        return getInstrumentation().getContext();
    }

    /**
     * @return context of the instrumented application
     */
    private Context getTargetContext() {
        return getInstrumentation().getTargetContext();
    }
}
