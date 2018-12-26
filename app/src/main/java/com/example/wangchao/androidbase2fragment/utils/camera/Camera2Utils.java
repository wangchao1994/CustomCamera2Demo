package com.example.wangchao.androidbase2fragment.utils.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.net.Uri;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;

import com.example.wangchao.androidbase2fragment.mode.CompareSizesByArea;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Camera2Utils {

    private static final String TAG = Camera2Utils.class.getSimpleName();
    /**
     * 拍照模式
     */
    public static final  int  MODE_CAMERA = 1;
    /**
     * 录像模式
     */
    public static final  int MODE_VIDEO_RECORD =2;

    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth,  int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
    /**
     * 计算合适的大小Size,在相机拍照
     *
     * @param choices
     * @param textureViewWidth
     * @param textureViewHeight
     * @param maxWidth
     * @param maxHeight
     * @param aspectRatio
     * @return
     */
    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight,
                                         int maxWidth, int maxHeight, Size aspectRatio,CompareSizesByArea compareSizesByArea) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        // Pick the smallest of those big enough. If there is no one big enough, pick the largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, compareSizesByArea);
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough,compareSizesByArea);
        } else {
            Log.e(" 计算结果", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation 屏幕的方向
     * @return JPEG的方向(例如：0,90,270,360)
     */
    public static int getOrientation(SparseIntArray ORIENTATIONS, int mSensorOrientation, int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }
    /**
     * 检查是否支持设备自动对焦
     * <p>
     * @param characteristics
     * @return
     */
    public static boolean checkAutoFocus(CameraCharacteristics characteristics) {
        int[] afAvailableModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        if (afAvailableModes.length == 0 || (afAvailableModes.length == 1 && afAvailableModes[0] == CameraMetadata.CONTROL_AF_MODE_OFF)) {
            return  false;
        } else {
            return  true;
        }
    }
    /**
     * 匹配指定方向的摄像头，前还是后
     *
     * LENS_FACING_FRONT是前摄像头标志
     * @param cameraCharacteristics
     * @param direction
     * @return
     */
    public static  boolean matchCameraDirection(CameraCharacteristics cameraCharacteristics,int direction){
        //设置后摄像头
        Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
        return  (facing != null && facing == direction)?true:false;
    }

    public  static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }
    /**
     * 计算合适的大小，在视频录制
     * @param choices
     * @param width
     * @param height
     * @param aspectRatio
     * @return
     */
    public  static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    /**
     * 计算zoom所对应的rect
     * @param originReact 相机原始的rect
     * @param currentZoom 当前的zoom值
     * @return
     */
    public static Rect createZoomRect(Rect originReact, float currentZoom){
        Rect zoomRect=null;
        try {
            if (originReact==null){
                return zoomRect;
            }else{
                float ratio=(float)1/currentZoom;
                int cropWidth=originReact.width()-Math.round((float)originReact.width() * ratio);
                int cropHeight=originReact.height() - Math.round((float)originReact.height() * ratio);
                zoomRect = new Rect(cropWidth/2, cropHeight/2, originReact.width() - cropWidth/2, originReact.height() - cropHeight/2);
            }
        }catch (Exception e){
            e.printStackTrace();
            zoomRect=null;
        }
        return zoomRect;
    }
    /**
     * 获取最大的数字变焦值，也就是缩放值
     * @param cameraCharacteristics
     * @return
     */
    public static  Float getMaxZoom(CameraCharacteristics cameraCharacteristics){
        Float maxZoom=null;
        try {
            maxZoom= cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            Log.d("getMaxZoom","getMaxZoom maxZoom==="+maxZoom);
        }catch (Exception e){
            e.printStackTrace();
        }
        return maxZoom;
    }
    /**
     * 通知图库更新图片
     * @param context
     * @param filePath
     */
    public static  void sendBroadcastNotify(Context context, String filePath){
        //扫描指定文件
        String action=Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;
        //生成问价路径对应的uri
        Uri uri=Uri.fromFile(new File(filePath));
        Intent intent=new Intent(action);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
}
