package com.example.wangchao.androidbase2fragment;
import android.app.Activity;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.TextureView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseActivity;
import com.example.wangchao.androidbase2fragment.device.CameraMangaer;
import com.example.wangchao.androidbase2fragment.event.GlobalAction;
import com.example.wangchao.androidbase2fragment.event.GlobalHandler;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.persenter.CameraPresenter;
import com.example.wangchao.androidbase2fragment.utils.thread.WorkThreadManager;
import com.example.wangchao.androidbase2fragment.view.AutoFitTextureView;

public class CameraActivity extends BaseActivity implements ICameraImp {
    public static final String TAG = CameraActivity.class.getSimpleName();
    private CameraFragment cameraFragment;
    private CameraMangaer mCameraManager;
    private WorkThreadManager mWorkThreadManager;
    private CameraContract.Presenter  mCameraPresenter;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera;
    }
    @Override
    protected void initView(Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            cameraFragment = (CameraFragment) getSupportFragmentManager().findFragmentByTag(CameraFragment.TAG);
            if (cameraFragment == null) {
                cameraFragment = CameraFragment.newInstance(this);
                getSupportFragmentManager().beginTransaction().add(R.id.container, cameraFragment, CameraFragment.TAG).commitAllowingStateLoss();
            }
            mCameraPresenter = new CameraPresenter(cameraFragment,this);
        }
    }
    @Override
    protected void initDataManager() {
        mCameraManager = new CameraMangaer(this);
        mWorkThreadManager = WorkThreadManager.newInstance();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public CameraMangaer getCameraManager() {
        if (mCameraManager != null){
            return mCameraManager;
        }
        return null;
    }

    @Override
    public WorkThreadManager getWorkThreadManager() {
        if (mWorkThreadManager != null){
            return mWorkThreadManager;
        }
        return null;
    }
    @Override
    public void setFlashOpenOrClose(boolean values) {
        if (mCameraManager != null){
            mCameraManager.setFlashOnOrClose(values);
        }
    }

    @Override
    public boolean getFlashOpenOrClose() {
        if (mCameraManager != null){
            return  mCameraManager.getFlashOnOrClose();
        }
        return true;
    }

    @Override
    public float getZoomProportion() {
        if (mCameraManager != null){
            return mCameraManager.getZoomProportion();
        }
        return 0;
    }

    @Override
    public CameraContract.Presenter getCameraModePresenter() {
        return mCameraPresenter;
    }

    @Override
    public void setManualFocus(boolean isManualFocus) {
        if (mCameraManager != null){
            mCameraManager.setManualFocus(isManualFocus);
        }
    }

    @Override
    public boolean getManualFocus() {
        if (mCameraManager != null){
            return mCameraManager.getMaunalFocus();
        }
        return false;
    }

    @Override
    public CameraDevice getCameraDevice() {
        if (mCameraManager != null){
            return mCameraManager.getCameraDevice();
        }
        return null;
    }

    @Override
    public CameraCaptureSession getCameraCaptureSession() {
        if (mCameraManager != null){
            return mCameraManager.getCameraCaptureSession();
        }
        return null;
    }

    @Override
    public CaptureRequest getCaptureRequest() {
        if (mCameraManager != null){
            return mCameraManager.getCaptureRequest();
        }
        return null;
    }

    @Override
    public CaptureRequest.Builder getCaptureRequestBuilder() {
        if (mCameraManager != null){
            return mCameraManager.getCaptureRequestBuilder();
        }
        return null;
    }

    @Override
    public GlobalHandler getGlobalHandler() {
        return globalHandler;
    }

    @Override
    public TextureView getCameraView() {
        if (cameraFragment != null){
            return cameraFragment.getCameraView();
        }
        return null;
    }

    @Override
    public FrameLayout getFrameLayout() {
        if (cameraFragment != null){
            return cameraFragment.getMainCameraLayout();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mCameraPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void handleMsg(Message msg) {
        switch (msg.what){
            case GlobalAction.SAVE_VIDEO_DIALOG_SHOW:
                mCameraManager.showProgress(getResources().getString(R.string.save_video));
                break;
            case GlobalAction.SAVE_VIDEO_DIALOG_DISMISS:
                if(mCameraManager.isShowingProgress()){
                    mCameraManager.dismissProgress();
                }
                break;
        }
    }
}