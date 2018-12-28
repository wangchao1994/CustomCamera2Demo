package com.example.wangchao.androidbase2fragment;


import android.animation.Animator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.base.BaseApplication;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.utils.animator.AnimatorBuilder;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.glide.GlideLoader;
import com.example.wangchao.androidbase2fragment.utils.toast.ToastUtils;
import com.example.wangchao.androidbase2fragment.view.AutoFitTextureView;
import com.example.wangchao.androidbase2fragment.view.focus.FocusIndicatorRotateLayout;
import com.example.wangchao.androidbase2fragment.view.focus.RotateLayout;

public class CameraFragment extends Fragment implements CameraContract.CameraView<CameraContract.Presenter> ,View.OnClickListener,AutoFitTextureView.OnGestureListener {

    public static final String TAG = CameraFragment.class.getSimpleName();
    private static ICameraImp mICameraImp;
    private ImageView mCameraModeView;
    private AutoFitTextureView mAutoFitTextureView;
    private CameraContract.Presenter mCameraPresenter;
    private String mFilePath;
    private ImageView mCameraThumb;
    private TextView tv_recording_time_show;
    private ImageView mRecordingPause;
    private TextView tv_camera_photo;
    private TextView tv_camera_video;
    private ImageView iv_camera_zoom;
    private Animator flashAnimator;
    private ImageView mCameraflash;
    private ImageView mCameraSwitch;
    boolean isCameraBack = true;
    private ImageView mCameraViewSettings;
    private float zoomProportion;
    private RotateLayout mRotateLayout;
    private FocusIndicatorRotateLayout mFocusIndicatorRotateLayout;

