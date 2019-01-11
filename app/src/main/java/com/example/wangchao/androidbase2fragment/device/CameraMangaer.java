package com.example.wangchao.androidbase2fragment.device;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseApplication;
import com.example.wangchao.androidbase2fragment.mode.CameraModeBase;
import com.example.wangchao.androidbase2fragment.mode.PhotoMode;
import com.example.wangchao.androidbase2fragment.mode.VideoMode;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.file.FileUtils;
import com.example.wangchao.androidbase2fragment.utils.toast.ToastUtils;
import com.example.wangchao.androidbase2fragment.view.focus.FocusViewController;
import com.example.wangchao.androidbase2fragment.view.focus.IFocusView;

/**
 * Camera管理者
 */
public class CameraMangaer {
    private static final String TAG = CameraMangaer.class.getSimpleName();
    private ICameraImp mICameraImp;
    private static final int CAMERA_BACK = 0;//默认使用后摄像头
    private static final int CAMERA_FRONT = 1;
    private int currentCameraDirection = 0;
    private CameraModeBase mPhotoMode;
    private CameraModeBase mVideoMode;
    private boolean isFlashOnOrClose = true;//默认开启闪光灯
    private float zoomProportion = 1.0f;
    private CameraModeBase mCurrentMode;
    private boolean isManualFocus;
    private String mPhotoPath;
    private FocusViewController mFocusViewController;


    public CameraMangaer(ICameraImp iCameraImp) {
        mICameraImp = iCameraImp;
        mPhotoMode = new PhotoMode(mICameraImp);
        mVideoMode = new VideoMode(mICameraImp);
        //设置当前CameraId
        setCurrentCameraDirection(currentCameraDirection);
        mCurrentMode = mPhotoMode;//默认拍照模式
        isManualFocus = false;//默认自动对焦

    }

    private void setCurrentCameraDirection(int currentCameraDirection) {
        int direction = (currentCameraDirection==CAMERA_BACK)? CameraCharacteristics.LENS_FACING_BACK:CameraCharacteristics.LENS_FACING_FRONT;
        Log.i(TAG," currentCameraId："+(direction == CameraCharacteristics.LENS_FACING_BACK?"BACK":"FRONT"));
        //拍照和录像的操作类
        mVideoMode.setCurrentCameraDirection(direction);
        mPhotoMode.setCurrentCameraDirection(direction);
    }

    public void setTextureView(TextureView textureView) {
        mPhotoMode.setWeakReference(textureView);
        mVideoMode.setWeakReference(textureView);
        mCurrentMode.startOperate();
    }

    public void setCamera2ResultCallBack(CameraModeBase.Camera2ResultCallBack camera2ResultCallBack) {
        mPhotoMode.setCamera2ResultCallBack(camera2ResultCallBack);
        mVideoMode.setCamera2ResultCallBack(camera2ResultCallBack);
    }

    public  void setCameraVideoCallBack(CameraModeBase.Camera2VideoRecordCallBack cameraVideoCallBack){
        mVideoMode.setCamera2VideoRecordCallBack(cameraVideoCallBack);
    }

    public void onPause() {
        mCurrentMode.stopOperate();//关闭相关操作
    }
    public void takePictureOrVideo() {
        mCurrentMode.cameraClick();//执行拍照或是录像
    }
    /**
     * 暂停视频
     */
    public  void pauseVideoRecord(){
        ((VideoMode) mVideoMode).pauseRecordingVideo();
    }
    /**
     * 切换到拍照还是录像模式
     * @param currentMode
     */
    public void switchCameraMode(int currentMode) {
        Log.i(TAG,TAG+" CurrenMode： "+(currentMode==Camera2Utils.MODE_CAMERA?"Photo":"Video"));
        switch (currentMode) {
            //切换到拍照模式
            case Camera2Utils.MODE_CAMERA:
                mVideoMode.stopOperate();
                mCurrentMode = mPhotoMode;
                break;
            //切换到录像模式
            case Camera2Utils.MODE_VIDEO_RECORD:
                mPhotoMode.stopOperate();
                mCurrentMode = mVideoMode;
                break;
            default:
                break;
        }
        mCurrentMode.startOperate();
    }

