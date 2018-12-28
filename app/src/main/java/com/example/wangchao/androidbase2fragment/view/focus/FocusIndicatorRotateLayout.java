package com.example.wangchao.androidbase2fragment.view.focus;

import java.text.DecimalFormat;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.example.wangchao.androidbase2fragment.R;

@SuppressLint("NewApi")
public class FocusIndicatorRotateLayout extends RotateLayout implements FocusIndicator {
    private static final String TAG = "Focus_RotateLayout";
    private int mState;
    private static final int STATE_IDLE = 0;
    private static final int STATE_FOCUSING = 1;
    private static final int STATE_FINISHING = 2;
    private static final int MAX_VALUE = 9999;
    
    private Runnable mDisappear = new Disappear();
    private Runnable mEndAction = new EndAction();
    private static final int SCALING_UP_TIME = 1000;
    private static final int SCALING_DOWN_TIME = 200;
    private static final int DISAPPEAR_TIMEOUT = 200;
    
    private boolean mIsNeedShow;
    
    private String mInfo;
    private TextView mDistanceMeasurementChild;
    private Handler mHandler;

    public FocusIndicatorRotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setDistanceInfo(String info) {
        mInfo = info;
    }
    
    public void needDistanceInfoShow(boolean needShow) {
        mIsNeedShow = needShow;
    }
    
    @Override
    protected void onFinishInflate() {
        mDistanceMeasurementChild = (TextView)findViewById(R.id.distance_info);
        super.onFinishInflate();
    }
    private void setDrawable(int resid) {
        mChild.setBackgroundDrawable(getResources().getDrawable(resid));
        if (resid == R.drawable.gy_aperture_focusing && mIsNeedShow) {
            mDistanceMeasurementChild.setText(formatInfo());
            mInfo = null;
            mIsNeedShow = false;
        }
    }
    
    private String formatInfo() {
        DecimalFormat df = new DecimalFormat("0.0");
        if (mInfo != null) {
            if (Integer.valueOf(mInfo) > MAX_VALUE) {
                return null;
            }
            if (Integer.valueOf(mInfo) < 100) {
                mInfo = mInfo + "CM";
            } else {
                mInfo = df.format((float)Integer.valueOf(mInfo) / 100) + "M";
            }
        }
        Log.d(TAG, "formatInfo info= " + mInfo);
        return mInfo;
    }
    

    @SuppressLint("NewApi")
	public void showStart() {
        Log.d(TAG, "showStart mState = " + mState);
        if (mState == STATE_IDLE) {
            //setDrawable(R.drawable.gy_aperture_focusing);
            setDrawable(R.drawable.gy_aperture_focused);
            animate().withLayer().setDuration(300).scaleX(1.4f).scaleY(1.4f);
            mState = STATE_FOCUSING;
        }
    }
    
    @SuppressLint("NewApi")
	public void showSuccess(boolean timeout) {
        Log.d(TAG, "showSuccess mState = " + mState);
        if (mState == STATE_FOCUSING) {
            setDrawable(R.drawable.gy_aperture_focused);
            animate().withLayer().setDuration(100).scaleX(1f).scaleY(1f)
                   .withEndAction(timeout ? mEndAction : null);
            mState = STATE_FINISHING;
        }
    }
    
    public void showFail(boolean timeout) {
        Log.d(TAG, "showFail mState = " + mState);
        if (mState == STATE_FOCUSING) {
            setDrawable(R.drawable.gy_aperture_focusing);
            animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f).scaleY(1f)
                    .withEndAction(timeout ? mEndAction : null);
            mState = STATE_FINISHING;
        }
    }
    
    @SuppressLint("NewApi")
	public void clear() {
        Log.d(TAG, "clear mState = " + mState);
        if(mState == STATE_FINISHING){
		animate().cancel();
		removeCallbacks(mDisappear);
		mDisappear.run();
		setScaleX(1f);
		setScaleY(1f);
        }
    }
    
    private class EndAction implements Runnable {
        public void run() {
            postDelayed(mDisappear, DISAPPEAR_TIMEOUT);
        }
    }
    
    private class Disappear implements Runnable {
        public void run() {
            Log.d(TAG, "Disappear run mState = " + mState);
            mChild.setBackgroundDrawable(null);
            mDistanceMeasurementChild.setText("");
            mState = STATE_IDLE;
        }
    }
    
    public boolean isFocusing() {
        return mState != STATE_IDLE;
    }
    
    @Override
    public boolean gatherTransparentRegion(Region region) {
        Log.i("faceView", "gatherTransparentRegion = " + region);
        if (region != null) {
            final int[] location = new int[2];
            int width = getWidth();
            int height = getHeight();
            getLocationInWindow(location);
            int l = location[0] + width / 2 - width;
            int t = location[1] + height / 2 - height;
            int r = l + width * 2;
            int b = t + height * 2;
            region.op(l, t, r, b, Region.Op.DIFFERENCE);
        }
        return true;
    }
}
