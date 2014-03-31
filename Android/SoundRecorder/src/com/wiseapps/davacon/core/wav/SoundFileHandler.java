package com.wiseapps.davacon.core.wav;

import android.content.Context;
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.File;
import java.util.List;

import static com.wiseapps.davacon.core.wav.WAVFile.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/30/14
 *         Time: 9:47 AM
 */
public class SoundFileHandler {
    private static final String TAG = SoundFileHandler.class.getSimpleName();

    public static SoundFile concat(Context context, List<SoundFile> wavs) throws Exception {
        SoundFile wav = SoundFile.create(new File(FileUtils.getTempFilename(context)));

        for (SoundFile w : wavs) {
            w.read();
            wav.write(w.getData());
        }

        wav.consume();

        return wav;
    }

    public static void split(Context context, SoundFile wav, int duration) throws Exception {
        List<byte[]> parts = wav.getDataParts(wav);

        SoundFile wav1 = SoundFile.create(new File(FileUtils.getFilename(context)));
        wav1.write(parts.get(0));
        wav1.consume();

        SoundFile wav2 = SoundFile.create(new File(FileUtils.getFilename(context)));
        wav2.write(parts.get(1));
        wav2.consume();
    }
}
