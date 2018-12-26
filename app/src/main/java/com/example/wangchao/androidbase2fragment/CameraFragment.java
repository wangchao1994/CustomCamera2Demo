package com.example.wangchao.androidbase2fragment;


import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.imp.CameraContract;
import com.example.wangchao.androidbase2fragment.utils.animator.AnimatorBuilder;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;
import com.example.wangchao.androidbase2fragment.utils.glide.GlideLoader;
import com.example.wangchao.androidbase2fragment.utils.toast.ToastUtils;
import com.example.wangchao.androidbase2fragment.view.AutoFitTextureView;

public class CameraFragment extends Fragment implements CameraContract.CameraView<CameraContract.Presenter> ,View.OnClickListener{

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
        mCameraModeView = mCameraView.findViewById(R.id.iv_camera_mode);
        mAutoFitTextureView = mCameraView.findViewById(R.id.main_texture_view);
        mCameraThumb = mCameraView.findViewById(R.id.iv_thumb);
        mCameraflash = mCameraView.findViewById(R.id.iv_camera_flash);
        mCameraSwitch = mCameraView.findViewById(R.id.iv_camera_switch);
        tv_camera_photo = mCameraView.findViewById(R.id.tv_camera_mode_photo);
        tv_camera_video = mCameraView.findViewById(R.id.tv_camera_mode_video);
        iv_camera_zoom = mCameraView.findViewById(R.id.iv_camera_zoom);
        tv_recording_time_show = mCameraView.findViewById(R.id.tv_time_show_recording);
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
                    PictureActivity.openActivity(getActivity(), mFilePath);
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
                mCameraPresenter.switchCameraMode(Camera2Utils.MODE_CAMERA);
                tv_camera_photo.setTextColor(Color.RED);
                tv_camera_video.setTextColor(Color.WHITE);
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

        }
    }
}
