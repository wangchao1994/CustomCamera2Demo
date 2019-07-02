package com.example.wangchao.androidbase2fragment.app;


import android.app.Activity;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.example.wangchao.androidbase2fragment.device.CameraMangaer;
import com.example.wangchao.androidbase2fragment.event.GlobalHandler;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.utils.thread.WorkThreadManager;
import com.example.wangchao.androidbase2fragment.view.AutoFitTextureView;

public interface ICameraImp {
    Activity getActivity();
    CameraMangaer getCameraManager();
    WorkThreadManager getWorkThreadManager();
    void setFlashOpenOrClose(boolean values);
    boolean getFlashOpenOrClose();
    float getZoomProportion();
    CameraContract.Presenter getCameraModePresenter();
    void setManualFocus(boolean isManualFocus);
    boolean getManualFocus();
    CameraDevice getCameraDevice();
    CameraCaptureSession getCameraCaptureSession();
    CaptureRequest getCaptureRequest();
    CaptureRequest.Builder getCaptureRequestBuilder();
    GlobalHandler getGlobalHandler();
    TextureView getCameraView();
    FrameLayout getFrameLayout();
}
