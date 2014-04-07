package com.wiseapps.davacon.core;

import com.wiseapps.davacon.core.wav.WAVFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class representing an abstract file that one can read and write.
 *
 * <p>Each concrete file type (say .wav) should overwrite it.</p>
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/30/14
 *         Time: 11:47 AM
 */
public abstract class SoundFile {

    static Factory[] sSubclassFactories = new Factory[] {
            WAVFile.getFactory()
    };

    static ArrayList<String> sSupportedExtensions = new ArrayList<String>();
    static HashMap<String, Factory> sExtensionMap =
            new HashMap<String, Factory>();

    static {
        for (Factory f : sSubclassFactories) {
            for (String extension : f.getSupportedExtensions()) {
                sSupportedExtensions.add(extension);
                sExtensionMap.put(extension, f);
            }
        }
    }

    private File file;

    /**
     * Method to create an object of some derived from the {@link SoundFile} type.
     *
     * @param file File from sd-card to create an object of some derived from the {@link SoundFile} type from.
     * @return object of the {@link SoundFile}
     * @throws IOException
     */
    public static SoundFile create(File file) throws IOException {
        String name = file.getName().toLowerCase();
        String[] components = name.split("\\.");
        if (components.length < 2) {
            return null;
        }

        Factory factory = sExtensionMap.get(components[components.length - 1]);
        if (factory == null) {
            return null;
        }

        return factory.create(file);
    }

    /**
     * Method to read an object derived from the {@link SoundFile} type.
     */
    public abstract void read();

    /**
     * Method to write an object derived from the {@link SoundFile} type.
     *
     * @param data array of bytes to write to file
     * @throws IOException
     */
    public abstract void write(byte[] data) throws IOException;

    /**
     * Method to consume an object derived from the {@link SoundFile} type.
     */
    public abstract void consume() throws IOException;

    /**
     * Method to return duration.
     *
     * @return duration in millis
     */
    public abstract int getDuration();

    /**
     * Method to return file from sd-card.
     *
     * @return file from sd-card
     */
    public File getFile() {
        return file;
    }

    /**
     * Method to set file from sd-card.
     *
     * @param file file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns file data.
     *
     * @return file data
     */
    public abstract byte[] getData();

    /**
     * Checks format of the {@link com.wiseapps.davacon.core.SoundFile SoundFile}.
     *
     * @return true if sound file is of correct format
     */
    public abstract boolean isFormatCorrect();

    /**
     * Returns arrays of bytes of both splitted parts.
     *
     * @param wav {@link com.wiseapps.davacon.core.SoundFile SoundFile} which data to split
     * @param durationPlayed duration of the first part in millis
     * @return arrays of bytes as of both splitted parts
     */
    public abstract List<byte[]> getDataParts(SoundFile wav, int durationPlayed);

    /**
     * Helper interface to handle objects of the derived from {@link com.wiseapps.davacon.core.SoundFile} types.
     */
    public interface Factory {
        public SoundFile create(File file) throws IOException;
        public String[] getSupportedExtensions();
    }
}
