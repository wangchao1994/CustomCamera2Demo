package com.example.wangchao.androidbase2fragment.device;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.view.TextureView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseApplication;
import com.example.wangchao.androidbase2fragment.mode.CameraModeBase;
import com.example.wangchao.androidbase2fragment.mode.PhotoMode;
import com.example.wangchao.androidbase2fragment.mode.VideoMode;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.toast.ToastUtils;

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
    public CameraMangaer(ICameraImp iCameraImp) {
        mICameraImp = iCameraImp;
        mPhotoMode = new PhotoMode(mICameraImp);
        mVideoMode = new VideoMode(mICameraImp);
        //设置当前CameraId
        setCurrentCameraDirection(currentCameraDirection);
        mCurrentMode = mPhotoMode;//默认拍照模式
        isManualFocus = true;//默认手动对焦
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
        mCurrentMode.stopOperate();
    }
    public void takePictureOrVideo() {
        mCurrentMode.cameraClick();
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
    /**
     * 获取缩放比例值
     * @return
     */
    public float getZoomProportion() {
        return zoomProportion;//默认1.0f
    }

    /**
     * 设置对焦类型
     * @param mIsManualFocus
     */
    public void setManualFocus(boolean mIsManualFocus){
        isManualFocus = mIsManualFocus;
    }
    public boolean getMaunalFocus(){
        return isManualFocus;
    }

}
