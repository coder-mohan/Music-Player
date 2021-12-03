package com.ldt.musicr.ui.widget.soundfile;

        import android.content.res.AssetFileDescriptor;
        import android.net.Uri;
        import android.util.Log;

        import com.ldt.musicr.App;

        import java.io.InputStream;

/**
 * CheapWAV represents a standard 16-bit WAV file, splitting it into
 * artificial frames to get an approximation of the waveform contour.
 *
 */
public class CheapWAV extends SoundFile {
    public static final String TAG = "CheapWAV";

    public static Factory getFactory() {
        return new Factory() {
            public SoundFile create() {
                return new CheapWAV();
            }
            public String[] getSupportedExtensions() {
                return new String[]{"wav"};
            }
        };
    }

    // Member variables containing frame info
    private int mNumFrames;
    private int[] mFrameGains;
    private int mFileSize;
    private int mSampleRate;
    private int mChannels;

    public CheapWAV() {
    }

    public int getNumFrames() {
        return mNumFrames;
    }

    public int getSamplesPerFrame() {
        return 1024;
    }

    public int[] getFrameGains() {
        return mFrameGains;
    }

    public int getFileSizeBytes() {
        return mFileSize;
    }

    public int getAvgBitrateKbps() {
        return mSampleRate * mChannels * 2 / 1024;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannels() {
        return mChannels;
    }

    public String getFiletype() {
        return "WAV";
    }

    public void readFile(Uri inputFile) throws java.io.IOException {
        super.readFile(inputFile);

        InputStream stream = null;
        AssetFileDescriptor file;
        file = App.getInstance().getContentResolver().openAssetFileDescriptor(inputFile, "r");

        if(file == null) throw  new NullPointerException("File is null");

        stream = file.createInputStream();
        if(stream == null) throw new NullPointerException("Input stream is null");

        else Log.d("audioSeekbar", "ReadFile: input stream is not null");

        // No need to handle filesizes larger than can fit in a 32-bit int
        mFileSize = (int) file.getLength();

        if (mFileSize < 128) {
            throw new java.io.IOException("File too small to parse");
        }
        try {
            WavFileDescriptor wavFile = WavFileDescriptor.openWavFile(file);
            mNumFrames = (int) (wavFile.getNumFrames() / getSamplesPerFrame());
            mFrameGains = new int[mNumFrames];
            mSampleRate = (int) wavFile.getSampleRate();
            mChannels = wavFile.getNumChannels();

            int gain, value;
            int[] buffer = new int[getSamplesPerFrame()];
            for (int i = 0; i < mNumFrames; i++) {
                gain = -1;
                wavFile.readFrames(buffer, getSamplesPerFrame());
                for (int j = 0; j < getSamplesPerFrame(); j++) {
                    value = buffer[j];
                    if (gain < value) {
                        gain = value;
                    }
                }
                mFrameGains[i] = (int) Math.sqrt(gain);
                if (mProgressListener != null) {
                    boolean keepGoing = mProgressListener.reportProgress(i * 1.0 / mFrameGains.length);
                    if (!keepGoing) {
                        break;
                    }
                }
            }
            if (wavFile != null) {
                wavFile.close();
            }
        } catch (WavFileException e) {
            Log.e(TAG, "Exception while reading wav file", e);
        }
    }
}