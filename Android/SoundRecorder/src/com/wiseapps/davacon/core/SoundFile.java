package com.wiseapps.davacon.core;

import com.wiseapps.davacon.core.wav.WAVFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
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

    public abstract void read();

    public abstract void write(byte[] data) throws IOException;

    public abstract void consume() throws IOException;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public abstract byte[] getData();

   public abstract List<byte[]> getDataParts(SoundFile wav);

    public interface Factory {
        public SoundFile create(File file) throws IOException;
        public String[] getSupportedExtensions();
    }
}
