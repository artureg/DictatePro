package com.wiseapps.davacon;

import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.wiseapps.davacon.ActivityNavigator.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/1/14
 *         Time: 3:53 PM
 */
public class ProcessTrackActivityTest extends ActivityUnitTestCase {

    private static final String REC_WAV = "rec.wav";
    private static final String REC_SPX_WAV = "rec_spx.wav";

    public ProcessTrackActivityTest() {
        super(ProcessTrackActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // /storage/emulated/0
        File file = new File("/sdcard/Android/data/com.wiseapps.davacon/" + REC_WAV);

        OutputStream out = new FileOutputStream(file);

        InputStream in = getInstrumentation().getTargetContext().getAssets().open(REC_WAV);

        byte[] data = new byte[1];
        while(in.read(data) > 0) {
            out.write(data);
        }

        in.close();
        out.close();

        SoundFile sf = SoundFile.create(file);

        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_TRACK, sf.getFile());

        Intent intent = new Intent(getInstrumentation()
                .getTargetContext(), ProcessTrackActivity.class);
        intent.putExtra(BUNDLE, bundle);

        startActivity(intent, null, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        File file = new File("/sdcard/Android/data/com.wiseapps.davacon/" + REC_WAV);
        file.delete();
    }

    //    public void testRecordRecord() {
//        final ProcessTrackActivity activity = getActivity();
//
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                View view = activity.findViewById(R.id.button_record);
//                activity.record(view);
//            }
//        });
//    }

    public void testPlayRecord() {
        final ProcessTrackActivity activity =
                (ProcessTrackActivity) getActivity();

        activity.play(activity.findViewById(R.id.button_record));
    }

    public void testSplitRecord() {
    }

    public void testRecordFormat() {
        final ProcessTrackActivity activity =
                (ProcessTrackActivity) getActivity();

        SoundFile sf = activity.getSoundFile();

    }
}
