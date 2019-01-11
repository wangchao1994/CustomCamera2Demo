package com.example.wangchao.androidbase2fragment.utils.camera;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * This class packages the APIs which are added by MTK for media recorder
 * in MTK platform.
 *
 */
public class MediaRecorderEx {
    private static final String TAG = "MediaRecorderEx";
    private static final String CLASS_NAME = "android.media.MediaRecorder";
    private static final String METHOD_NAME = "setParameter";
    private static final String METHOD_NAME_EXTRA = "setParametersExtra";
    private static final Class[] METHOD_TYPES = new Class[] {String.class};
    private static Method sSetParameter;
    private static Method sSetParametersExtra;
    static {
        try {
            Class cls = Class.forName(CLASS_NAME);
            sSetParameter = cls.getDeclaredMethod(METHOD_NAME, METHOD_TYPES);
            if (sSetParameter != null) {
                sSetParameter.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException: " + METHOD_NAME);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException: " + CLASS_NAME);
        }
    }

    static {
        try {
            Class cls = Class.forName(CLASS_NAME);
            sSetParametersExtra = cls.getDeclaredMethod(METHOD_NAME_EXTRA, METHOD_TYPES);
            if (sSetParametersExtra != null) {
                sSetParametersExtra.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException: " + METHOD_NAME_EXTRA);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException: " + CLASS_NAME);
        }
    }
    private static void setParameter(MediaRecorder recorder, String nameValuePair) {
        if (sSetParameter != null) {
            try {
                sSetParameter.invoke(recorder, nameValuePair);
            } catch (IllegalAccessException ae) {
                Log.e(TAG, "IllegalAccessException!", ae);
            } catch (InvocationTargetException te) {
                Log.e(TAG, "InvocationTargetException!", te);
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "IllegalArgumentException!", ex);
            } catch (NullPointerException npe) {
                Log.e(TAG, "NullPointerException!", npe);
            }
        } else {
            Log.e(TAG, "setParameter: Null method!");
        }
    }

    /**
     * Defines the HD record mode. These constants are used with.
     * {@link MediaRecorderEx#setHDRecordMode(MediaRecorder, int, boolean)}.
     */
    public final class HDRecordMode {
        /* Do not change these values without updating their counterparts.
         * in AudioYusuHardware.cpp.
         */
        private HDRecordMode() {}
        /** Normal mode. */
        public static final int NORMAL = 0;
        /** Indoor mode. */
        public static final int INDOOR = 1;
        /** Outdoor mode. */
        public static final int OUTDOOR = 2;
    }

    /**
     * Sets up the HD record mode to be used for recording.
     * Call this method before MediaRecorder.prepare().
     *
     * @param recorder Recorder used to record audio or video
     * @param mode HD record mode to be used
     * @param isVideo True if it is used for record video; otherwise, false.
     * @throws IllegalArgumentException if mode is wrong
     * @throws IllegalStateException If it is called after MediaRecorder.prepare()
     * @see //com.mediatek.media.MediaRecorderEx.HDRecordMode
     */
    public static void setHDRecordMode(MediaRecorder recorder, int mode, boolean isVideo)
            throws IllegalStateException, IllegalArgumentException {
        if (mode < HDRecordMode.NORMAL || mode > HDRecordMode.OUTDOOR) {
            throw new IllegalArgumentException("Illegal HDRecord mode:" + mode);
        }

        if (isVideo) {
            setParameter(recorder, "audio-param-hdrecvideomode=" + mode);
        } else {
            setParameter(recorder, "audio-param-hdrecvoicemode=" + mode);
        }
    }

    /**
     * Sets up the artist meta data to be saved in file header during recording.
     * It only works for MediaRecorder.OutputFormat.THREE_GPP. For other
     *  MediaRecorder.OutputFormat, it does nothing.
     * Call this method before MediaRecorder.prepare().
     *
     * @param recorder Recorder used to record audio
     * @param artist Artist name to be set
     * @throws IllegalStateException If it is called after MediaRecorder.prepare()
     */
    public static void setArtistTag(MediaRecorder recorder, String artist)
        throws IllegalStateException {
        setParameter(recorder, "media-param-tag-artist=" + artist);
    }

    /**
     * Sets up the album meta data to be saved in file header during recording.
     * It only works for MediaRecorder.OutputFormat.THREE_GPP. For other
     *  MediaRecorder.OutputFormat, it does nothing.
     * Call this method only before MediaRecorder.prepare().
     *
     * @param recorder Recorder used to record audio
     * @param album Album name to be set
     * @throws IllegalStateException If it is called after MediaRecorder.prepare()
     */
    public static void setAlbumTag(MediaRecorder recorder, String album)
        throws IllegalStateException {
        setParameter(recorder, "media-param-tag-album=" + album);
    }

    /**
     * Sets up the effect option during recording.
     *
     * @param recorder Recorder used to record audio
     * @param effectOption Effect option to be set
     * @throws IllegalStateException If it is called after MediaRecorder.prepare()
     *
     * @hide
     * @internal
     */
    public static void setPreprocessEffect(MediaRecorder recorder, int effectOption)
        throws IllegalStateException {
        setParameter(recorder, "audio-param-preprocesseffect=" + effectOption);
    }

    /**
     * Set video offset.
     * @param recorder is media recorder
     * @param offset value
     * @param video is video
     * @hide
     * @internal
     */
    public static void setVideoBitOffSet(MediaRecorder recorder, int offset, boolean video) {
        if (video) {
            setParameter(recorder, "param-use-64bit-offset=" + offset);
            Log.v(TAG, "setVideoBitOffSet is true,offset= " + offset);
        }
    }
    /**
     * set paramters for extend by self.
     * @param recorder the meida recorder
     * @param info the want set info
     * @throws IllegalStateException If info is not define.
     */
    public static void setParametersExtra(MediaRecorder recorder, String info)
        throws IllegalStateException {
        if (recorder == null) {
            Log.e(TAG, "Null MediaRecorder!");
            return;
        }
        //recorder.setParametersExtra(info);
    }

    /**
     * use to charge whether can use function setParametersExtra.
     * setParametersExtra function is add by self,so before use it
     * should charge whether can use it.
     * @return the charge result.
     */
    public static boolean isCanUseParametersExtra() {
        return sSetParametersExtra != null;
    }

    /**
     * Pause the recording.
     * Call this function after MediaRecorder.start()
     * In additation ,call MediaRecorder.start() to resuem the recorder after
     * this function called.
     * @param recorder Recorder used to record audio.
     * @throws IllegalStateException If it is not called after start().
     */
    public static void pause(MediaRecorder recorder) throws IllegalStateException {
        if (recorder == null) {
            return;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //recorder.setParametersExtra("media-param-pause=1");
        } else {
            recorder.pause();
        }

    }
    /**
     * Resuem the recording.
     * Call this function after pause() to resuem the recorder
     * @param recorder Recorder used to record audio.
     * @throws IllegalStateException If it is not called after pause().
     */
    public static void resume(MediaRecorder recorder) throws IllegalStateException {
        if (recorder == null) {
            return;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            recorder.start();
        } else {
            recorder.resume();
        }
    }
}