    public CameraFragment() {

    }
    public static CameraFragment newInstance(ICameraImp iCameraImp) {
        mICameraImp = iCameraImp;
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraPresenter = mICameraImp.getCameraModePresenter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mCameraView = inflater.inflate(R.layout.fragment_camera, container, false);
        initView(mCameraView);
        return mCameraView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraPresenter != null) {
            mCameraPresenter.onResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mCameraPresenter != null) {
            mCameraPresenter.onPause();
        }
    }

    private void initView(View mCameraView) {
        mRotateLayout=(RotateLayout)mCameraView.findViewById(R.id.focus_indicator_rotate_layout);
        mFocusIndicatorRotateLayout=(FocusIndicatorRotateLayout)mRotateLayout;
        mCameraModeView = mCameraView.findViewById(R.id.iv_camera_mode);
        mAutoFitTextureView = mCameraView.findViewById(R.id.main_texture_view);
        mAutoFitTextureView.setOnGestureListener(this);
        mCameraThumb = mCameraView.findViewById(R.id.iv_thumb);
        mCameraflash = mCameraView.findViewById(R.id.iv_camera_flash);
        mCameraSwitch = mCameraView.findViewById(R.id.iv_camera_switch);
        tv_camera_photo = mCameraView.findViewById(R.id.tv_camera_mode_photo);
        tv_camera_video = mCameraView.findViewById(R.id.tv_camera_mode_video);
        iv_camera_zoom = mCameraView.findViewById(R.id.iv_camera_zoom);
        tv_recording_time_show = mCameraView.findViewById(R.id.tv_time_show_recording);
        mCameraViewSettings = mCameraView.findViewById(R.id.iv_camera_setting);
        mRecordingPause = mCameraView.findViewById(R.id.iv_recording_pause);
        //录制状态TAG
        mRecordingPause.setTag(CameraContract.CameraView.MODE_RECORD_FINISH);
        mCameraModeView.setOnClickListener(this);
        mCameraThumb.setOnClickListener(this);
        mRecordingPause.setOnClickListener(this);
        tv_camera_video.setOnClickListener(this);
        tv_camera_photo.setOnClickListener(this);
        iv_camera_zoom.setOnClickListener(this);
        mCameraflash.setOnClickListener(this);
        mCameraSwitch.setOnClickListener(this);
        mCameraViewSettings.setOnClickListener(this);
    }

    @Override
    public TextureView getCameraView() {
        return mAutoFitTextureView;
    }

    @Override
    public void loadPictureResult(String filePath) {
        mFilePath = filePath;
        GlideLoader.loadNetWorkResource(getActivity(),filePath,mCameraThumb);
    }

    @Override
    public void setTimingShow(String timing) {
        tv_recording_time_show.setText(timing);
    }

    @Override
    public void switchRecordMode(int mode) {
        switch (mode) {
            //录制开始
            case CameraContract.CameraView.MODE_RECORD_START:
                Log.d("camera_log","录制开始------------>");
                tv_recording_time_show.setVisibility(View.VISIBLE);
                tv_recording_time_show.setTextColor(Color.WHITE);
                if (flashAnimator != null && flashAnimator.isRunning()) {
                    flashAnimator.cancel();
                }
                break;
            //录制暂停
            case CameraContract.CameraView.MODE_RECORD_STOP:
                Log.d("camera_log","录制暂停------------>");
                flashAnimator = AnimatorBuilder.createFlashAnimator(tv_recording_time_show);
                flashAnimator.start();
                break;
            //录制完成
            case CameraContract.CameraView.MODE_RECORD_FINISH:
                Log.d("camera_log","录制完成------------>");
                tv_recording_time_show.setText("");
                tv_recording_time_show.setVisibility(View.GONE);
                mRecordingPause.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        mRecordingPause.setTag(mode);
    }

    @Override
    public void showToast(String content) {
        ToastUtils.showToastRunUIThread(getActivity(),content);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.iv_camera_mode:
                int cameraMode = mCameraPresenter.getCameraMode();
                Log.d("camera_log","cameraMode PhotoMode====================="+cameraMode);
                if (cameraMode == 1){
                    mRecordingPause.setVisibility(View.GONE);
                }else if (cameraMode == 2){
                    mRecordingPause.setVisibility(View.VISIBLE);
                }
                mCameraPresenter.takePictureOrVideo();
                break;
            case R.id.iv_camera_flash:
                boolean flashOpenOrClose = mICameraImp.getFlashOpenOrClose();
                Log.d("camera_log","flashOpenOrClose  mICameraImp.getFlashOpenOrClose()====="+mICameraImp.getFlashOpenOrClose());
                if (flashOpenOrClose){
                    mICameraImp.setFlashOpenOrClose(false);
                    mCameraflash.setImageResource(R.drawable.btn_flash_off_normal);
                }else{
                    mICameraImp.setFlashOpenOrClose(true);
                    mCameraflash.setImageResource(R.drawable.btn_flash_auto_normal);
                }
                break;
            case R.id.iv_camera_switch:
                if (isCameraBack){
                    mCameraPresenter.switchCameraId(1);
                    isCameraBack = false;
                    Log.d("camera_log","------------------CAMERA_FACING_FRONT isCameraBack="+isCameraBack);
                }else{
                    mCameraPresenter.switchCameraId(0);
                    isCameraBack = true;
                    Log.d("camera_log","------------------CAMERA_FACING_BACK isCameraBack="+isCameraBack);
                }
                break;
            case R.id.iv_thumb:
                if (!TextUtils.isEmpty(mFilePath)) {
                    Log.d(TAG,"mFilePath-----------------"+mFilePath);
                    //PictureActivity.openActivity(getActivity(), mFilePath);
                    //跳转系统图库
                    Camera2Utils.OnIntentGallery(getActivity(),mFilePath);
                }
                break;
            case R.id.iv_recording_pause:
                int mode = (int) mRecordingPause.getTag();
                if (mode == CameraContract.CameraView.MODE_RECORD_START) { //录制状态中，可以暂停
                    mCameraPresenter.stopRecord();
                    mRecordingPause.setImageResource(R.drawable.ic_recording_play);
                }else if (mode == CameraContract.CameraView.MODE_RECORD_STOP) {//暂停状态，可以继续开始录制
                    mCameraPresenter.restartRecord();
                    mRecordingPause.setImageResource(R.drawable.ic_recording_pause);
                }
                break;
            case R.id.tv_camera_mode_photo:
                Log.d("camera_mode","mICameraImp.getCameraMangaer().isVideoRecording()="+mICameraImp.getCameraMangaer().isVideoRecording());
                if (mICameraImp.getCameraMangaer().isVideoRecording()){
                    ToastUtils.showToast(BaseApplication.getInstance(),"请结束录像后重试!");
                }else {
                    mCameraPresenter.switchCameraMode(Camera2Utils.MODE_CAMERA);
                    tv_camera_photo.setTextColor(Color.RED);
                    tv_camera_video.setTextColor(Color.WHITE);
                }
                break;
            case R.id.tv_camera_mode_video:
                mCameraPresenter.switchCameraMode(Camera2Utils.MODE_VIDEO_RECORD);
                tv_camera_video.setTextColor(Color.RED);
                tv_camera_photo.setTextColor(Color.WHITE);
                break;
            case R.id.iv_camera_zoom:
                float zoomProportion = mICameraImp.getZoomProportion();
                if (zoomProportion == 4.0){
                    zoomProportion = 0.0f;
                }
                zoomProportion += 1.0f;
                mCameraPresenter.setZoomValues(zoomProportion);
                Log.d("camera_log","zoomProportion==camera_zoom_change==="+zoomProportion);
                break;
            case R.id.iv_camera_setting:
                initPopupWindow(v);
                break;

        }
    }
    private void initPopupWindow(View view) {
        View mPopupWindowView= LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_item, null, false);
        SwitchCompat mSwitchCompat = mPopupWindowView.findViewById(R.id.sc_camera_switch_focus);
        boolean manualFocus = mICameraImp.getManualFocus();
        if (manualFocus){
           mSwitchCompat.setChecked(false);
        }else{
            mSwitchCompat.setChecked(true);
        }
        PopupWindow mPopupWindow = new PopupWindow(mPopupWindowView,ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setAnimationStyle(R.anim.anim_pop);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.showAsDropDown(view, 100, 20);
       // mPopupWindow.showAtLocation(view,Gravity.TOP,50,50);
        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //打开自动对焦
                    mICameraImp.setManualFccus(false);
                    Log.d(TAG,"focus-------AutoFocus---------------------");
                }else{
                    //关闭自动对焦
                    mICameraImp.setManualFccus(true);
                    Log.d(TAG,"focus-------ManualFocus---------------------");
                }
            }
        });
    }




    /**点击对焦*/
    @Override
    public boolean onSingleTap(MotionEvent e) {
        if (null == mAutoFitTextureView) {
            return false;
        }
        //if (mICameraImp.getManualFocus()){
            Log.d("onSingleTap-","ManualFocus-------------------------------");
            mCameraPresenter.focusOnTouch(e, mAutoFitTextureView.getWidth(), mAutoFitTextureView.getHeight());
//            float x = e.getX();
//            float y = e.getY();
//            Log.d("single","x===="+x+"   y="+y);
//            mFocusIndicatorRotateLayout.clear();
//            mFocusIndicatorRotateLayout.showStart();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mFocusIndicatorRotateLayout.showSuccess(true);
//                }
//            },1000);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mFocusIndicatorRotateLayout.clear();
//                }
//            },1400);
        //}
        return false;
    }

    @Override
    public void showPress() {

    }

    @Override
    public void onScale(float factor) {
        Log.d("onScale-","factor-----------"+factor);
        zoomProportion = mICameraImp.getZoomProportion();
        Log.d("onScale","zoomProportion-----------zoomProportion="+zoomProportion);
        if (factor >= 1.0f){
            if (zoomProportion >=1.0f){
                zoomProportion += 0.1f;
                if (zoomProportion >4.0f){
                    zoomProportion = 4.0f;
                }
            }
        }else{
            if (zoomProportion >1.0f){
                zoomProportion -= 0.1f;
                if (zoomProportion < 1.0f){
                    zoomProportion = 1.0f;
                }
            }
        }
        mCameraPresenter.setZoomValues(zoomProportion);
    }
    @Override
    public void onLongPress() {

    }

    @Override
    public void onActionUp() {
    }
}