    /**
     * 切换摄像头，前还是后
     * @param direction
     */
    public void switchCameraDirection(int direction){
        //相同摄像头方向，不进行操作
        if (currentCameraDirection== direction){
            Log.d("camera_log","switchCameraDirection- currentDirection== direction---="+(currentCameraDirection== direction));
            return;
        }
        //当视频录制状态，不能切换摄像头
        if (mCurrentMode instanceof  VideoMode){
            if (((VideoMode)mCurrentMode).isVideoRecord()){
                ToastUtils.showToast(BaseApplication.getInstance(),"请结束录像，再切换摄像头");
                return;
            }
        }
        switch (direction){
            case  CAMERA_BACK:
                Log.d("camera_log","正在切换到后摄像头-----------------------");
                break;
            case  CAMERA_FRONT:
                Log.d("camera_log","正在切换到前摄像头-----------------------");
                break;
        }
        currentCameraDirection = direction;
        setCurrentCameraDirection(currentCameraDirection);
        mCurrentMode.switchCameraDirectionOperate();
    }

    /**
     * 闪光灯开关
     * @param values
     */
    public void setFlashOnOrClose(boolean values){
        isFlashOnOrClose = values;
    }
    public boolean getFlashOnOrClose() {
        return isFlashOnOrClose;
    }
    /**
     * 设置焦距比例，从设置焦距值
     * @param mZoomProportion
     */
    public void setZoomProportion(float mZoomProportion) {
        zoomProportion = mZoomProportion;
        mCurrentMode.notifyFocusState();
    }
    public boolean isVideoRecording() {
        return mCurrentMode.isVideoRecord();
    }

    /**
     * 获取当前CameraId
     * @return
     */
    public int getCameraId(){
        return currentCameraDirection;
    }
    /**
     * 释放MediaRecord
     */
    public void onReleaseMediaRecord() {
        ((VideoMode) mVideoMode).onReleaseRecord();
    }
    /**
     * 获取缩放比例值
     * @return
     */
    public float getZoomProportion() {
        return zoomProportion;//默认1.0f
    }

    /**
     * 设置对焦类型(是否手动对焦)
     * @param mIsManualFocus
     */
    public void setManualFocus(boolean mIsManualFocus){
        isManualFocus = mIsManualFocus;
    }
    /**
     * 获取当前对焦模式
     * @return
     */
    public boolean getMaunalFocus(){
        return isManualFocus;
    }
    /**
     * CameraDevice
     * @return
     */
    public CameraDevice getCameraDevice(){
        return mCurrentMode.getCameraDevice();
    };

    /**
     * CameraCaptureSession
     * @return
     */
    public CameraCaptureSession getCameraCaptureSession(){
        return mCurrentMode.getCameraCaptureSession();
    }
    /**
     * CaptureRequest
     * @return
     */
    public CaptureRequest getCaptureRequest(){
        return mCurrentMode.getCaptureRequest();
    }

    /**
     * CaptureRequest.Builder
     * @return
     */
    public CaptureRequest.Builder getCaptureRequestBuilder() {
        return mCurrentMode.getCaptureRequestBuilder();
    }

    /**
     * PreviewSize
     * @return
     */
    public Size getPreviewSize() {
        return mCurrentMode.getPreviewSize();
    }

