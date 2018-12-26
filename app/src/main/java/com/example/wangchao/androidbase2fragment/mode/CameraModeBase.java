package com.example.wangchao.androidbase2fragment.mode;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;

import rx.Observable;

public abstract class CameraModeBase {
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    protected Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * 转换屏幕旋转角度到JPEG的方向
     */
    protected static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    protected static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    protected static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    protected static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    /**
     * 相机设备
     */
    protected CameraDevice mCameraDevice;
    private WeakReference<TextureView> weakReference;
    protected int mCurrentCameraDirection;

    public void setCurrentCameraDirection(int currentDirection) {
        mCurrentCameraDirection = currentDirection;
    }

    public void setWeakReference(TextureView textureView) {
        if (getTextureView()==null){
            weakReference = new WeakReference<>(textureView);
        }
    }

    protected TextureView getTextureView() {
        return weakReference != null ? weakReference.get() : null;
    }

    protected Activity getTextureViewContext() {
        return (Activity) getTextureView().getContext();
    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    protected final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(getTextureViewContext(),width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(getTextureViewContext(),width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    protected final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getTextureViewContext();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    /**
     * 切换摄像头的操作
     */
    public void switchCameraDirectionOperate(){
        stopOperate();
        startOperate();
    }
    /**
     * ImageReader的回调监听器
     * <p>
     * onImageAvailable被调用的时候，已经拍照完，准备保存的操作
     * 通常写入磁盘文件中。
     */
    protected  final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            writePictureData(reader.acquireNextImage());
        }
    };
    /**
     * 写入图片数据
     *
     * @param image
     */
    protected abstract void writePictureData(Image image);
    /**
     * openCamera
     * @param width
     * @param height
     */
    protected abstract void openCamera(Activity activity, int width, int height);

    protected abstract void configureTransform(Activity activity, int viewWidth, int viewHeight);

    protected abstract void startPreview();

    /**
     * 开始操作
     */
    public abstract  void startOperate();

    /**
     * 关闭当前的PreviewSession
     */
    protected abstract  void closePreviewSession();
    /**
     * 通知调焦情况，发生改变
     */
    public abstract  void notifyFocusState();
    /**
     * 停止操作
     */
    public  abstract  void stopOperate();
    /**
     *相机按钮的点击事件，可能在拍照，可能在录像
     */
    public abstract void cameraClick();


    protected Camera2ResultCallBack mCamera2ResultCallBack;

    public void setCamera2ResultCallBack(Camera2ResultCallBack camera2ResultCallBack) {
        mCamera2ResultCallBack = camera2ResultCallBack;
    }
    public interface Camera2ResultCallBack {
        /**
         * 写入JPEG图片后返回的路径
         *
         * @param result
         */
        void callBack(Observable<String> result);
    }
    public interface  Camera2VideoRecordCallBack{
        /**
         *  开始录制
         */
        void startRecord();

        /**
         * 完成录制
         */
        void finishRecord();
    }

    protected Camera2VideoRecordCallBack camera2VideoRecordCallBack;

    public void setCamera2VideoRecordCallBack(Camera2VideoRecordCallBack mCamera2VideoRecordCallBack) {
        camera2VideoRecordCallBack = mCamera2VideoRecordCallBack;
    }
}
