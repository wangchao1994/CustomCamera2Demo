package com.example.wangchao.androidbase2fragment.mode;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseApplication;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.permission.PermissionsManager;
import com.example.wangchao.androidbase2fragment.utils.rxjava.ObservableBuilder;
import com.example.wangchao.androidbase2fragment.utils.toast.ToastUtils;
import com.example.wangchao.androidbase2fragment.view.AutoFitTextureView;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class PhotoMode extends CameraModeBase{
    private static final String TAG = PhotoMode.class.getSimpleName();
    /**
     * 开启相机的锁住时间
     */
    private final int LOCK_TIME = 2500;
    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;
    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    /**
     * 预览请求的Builder
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /**
     * 预览的请求
     */
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCaptureSession;
    /**
     * 处理静态图片的输出
     */
    private ImageReader imageReader;
    /**
     * 相机传感器方向
     */
    private int mSensorOrientation;
    /**
     * 相机最大的预览宽度
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * 相机最大的预览高度
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    /**
     * 相机预览的大小Size
     */
    private Size mPreviewSize;
    /**
     * 是否支持自动对焦
     */
    private boolean mAutoFocusSupported;
    private float currentZoom;
    private Rect zoomRect;
    private CameraCharacteristics characteristics;
    private ICameraImp mICameraImp;
    /**
     * 是否支持闪光灯
     */
    private boolean mFlashSupported;
    /**
     * Camera state: Showing camera preview.
     * 相机预览状态
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     * <p>
     * 相机拍照，被锁住，等待焦点状态
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     * 图片已经获取
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;
    /**
     * CameraCaptureSession.CaptureCallback : 处理捕获到的JPEG事件。
      */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };


    public PhotoMode(ICameraImp iCameraImp){
        mICameraImp = iCameraImp;
    }
    @Override
    protected void writePictureData(Image image) {
        if (mCamera2ResultCallBack != null) {
            mCamera2ResultCallBack.callBack(ObservableBuilder.createWriteCaptureImage(BaseApplication.getInstance(), image));
        }
    }

    @Override
    protected void openCamera(Activity activity,int width, int height) {
        if (PermissionsManager.checkCameraPermission(activity)){
            setUpCameraOutputs(activity,width, height);
            configureTransform(activity,width, height);
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (!mCameraOpenCloseLock.tryAcquire(LOCK_TIME, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                manager.openCamera(mCameraId, mStateCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }
        }
    }

    @Override
    protected void configureTransform(Activity activity,int viewWidth, int viewHeight) {

        if (null == getTextureView() || null == mPreviewSize || null ==  activity) {
            return;
        }
        int rotation =  activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
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
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        getTextureView().setTransform(matrix);
    }

    @Override
    protected void startPreview() {
        //开启相机预览界面
        createCameraPreviewSession();
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
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    @Override
    public void notifyFocusState() {
        if (mPreviewRequestBuilder != null) {
            try {
                //currentZoom = maxZoom * mICameraImp.getCamera2Manager().getZoomProportion();//设置最大缩放比例
                currentZoom = 1.0f * mICameraImp.getCameraMangaer().getZoomProportion();
                setZoom(currentZoom);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("notifyFocusState","notifyFocusState Exception==="+e.getMessage());
            }
        }
    }
    public void setZoom(float currentZoom) {
        try {
            zoomRect = createZoomReact();
            Log.d("camera_log","setZoom======"+zoomRect);
            if (zoomRect==null){
                Log.d("camera_log","相机不支持对焦");
                return ;
            }
            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION,zoomRect);
            mPreviewRequest = mPreviewRequestBuilder.build();
            //为CameraCaptureSession设置复用的CaptureRequest。
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 计算出zoom所对应的裁剪区域
     * @return
     */
    private Rect createZoomReact() {
        if (currentZoom==0){
            return null;
        }
        try {
            Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (rect==null){
                return null;
            }
            zoomRect =Camera2Utils.createZoomRect(rect,currentZoom);
            Log.i("camera_log", "zoom对应的 rect对应的区域 " + zoomRect.left + " " + zoomRect.right + " " + zoomRect.top + " " + zoomRect.bottom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zoomRect;
    }
    @Override
    public void stopOperate() {
        closeCamera();
    }

    @Override
    public void cameraClick() {
        takePicture();
    }

    private void takePicture() {
        if (mAutoFocusSupported) {
            Log.i(TAG,"camera 支持自动调焦，正在锁住焦点");
            lockFocus();
        } else {//设备不支持自动对焦，则直接拍照。
            Log.i(TAG,"camera 不支持自动调焦，直接拍照");
            captureStillPicture();
        }
    }
    /**
     * 锁住焦点
     */
    private void lockFocus() {
        try {
            //锁住焦点
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // 标识，正在进行拍照动作
            mState = STATE_WAITING_LOCK;
            //进行拍照处理
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(Activity activity,int width, int height) {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                characteristics  = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (!Camera2Utils.matchCameraDirection(characteristics, mCurrentCameraDirection)) {
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                //检查设备,是否支持自动对焦
                mAutoFocusSupported = Camera2Utils.checkAutoFocus(characteristics);

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                //设置ImageReader,将大小，图片格式
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mICameraImp.getWorkThreadManager().getBackgroundHandler());

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.

//                mPreviewSize = Camera2Utils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, largest);
                mPreviewSize = Camera2Utils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest, new CompareSizesByArea());
                Log.d("configureTransform","configureTransform viewWidth="+mPreviewSize.getHeight()+"   viewWidth="+mPreviewSize.getWidth());
                Log.d("configureTransform","configureTransform maxPreviewHeight="+maxPreviewHeight+"   maxPreviewWidth="+maxPreviewWidth);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = activity.getResources().getConfiguration().orientation;
                AutoFitTextureView mTextureView = (AutoFitTextureView) getTextureView();
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(  mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio( mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                mTextureView.setAspectRatio(maxPreviewHeight, maxPreviewWidth);
                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                Log.i("camera_log", " 根据相机的前后摄像头" + mCameraId + " 方向是：" + mCurrentCameraDirection);
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            //不支持该设备
            if (e instanceof NullPointerException) {
                ToastUtils.showToast(BaseApplication.getInstance(), "设备不支持Camera2 API");
            }
        }
    }
    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = getTextureView().getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed( @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.d("TAG"," Attach ----->Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void captureStillPicture() {
        try {
            Activity actvity = getTextureViewContext();
            if (null == actvity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = actvity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, Camera2Utils.getOrientation(ORIENTATIONS, mSensorOrientation, rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback  = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    //拍照完成，进行释放焦点操作。
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,   CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());
            Log.i("camera_log", TAG+" 拍照完成，释放焦点  unlockFocus() ");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * 运行预捕获的序列，捕获一个静态图片
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set. 设置成预捕获状态，将需等待。
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,  mICameraImp.getWorkThreadManager().getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 关闭当前的相机设备，释放资源
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closi" + "ng.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
    /**
     * 设置是否开启闪光灯
     *
     * @param requestBuilder
     */
    public void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        boolean flashOpenOrClose = mICameraImp.getFlashOpenOrClose();
        Log.d("camera_log","setAutoFlash------flashOpenOrClose===="+flashOpenOrClose);
        if (mFlashSupported){
            if (flashOpenOrClose){
                requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            }else{
                requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            }
        }
    }

}
