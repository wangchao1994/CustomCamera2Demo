package com.example.wangchao.androidbase2fragment;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseActivity;
import com.example.wangchao.androidbase2fragment.device.CameraMangaer;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.persenter.CameraPresenter;
import com.example.wangchao.androidbase2fragment.utils.thread.WorkThreadManager;

public class CameraActivity extends BaseActivity implements ICameraImp {
    public static final String TAG = CameraActivity.class.getSimpleName();
    private CameraFragment cameraFragment;
    private CameraMangaer mCameraMangaer;
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
        mCameraMangaer = new CameraMangaer(this);
        mWorkThreadManager = WorkThreadManager.newInstance();
    }

    @Override
    public CameraMangaer getCameraMangaer() {
        if (mCameraMangaer != null){
            return mCameraMangaer;
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
        if (mCameraMangaer != null){
            mCameraMangaer.setFlashOnOrClose(values);
        }
    }

    @Override
    public boolean getFlashOpenOrClose() {
        if (mCameraMangaer != null){
            return  mCameraMangaer.getFlashOnOrClose();
        }
        return true;
    }

    @Override
    public float getZoomProportion() {
        if (mCameraMangaer != null){
            return mCameraMangaer.getZoomProportion();
        }
        return 0;
    }

    @Override
    public CameraContract.Presenter getCameraModePresenter() {
        if (mCameraPresenter != null){
            return mCameraPresenter;
        }
        return null;
    }

    @Override
    public void setManualFccus(boolean isManualFocus) {
        if (mCameraMangaer != null){
            mCameraMangaer.setManualFocus(isManualFocus);
        }
    }

    @Override
    public boolean getManualFocus() {
        if (mCameraMangaer != null){
            return mCameraMangaer.getMaunalFocus();
        }
        return false;
    }

    @Override
    public CameraDevice getCameraDevice() {
        if (mCameraMangaer != null){
            return mCameraMangaer.getCameraDevice();
        }
        return null;
    }

    @Override
    public CameraCaptureSession getCameraCaptureSession() {
        if (mCameraMangaer != null){
            return mCameraMangaer.getCameraCaptureSession();
        }
        return null;
    }

    @Override
    public CaptureRequest getCaptureRequest() {
        if (mCameraMangaer != null){
            return mCameraMangaer.getCaptureRequest();
        }
        return null;
    }

    @Override
    public CaptureRequest.Builder getCaptureRequestBuilder() {
        if (mCameraMangaer != null){
            return mCameraMangaer.getCaptureRequestBuilder();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mCameraPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}