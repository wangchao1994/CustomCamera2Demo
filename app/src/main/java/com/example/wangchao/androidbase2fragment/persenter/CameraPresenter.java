package com.example.wangchao.androidbase2fragment.persenter;

import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseApplication;
import com.example.wangchao.androidbase2fragment.device.CameraMangaer;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.mode.CameraModeBase;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.glide.GlideLoader;
import com.example.wangchao.androidbase2fragment.utils.permission.PermissionsManager;
import com.example.wangchao.androidbase2fragment.utils.thread.WorkThreadManager;
import com.example.wangchao.androidbase2fragment.utils.time.TimingUtils;
import com.example.wangchao.androidbase2fragment.utils.toast.ToastUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class CameraPresenter implements CameraContract.Presenter, CameraModeBase.Camera2ResultCallBack, CameraModeBase.Camera2VideoRecordCallBack{
    private CameraMangaer mCameraMangaer;
    private WorkThreadManager mWorkThreadManager;
    private ICameraImp mICameraImp;
    private CompositeSubscription compositeSubscription;
    private CameraContract.CameraView mCameraView;
    private long time = 0;
    private Subscription cycleTimeSubscription;
    private int mCurrenMode;

    public CameraPresenter(CameraContract.CameraView cameraView,ICameraImp iCameraImp){
        mICameraImp = iCameraImp;
        mCameraView = cameraView;
        compositeSubscription = new CompositeSubscription();
        mCameraMangaer = mICameraImp.getCameraManager();
        mWorkThreadManager = mICameraImp.getWorkThreadManager();
        mCameraMangaer.setCamera2ResultCallBack(this);
        mCameraMangaer.setCameraVideoCallBack(this);
        mCurrenMode = Camera2Utils.MODE_CAMERA;//默认拍照模式
    }

    @Override
    public void onResume() {
        mWorkThreadManager.startWorkThread();
        if (mCameraView.getCameraView() != null){
            mCameraMangaer.setTextureView(mCameraView.getCameraView());
        }
    }

    @Override
    public void onPause() {
        mCameraMangaer.onPause();
        mWorkThreadManager.stopBackgroundThread();
    }

    @Override
    public void takePictureOrVideo() {
        mCameraMangaer.takePictureOrVideo();
    }

    @Override
    public void switchCameraMode(int mode) {
        if (mode == mCurrenMode) {
            return;
        }
        mCurrenMode = mode;
        switch (mCurrenMode) {
            //切换到拍照模式
            case Camera2Utils.MODE_CAMERA:
                //mCameraView.showToast("正在切换到拍照模式");
                break;
            //切换到录像模式
            case Camera2Utils.MODE_VIDEO_RECORD:
                //mCameraView.showToast("正在切换到录像模式");
                break;
            default:
                break;
        }
       mCameraMangaer.switchCameraMode(mode);
    }

    @Override
    public void switchCameraId(int direction) {
        mCameraMangaer.switchCameraDirection(direction);
    }

    @Override
    public int getCameraId() {
        return mCameraMangaer.getCameraId();
    }

    @Override
    public void stopRecord() {
        if (cycleTimeSubscription != null) {
            compositeSubscription.remove(cycleTimeSubscription);
        }
        mCameraView.switchRecordMode(CameraContract.CameraView.MODE_RECORD_STOP);
        mCameraMangaer.pauseVideoRecord();
    }

    @Override
    public void restartRecord() {
        mCameraMangaer.takePictureOrVideo();
    }

    @Override
    public void setZoomValues(float focusProportion) {
        mCameraMangaer.setZoomProportion(focusProportion);
    }

    @Override
    public int getCameraMode() {
        return mCurrenMode;
    }

    @Override
    public void focusOnTouch(MotionEvent event, int viewWidth, int viewHeight) {
        mCameraMangaer.setFocusOnTouchEvent(event,viewWidth,viewHeight);
    }

    @Override
    public void onReleaseMediaRecord() {
        mCameraMangaer.onReleaseMediaRecord();
    }

    @Override
    public void setRecentlyPhotoPath(String filePath) {
        ImageView cameraThumbView = mCameraView.getCameraThumbView();
        if (cameraThumbView != null){
            GlideLoader.loadNetWorkResource(BaseApplication.getInstance(),filePath,cameraThumbView);
        }
    }

    @Override
    public void callBack(Observable<String> result) {
        if (result != null) {
            Subscription subscription = result.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String filePath) {
                            //通知图库，用于刷新
                            Camera2Utils.sendBroadcastNotify(BaseApplication.getInstance(), filePath);
                            mCameraView.loadPictureResult(filePath);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            //写入图片到磁盘失败
                            ToastUtils.showToast(BaseApplication.getInstance(), "写入磁盘失败");
                        }
                    });
            compositeSubscription.add(subscription);
        }
    }

    @Override
    public void startRecord() {
        mCameraView.switchRecordMode(CameraContract.CameraView.MODE_RECORD_START);
        cycleTimeSubscription = Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.computation())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        time += 1000;
                        String time_show = TimingUtils.getDate(time);
                        Log.d("camera_log","startRecord---------="+time_show);
                        mCameraView.setTimingShow(time_show);
                    }
                });
        compositeSubscription.add(cycleTimeSubscription);
    }

    @Override
    public void finishRecord() {
        mCameraView.switchRecordMode(CameraContract.CameraView.MODE_RECORD_FINISH);
        if (cycleTimeSubscription != null) {
            compositeSubscription.remove(cycleTimeSubscription);
        }
        time = 0;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.CAMERA_REQUEST_CODE:
                //权限请求失败
                if (grantResults.length == PermissionsManager.CAMERA_REQUEST.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            ToastUtils.showToast(BaseApplication.getInstance(), "拍照权限被拒绝");
                            break;
                        }
                    }
                }
                break;
            case PermissionsManager.VIDEO_REQUEST_CODE:
                if (grantResults.length == PermissionsManager.VIDEO_PERMISSIONS.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            ToastUtils.showToast(BaseApplication.getInstance(), "录像权限被拒绝");
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
