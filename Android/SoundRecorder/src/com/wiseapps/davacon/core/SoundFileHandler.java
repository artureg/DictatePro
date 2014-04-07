package com.wiseapps.davacon.core;

import android.content.Context;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/30/14
 *         Time: 9:47 AM
 */
public class SoundFileHandler {
    private static final String TAG = SoundFileHandler.class.getSimpleName();

    /**
     * Concatenates the tracks.
     *
     * @param context Context
     * @param wavs objects of {@link com.wiseapps.davacon.core.SoundFile} type to concatenate
     * @param filename name of the resultant file
     * @return the object of {@link com.wiseapps.davacon.core.SoundFile} type with the concatenated contents
     * @throws Exception
     */
    public static SoundFile concat(Context context, List<SoundFile> wavs, String filename) throws Exception {
        SoundFile wav = SoundFile.create(new File(filename));

        for (SoundFile w : wavs) {
            w.read();
            wav.write(w.getData());
        }

        wav.consume();

//        String speexFilename = FileUtils.getSpeexFilename(context, String.valueOf(currentTimeMillis) + ".wav");
//        int result = SpeexWrapper.encode(wav.getFile().getAbsolutePath(),
//                speexFilename);
//        if (result == 0) {
//            File speex = new File(speexFilename);
//            LoggerFactory.obtainLogger(TAG).
//                    d(String.format("concat# size of speex file is %s", speex.length()));
//            LoggerFactory.obtainLogger(TAG).
//                    d(String.format("concat# size of initial file is %s", wav.getFile().length()));
//        }

        return wav;
    }

    /**
     * Splits the track.
     *
     * @param context Context
     * @param wav object of {@link com.wiseapps.davacon.core.SoundFile} to split the contents of
     * @param durationPlayed duration of the first part in millis
     * @throws Exception
     */
    public static void split(Context context, SoundFile wav, int durationPlayed) throws Exception {
        List<byte[]> parts = wav.getDataParts(wav, durationPlayed);

        SoundFile wav1 = SoundFile.create(new File(FileUtils.getFilename(context)));
        wav1.write(parts.get(0));
        wav1.consume();

        SoundFile wav2 = SoundFile.create(new File(FileUtils.getFilename(context)));
        wav2.write(parts.get(1));
        wav2.consume();
    }
}
