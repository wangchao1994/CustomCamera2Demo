package com.example.wangchao.androidbase2fragment.app;


import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;

import com.example.wangchao.androidbase2fragment.device.CameraMangaer;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.utils.thread.WorkThreadManager;

public interface ICameraImp {
    CameraMangaer getCameraMangaer();
    WorkThreadManager getWorkThreadManager();
    void setFlashOpenOrClose(boolean values);
    boolean getFlashOpenOrClose();
    float getZoomProportion();
    CameraContract.Presenter getCameraModePresenter();
    void setManualFccus(boolean isManualFocus);
    boolean getManualFocus();
    CameraDevice getCameraDevice();
    CameraCaptureSession getCameraCaptureSession();
    CaptureRequest getCaptureRequest();
    CaptureRequest.Builder getCaptureRequestBuilder();
}
