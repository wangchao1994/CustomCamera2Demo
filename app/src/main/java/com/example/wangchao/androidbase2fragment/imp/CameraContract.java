package com.example.wangchao.androidbase2fragment.imp;


import android.view.MotionEvent;
import android.view.TextureView;
import android.widget.ImageView;

public interface CameraContract {

    interface Presenter {

        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

        void onResume();

        void onPause();

        void takePictureOrVideo();

        void switchCameraMode(int mode);

        void switchCameraId(int direction);

        int getCameraId();

        void stopRecord();

        void restartRecord();

        void setZoomValues(float focusProportion);

        int getCameraMode();

        void focusOnTouch(MotionEvent event, int viewWidth, int viewHeight);

        void onReleaseMediaRecord();

        void setRecentlyPhotoPath(String filePath);
    }

    interface CameraView<T extends Presenter> {
        /**
         * 获取TextureView
         * @return
         */
        TextureView getCameraView();
        /**
         * 获取TextureView
         * @return
         */
        ImageView getCameraThumbView();
        /**
         * 加载拍照的图片路径
         *
         * @param filePath
         */
        void loadPictureResult(String filePath);

        /**
         * 显示计时时间
         * @param timing
         */
        void setTimingShow(String timing);

        /**
         *切换到录制状态
         * @param  mode
         *
         */
        void switchRecordMode(int mode);
        /**
         * toast提示
         * @param content
         */
        void  showToast(String content);
        /**
         * 视频录制的三种状态,开始，停止，完成
         */
        int MODE_RECORD_START=1;
        int MODE_RECORD_STOP=2;
        int MODE_RECORD_FINISH=3;

    }
}