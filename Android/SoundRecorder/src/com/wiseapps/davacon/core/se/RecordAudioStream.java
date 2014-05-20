package com.wiseapps.davacon.core.se;

import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;

import java.io.*;

import static com.wiseapps.davacon.core.se.SEProjectEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 9:23 PM
 */
public class RecordAudioStream extends AudioStream {
    private static final String TAG = RecordAudioStream.class.getSimpleName();

    final SERecord record;

    private OutputStream mockOut;
    private InputStream mockIn;

    RecordAudioStream(final SERecord record) {
        this.record = record;
    }

    @Override
    void open(Mode mode) {
        this.mode = mode;
    }

    @Override
    void close() {
//        File file = new File(record.soundPath);
//        LoggerFactory.obtainLogger(TAG).
//                d("work# file.length = " + file.length());
//
//        mockClose();
    }

    @Override
    void clear() {
    }

    @Override
    InputStream getInputStream() throws Exception {
        int format = SEProjectEngine.fileFormat;
        RecordFilterInputStream rfin = new RecordFilterInputStream(SpeexWrapper.getInputStream(record.soundPath, format));
        rfin.skip(record.start + record.position);
        rfin.setLimit(record.start, record.duration);
        return rfin;
        
//        return mockGetInputStream();
    }

    @Override
    OutputStream getOutputStream() throws Exception{
        int format = SEProjectEngine.fileFormat;
        return SpeexWrapper.getOutputStream(record.soundPath, format,
                SEProjectEngine.sampleRate, BITS_PER_SAMPLE, 1);

//       return mockGetOutputStream();
    }

//    private void mockClose() {
//        if (mode == Mode.READ) {
//            if (mockIn != null) {
//                try {
//                    mockIn.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return;
//        }
//
//        if (mode == Mode.WRITE) {
//            if (mockOut != null) {
//                try {
//                    mockOut.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private InputStream mockGetInputStream() throws Exception {
        File file = new File(record.soundPath);
        if (!file.exists()) {
            file.createNewFile();
        }

//        mockIn = new FileInputStream(file);
        RecordFilterInputStream rfin =
                new RecordFilterInputStream(new FileInputStream(file));

        rfin.skip(record.start + record.position);
        rfin.setLimit(record.start, record.duration);

        mockIn = rfin;
        return mockIn;
    }

    private OutputStream mockGetOutputStream() throws Exception {
        File file = new File(record.soundPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        mockOut = new FileOutputStream(file);
        return mockOut;
    }

    @Override
    void updatePosition(long position) {
        record.position += position;
        record.project.position += position;
    }

    @Override
    void updateDuration(long duration) {
        record.duration += duration;
        record.project.duration += duration;
    }

    @Override
    void finalizePosition() {
        LoggerFactory.obtainLogger(TAG).
                d("finalizePosition# record.position = " + record.position +
                        ", record.project.position = " + record.project.position);
    }

    @Override
    void finalizeDuration() {
        LoggerFactory.obtainLogger(TAG).
                d("finalizeDuration# record.duration = " + record.duration +
                        ", record.project.duration = " + record.project.duration);
    }

    @Override
    Mode getMode() {
        return mode;
    }

    private class RecordFilterInputStream extends FilterInputStream {
        private long readBytes;
        private long limit; // Indicates how many bytes can be read (after begin of file)
        private long skiped;
        private boolean isReachedEnd; // true - if we have reached the end of record

        private RecordFilterInputStream(InputStream in) {
            super(in);

            isReachedEnd = false;
            readBytes = 0;
            limit = 0;
            skiped = 0;
        }

        @Override
        public long skip(long n) throws IOException {
            long result = in.skip(n);

            readBytes += result;
//            if (limit != 0) {
//                limit = limit - skiped + result;
//                
//            }
            skiped = result;

            LoggerFactory.obtainLogger(TAG).
                    d("skip# " + getDescription() + "skiped = " + limit);
            return result;
        }

        /**
         * @param n
         * @throws IOException
         */
        public void setLimit(long start, long n) throws IOException {

            limit = start + n;

            LoggerFactory.obtainLogger(TAG).
                    d("setLimit# " + getDescription() + "limit = " + limit + "start = " + start);
        }

        @Override
        public int read() throws IOException {
            if (limit != 0 && readBytes == limit) return -1;
            
            int result = in.read();
            
            if (result != -1) {
                readBytes++;
//                LoggerFactory.obtainLogger(TAG).
//                        d("read# " + getDescription() + "readBytes = " + readBytes);
            } else {
//                LoggerFactory.obtainLogger(TAG).
//                        d("read# " + getDescription() + "EOF");
            }

            return result;
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (isReachedEnd) {
//                LoggerFactory.obtainLogger(TAG).
//                        d("read[]# " + getDescription() + "EOF");
                return -1;
            }

            int result = in.read(b);
            
            if (result != -1) {
                readBytes += result;
//                LoggerFactory.obtainLogger(TAG).
//                        d("read[]# " + getDescription() + "readBytes = " + readBytes);

                if (limit != 0 && readBytes > limit) {
                    b = cutPackage(b);
                    result = b.length;
                }
            } else {
//                LoggerFactory.obtainLogger(TAG).
//                        d("read[]# " + getDescription() + "EOF");
            }

//            LoggerFactory.obtainLogger(TAG).
//                    d("read[]# " + getDescription() + "returned = " + b.length);
            return result;

        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (isReachedEnd) return -1;
            
            LoggerFactory.obtainLogger(TAG).d("SSS off=" + off + " len=" + len);
            LoggerFactory.obtainLogger(TAG).d("SSS limit=" + limit + " skiped=" + skiped);
            
//            return read(b);  // FIXME
            
            int result = in.read(b, off, len);
            
            LoggerFactory.obtainLogger(TAG).d("SSS result=" + result);
            
            if (result != -1) {
                readBytes += result;

                if (limit != 0 && readBytes > limit) {
                    b = cutPackage(b);
                    result = b.length;
                }
            }

//            System.out.println(" read off, len result = " + result);
            return result;
        }

        private byte[] cutPackage(byte[] data) {
            int lastBytes = (int) (data.length - (readBytes - limit));
            byte[] buf = new byte[lastBytes];
            isReachedEnd = true;
            return buf;
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();

            isReachedEnd = false;
            readBytes = 0;
            limit = 0;
        }

        private String getDescription() {
            return "{start=" + record.start +
                    ", duration=" + record.duration +
                    ", position=" + record.position +
                    ", path=" + record.soundPath + "} ";
        }
    }
}