    /**
     * CameraCaptureSession.CaptureCallback
     * @return
     */
    public CameraCaptureSession.CaptureCallback getCameraCaptureSessionCaptureCallback() {
        //return mCurrentMode.getCameraCaptureSessionCaptureCallback();
        return mPhotoMode.getCameraCaptureSessionCaptureCallback();
    }
    /**
     * Rect ActiveArraySize
     * @return
     */
    public Rect getActiveArraySize() {
        return mPhotoMode.getActiveArraySize();
    }
    /**
     * DisplayOrientation
     * @return
     */
    public int getDisplayOrientation() {
        return mPhotoMode.getDisplayOrientation();
    }
    /**
     * 点击对焦
     * @param event
     * @param viewWidth
     * @param viewHeight
     */
    public void setFocusOnTouchEvent(MotionEvent event, int viewWidth, int viewHeight){
        if (mFocusViewController == null){
            mFocusViewController = new FocusViewController(mICameraImp);
        }
        Log.d("camera_log","setFocusOnTouchEvent--------------getCameraDevice()="+getCameraDevice()+"    getCameraCaptureSession()= "+getCameraCaptureSession()  );
        if (null == getCameraDevice() || null == getCameraCaptureSession() || null == getCaptureRequest()) {
            return;
        }
        Size mPreviewSize = getPreviewSize();
        CaptureRequest.Builder mPreviewRequestBuilder = getCaptureRequestBuilder();
        CameraCaptureSession mCaptureSession = getCameraCaptureSession();
        CameraCaptureSession.CaptureCallback mAfCaptureCallback = getCameraCaptureSessionCaptureCallback();
        Rect mActiveArraySize = getActiveArraySize();
        int mDisplayRotate = getDisplayOrientation();
        // 先取相对于view上面的坐标
        double x = event.getX();
        double y = event.getY();

        double tmp;
        int realPreviewWidth = mPreviewSize.getWidth(), realPreviewHeight = mPreviewSize.getHeight();
        if (90 == mDisplayRotate || 270 == mDisplayRotate) {
            realPreviewWidth = mPreviewSize.getHeight();
            realPreviewHeight = mPreviewSize.getWidth();
        }
        // 计算摄像头取出的图像相对于view放大了多少，以及有多少偏移
        double imgScale = 1.0f, verticalOffset = 0, horizontalOffset = 0;
        if (realPreviewHeight * viewWidth > realPreviewWidth * viewHeight) {
            imgScale = viewWidth * 1.0 / realPreviewWidth;
            verticalOffset = (realPreviewHeight - viewHeight / imgScale) / 2;
        } else {
            imgScale = viewHeight * 1.0 / realPreviewHeight;
            horizontalOffset = (realPreviewWidth - viewWidth / imgScale) / 2;
        }
        // 将点击的坐标转换为图像上的坐标
        x = x / imgScale + horizontalOffset;
        y = y / imgScale + verticalOffset;
        if (90 == mDisplayRotate) {
            tmp = x;
            x = y;
            y = mPreviewSize.getHeight() - tmp;
        } else if (270 == mDisplayRotate) {
            tmp = x;
            x = mPreviewSize.getWidth() - y;
            y = tmp;
        }
        // 计算取到的图像相对于裁剪区域的缩放系数，以及位移
        Rect cropRegion = getCaptureRequest().get(CaptureRequest.SCALER_CROP_REGION);
        if (null == cropRegion) {
            Log.e(TAG, "can't get crop region");
            cropRegion = mActiveArraySize;
        }
        int cropWidth = cropRegion.width(), cropHeight = cropRegion.height();
        if (mPreviewSize.getHeight()* cropWidth > mPreviewSize.getWidth() * cropHeight) {
            imgScale = cropHeight * 1.0 / mPreviewSize.getHeight();
            verticalOffset = 0;
            horizontalOffset = (cropWidth - imgScale * mPreviewSize.getWidth()) / 2;
        } else {
            imgScale = cropWidth * 1.0 / mPreviewSize.getWidth();
            horizontalOffset = 0;
            verticalOffset = (cropHeight - imgScale * mPreviewSize.getHeight()) / 2;
        }
        // 将点击区域相对于图像的坐标，转化为相对于成像区域的坐标
        x = x * imgScale + horizontalOffset + cropRegion.left;
        y = y * imgScale + verticalOffset + cropRegion.top;
        double tapAreaRatio = 0.1;
        Rect rect = new Rect();
        rect.left = clamp((int) (x - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.right = clamp((int) (x + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.top = clamp((int) (y - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
        rect.bottom = clamp((int) (y + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        CaptureRequest mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mAfCaptureCallback, mICameraImp.getWorkThreadManager().getBackgroundHandler());
        } catch (CameraAccessException e) {
            Log.e(TAG,  "setRepeatingRequest failed, " + e.getMessage());
        }

        if (mFocusViewController != null){
            Log.d("wangchao_focus","mFocusViewController-----------------"+mFocusViewController);
            mFocusViewController.addFocusView();
            mFocusViewController.showActiveFocusAt((int)event.getX(),(int)event.getY());
            //mFocusViewController.stopFocusAnimations();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFocusViewController.clearFocusUi();
                }
            },2000);
        }
       /* IFocusView.FocusViewState focusState = mFocusViewController.getFocusState();
        if (focusState == IFocusView.FocusViewState.STATE_ACTIVE_FOCUSED){
            mFocusViewController.clearFocusUi();
            mFocusViewController.removeFocusView();
        }*/
    }

    private int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }
    /**
     * 获取最近一次拍照的图片ID
     * @param context
     * @return
     */
    public String getRecentlyPhotoPath(Context context) {
        String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + FileUtils.DIRECTORY + "%' ";
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor = context.getContentResolver().query(
                uri, new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA,MediaStore.Files.FileColumns.SIZE}, searchPath, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            mPhotoPath =  cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return mPhotoPath;
    }
    /**
     * save Video Dialog
     */
    public void showProgress(String msg) {
        ((VideoMode) mVideoMode).getRotateProgress().showProgress(msg);
    }
    public void dismissProgress() {
        ((VideoMode) mVideoMode).getRotateProgress().hide();
    }
    public boolean isShowingProgress() {
        return ((VideoMode) mVideoMode).getRotateProgress().isShowing();
    }

}
