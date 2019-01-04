package com.example.wangchao.androidbase2fragment.mode;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseApplication;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.file.FileUtils;
import com.example.wangchao.androidbase2fragment.utils.permission.PermissionsManager;
import com.example.wangchao.androidbase2fragment.utils.rxjava.ObservableBuilder;
import com.example.wangchao.androidbase2fragment.view.AutoFitTextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class VideoMode extends CameraModeBase{
    private static final String TAG = VideoMode.class.getSimpleName();
    /**
     * 当前是否是在录制视频
     */
    private boolean mIsRecordingVideo;
    private ICameraImp mICameraImp;
    /**
     * 相机预览的大小Size
     */
    private Size mPreviewSize;
    /**
     * 传感器的方向
     */
    private Integer mSensorOrientation;
    /**
     * 相机预览请求的Builder
     */
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;
    private Rect zoomRect;
    /**
     * 为相机创建一个CameraCaptureSession
     */
    private CameraCaptureSession mPreviewSession;
    private boolean isRecordGonging = false;
    /**
     * 点击开启录制时候创建的新视频文件路径
     */
    private String mNextVideoAbsolutePath;
    /**
     * 视频录制的大小
     */
    private Size mVideoSize;
    /**
     * 开启相机的锁住时间
     */
    private final int LOCK_TIME = 2500;
    private CameraCharacteristics characteristics;
    private List<String> oldVideoPath;
    private CompositeSubscription compositeSubscription;
    private float maxZoom;

    public VideoMode(ICameraImp iCameraImp){
        mICameraImp = iCameraImp;
        oldVideoPath = new CopyOnWriteArrayList<>();
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void writePictureData(Image image) {

    }

    @Override
    protected void openCamera(Activity activity,int width, int height) {
        if (PermissionsManager.checkVideoRecordPermission(activity)) {
            if (null == activity || activity.isFinishing()) {
                return;
            }
            AutoFitTextureView mTextureView = (AutoFitTextureView) getTextureView();
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
                Log.d(TAG, "tryAcquire");
                if (!mCameraOpenCloseLock.tryAcquire(LOCK_TIME, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                //String cameraId = manager.getCameraIdList()[0];
                // Choose the sizes for camera preview and video recording
                //characteristics = manager.getCameraCharacteristics(cameraId);
                for (String cameraId : manager.getCameraIdList()) {
                    characteristics = manager.getCameraCharacteristics(cameraId);
                    if (!Camera2Utils.matchCameraDirection(characteristics, mCurrentCameraDirection)) {
                        continue;
                    }
                    StreamConfigurationMap map = characteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    if (map == null) {
                        throw new RuntimeException("Cannot get available preview/video sizes");
                    }
                    //获取最大缩放值-----start
                    Float maxZoomValue = Camera2Utils.getMaxZoom(characteristics);
                    if (maxZoomValue != null) {
                        maxZoom = maxZoomValue;
                    }
                    //获取最大缩放值-----end
                    mVideoSize = Camera2Utils.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                    mPreviewSize = Camera2Utils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, mVideoSize);

                    int orientation = activity.getResources().getConfiguration().orientation;
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    } else {
                        mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    }
                    Log.d("configureTransform","configureTransform viewWidth="+mPreviewSize.getHeight()+"   viewWidth="+mPreviewSize.getWidth());

                    configureTransform(activity,width, height);
                    mMediaRecorder = new MediaRecorder();
                    manager.openCamera(cameraId, mStateCallback, null);
                    return;//add video--------exception
                }
            } catch (CameraAccessException e) {
                Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
                activity.finish();
            } catch (NullPointerException e) {
                // Currently an NPE is thrown when the Camera2API is used but not supported on the
                // device this code runs.
                Log.d(TAG,"not supported Camera2API-------------");
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.");
            }
        }
    }
    @Override
    protected void configureTransform(Activity activity,int viewWidth, int viewHeight) {
        if (null == getTextureView() || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        Log.d("configureTransform","configureTransform viewWidth="+mPreviewSize.getHeight()+"   viewWidth="+mPreviewSize.getWidth());
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        getTextureView().setTransform(matrix);
    }

    @Override
    protected void startPreview() {
        TextureView mTextureView = getTextureView();
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        //开始相机预览
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            //从拍照切换到摄像头，获取录像完成后需要重新恢复以前的状态
            //zoom 缩放---start--
            //float currentZoom = mICameraImp.getCamera2Manager().getZoomProportion() * maxZoom;//设置最大缩放比例
            float currentZoom = mICameraImp.getCameraMangaer().getZoomProportion() * 1.0f;
            updateZoomRect(currentZoom);
            //zoom 缩放---end--
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getTextureViewContext();
                            if (null != activity) {
                                Toast.makeText(activity, "相机预览配置失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mICameraImp.getWorkThreadManager().getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (null != mTextureView) {
            configureTransform(getTextureViewContext(), mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    @Override
    public void startOperate() {
        TextureView mTextureView = getTextureView();
        if (mTextureView.isAvailable()) {
            openCamera(getTextureViewContext(),mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    @Override
    public void notifyFocusState() {
        //float currentZoom = mICameraImp.getCameraMangaer().getZoomProportion() * maxZoom;
        float currentZoom = mICameraImp.getCameraMangaer().getZoomProportion() * 1.0f;
        updateZoomRect(currentZoom);
        updatePreview();
    }

    /**
     * 更新缩放，数字调焦
     *
     * @param currentZoom
     */
    private void updateZoomRect(float currentZoom) {
        try {
            Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (rect == null) {
                return;
            }
            zoomRect = Camera2Utils.createZoomRect(rect, currentZoom);
            if (zoomRect == null) {
                return;
            }
            Log.i(TAG, "zoom对应的 rect对应的区域 " + zoomRect.left + " " + zoomRect.right + " " + zoomRect.top + " " + zoomRect.bottom);
            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopOperate() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @Override
    public void cameraClick() {
        if (mIsRecordingVideo) {
            stopRecordingVideo(true);
        } else {
            startRecordingVideo();
        }
    }
    /**
     * 停止录制
     */
    private void stopRecordingVideo(final boolean isFinish) {
        mIsRecordingVideo = false;
        try {
            mPreviewSession.stopRepeating();
            mPreviewSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Subscription subscription = Observable
                //延迟三十毫秒
                .timer(30, TimeUnit.MICROSECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        // 停止录制
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        if (isFinish) {
                            isRecordGonging = false;
                            Log.i(TAG, "stopRecordingVideo recording complete--------");
                            if (camera2VideoRecordCallBack != null) {
                                camera2VideoRecordCallBack.finishRecord();
                            }
                            mergeMultipleFileCallBack();
                            mNextVideoAbsolutePath = null;
                            oldVideoPath.clear();
                        } else {//暂停的操作
                            Log.i(TAG, "pauseRecordingVideo recording stop--------");
                            //若是开始新的录制，原本暂停产生的多个文件合并成一个文件。
                            oldVideoPath.add(mNextVideoAbsolutePath);
                            if (oldVideoPath.size() > 1) {
                                mergeMultipleFile();
                            }
                            mNextVideoAbsolutePath = null;
                        }
                        startPreview();
                    }
                });
        compositeSubscription.add(subscription);
    }
    /**
     * 完成录制，输出最终的视频录制文件
     */
    private void mergeMultipleFileCallBack() {
        if (oldVideoPath.size() > 0) {
            Log.i(TAG, " mergeMultipleFileCallBack file.size()===" + oldVideoPath.size());
            Subscription subscription = ObservableBuilder.createMergeMuiltFile(BaseApplication.getInstance(), oldVideoPath.get(0), mNextVideoAbsolutePath)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            if (mCamera2ResultCallBack != null) {
                                mCamera2ResultCallBack.callBack(ObservableBuilder.createVideo(s));
                            }
                            Log.i(TAG, " mergeMultipleFileCallBack--------success-------------");
                            //ToastUtils.showToast(BaseApplication.getInstance(), "视频文件保存路径:" + s);
                            Log.d(TAG,"视频文件保存路径======"+s);
                        }
                    });
            compositeSubscription.add(subscription);
        } else {
            if (mCamera2ResultCallBack != null) {
                mCamera2ResultCallBack.callBack(ObservableBuilder.createVideo(mNextVideoAbsolutePath));
            }
            Log.d(TAG,"视频文件保存在======"+mNextVideoAbsolutePath);
           // ToastUtils.showToast(BaseApplication.getInstance(), "视频文件保存在" + mNextVideoAbsolutePath);
        }
    }
    /**
     * 暂停后又从新恢复录制，合并多个视频文件
     */
    private void mergeMultipleFile() {
        Log.i(TAG, " mergeMultipleFile  开始操作：文件个数 " + oldVideoPath.size());
        Subscription subscription = ObservableBuilder.createMergeMuiltFile(BaseApplication.getInstance(), oldVideoPath.get(0), oldVideoPath.get(1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String filePath) {
                        oldVideoPath.clear();
                        oldVideoPath.add(filePath);
                        Log.i(TAG, " mergeMultipleFile  完成： 文件个数" + oldVideoPath.size());
                    }
                });

        compositeSubscription.add(subscription);
    }
    /**
     * 开始视频录制，创建次录像的session会。
     */
    private void startRecordingVideo() {
        Log.i(TAG, " startRecordingVideo  init start --------------------------- ");
        TextureView mTextureView = getTextureView();
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            mTextureView.setKeepScreenOn(true);//录像设置屏幕常亮度
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            //创建录制的session会话
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            // 为相机预览设置Surface
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);
            // 为 MediaRecorder设置Surface
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);
            //与未录像的状态保持一致。
            if (zoomRect != null) {
                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
            }
            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    Log.i(TAG, " startRecordingVideo  isRecording----------------- ");
                    getTextureViewContext().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsRecordingVideo = true;
                            isRecordGonging = true;
                            mMediaRecorder.start();
                            if (camera2VideoRecordCallBack != null) {
                                camera2VideoRecordCallBack.startRecord();
                            }
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getTextureViewContext();
                    if (null != activity) {
                        Toast.makeText(activity.getApplicationContext(), "相机设备配置失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mICameraImp.getWorkThreadManager().getBackgroundHandler());
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * startPreView()之后执行用于更新相机预览界面
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mICameraImp.getWorkThreadManager().getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }
    /**
     * 设置媒体录制器的配置参数
     * <p>
     * 音频，视频格式，文件路径，频率，编码格式等等
     *
     * @throws IOException
     */
    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getTextureViewContext();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mNextVideoAbsolutePath = FileUtils.createVideoDiskFile(activity, FileUtils.createVideoFileName()).getAbsolutePath();
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        //每秒30帧
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(ORIENTATIONS.get(rotation));
                break;
            default:
                break;
        }
        mMediaRecorder.prepare();
    }

    /**
     * 是否在进行视频录制，录制状态，包含进行中，暂停中。
     *
     * @return
     */
    @Override
    public boolean isVideoRecord() {
        return isRecordGonging;
    }
    /**
     * 暂停录制
     */
    public void pauseRecordingVideo() {
        stopRecordingVideo(false);
    }
    /**
     * 释放MediaRecord
     */
    public void onReleaseRecord() {
        if (mIsRecordingVideo){
            stopRecordingVideo(true);
        }
    }
    /**
     * CameraDevice
     * @return
     */
    @Override
    public CameraDevice getCameraDevice(){
        return mCameraDevice;
    };

    /**
     * CameraCaptureSession
     * @return
     */
    @Override
    public CameraCaptureSession getCameraCaptureSession(){
        return mPreviewSession;
    }
    /**
     * CaptureRequest
     * @return
     */
    @Override
    public CaptureRequest getCaptureRequest(){
        return mPreviewBuilder.build();
    }

    @Override
    public CaptureRequest.Builder getCaptureRequestBuilder() {
        return mPreviewBuilder;
    }
    @Override
    public Size getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public Rect getActiveArraySize() {
        return null;
    }
    @Override
    public int getDisplayOrientation() {
        return 0;
    }
    @Override
    public CameraCaptureSession.CaptureCallback getCameraCaptureSessionCaptureCallback() {
        return null;
    }

}
