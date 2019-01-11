/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.example.wangchao.androidbase2fragment.view.focus;

import android.app.Activity;
import android.graphics.RectF;
import android.util.Log;
import android.widget.FrameLayout;
import com.example.wangchao.androidbase2fragment.R;
import com.example.wangchao.androidbase2fragment.app.ICameraImp;
import com.example.wangchao.androidbase2fragment.utils.camera.Camera2Utils;

/**
 * The focus controller interacts with the focus UI.
 */
public class FocusViewController {
    private static final int ORIENTATION_UNKNOWN = -1;
    private FocusView mFocusView;
    private Activity mActivity;
    private RectF mPreviewRect = new RectF();
    private ICameraImp mICameraImp;
    private FrameLayout mFeatureRootView;
    /**
     * Constructor of focus view.
     *
     * @param iCameraImp   The application app level controller
     */
    public FocusViewController(final ICameraImp iCameraImp) {
        mICameraImp = iCameraImp;
        mActivity = mICameraImp.getActivity();
        mFeatureRootView = mICameraImp.getFrameLayout();
    }

    /**
     * Show a passive focus animation at the center of the active area.
     */
    public void showPassiveFocusAtCenter() {
        Log.d("showPassive","showPassiveFocusAtCenter------------------------------>");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("showPassive","mFocusView--------------->"+mFocusView);
                if (mFocusView == null) {
                    return;
                }
                makeSureViewOnTree();
                setFocusLocation(mFeatureRootView.getWidth() / 2, mFeatureRootView.getHeight() / 2);
                mFocusView.startPassiveFocus();
                mFocusView.centerFocusLocation();
            }
        });
    }

    /**
     * Show an active focus animation at the given viewX and viewY position.
     * This is normally initiated by the user touching the screen at a given
     * point.
     * <p>
     *
     * @param viewX the view's x coordinate
     * @param viewY the view's y coordinate
     */
    public void showActiveFocusAt(final int viewX, final int viewY) {
        if (mFocusView == null) {
            return;
        }
        makeSureViewOnTree();
        setFocusLocation(viewX, viewY);
        mFocusView.setFocusLocation(viewX, viewY);
        mFocusView.startActiveFocus();
    }
    /**
     * Stop any currently executing focus animation.
     */
    public void stopFocusAnimations() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFocusView == null) {
                    return;
                }
                makeSureViewOnTree();
                Log.d("stopFocusAnimations","stopFocusAnimations---------------------------->");
                mFocusView.stopFocusAnimations();
            }
        });
    }

    public void clearFocusUi() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFocusView == null) {
                    return;
                }
                makeSureViewOnTree();
                mFocusView.clearFocusUi();
            }
        });
    }

    public void highlightFocusView() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFocusView == null) {
                    return;
                }
                makeSureViewOnTree();
                mFocusView.highlightFocusView();
            }
        });
    }
    public void lowlightFocusView() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFocusView == null) {
                    return;
                }
                makeSureViewOnTree();
                mFocusView.lowlightFocusView();
            }
        });
    }

    public void addFocusView() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("addFocusView","addFocusView-------------------->"+mFeatureRootView);
                // focus view may inflate by other instance or exposure view.
                mFocusView = (FocusView) mFeatureRootView.findViewById(R.id.focus_view);
                if (mFocusView == null) {
                    mFocusView = (FocusView) mActivity.getLayoutInflater().inflate(R.layout.focus_view, mFeatureRootView,false);
                    mFeatureRootView.addView(mFocusView);
                }
                mFocusView.setPreviewRect(mPreviewRect);
                int orientation = mFocusView.getOrientation();
                if (orientation != ORIENTATION_UNKNOWN) {
                    int compensation = Camera2Utils.getDisplayRotation(mActivity);
                    orientation = orientation + compensation;
                    Camera2Utils.rotateViewOrientation(mFocusView, orientation, false);
                } else {
                    Log.d("FocusviewC", "[addFocusView] unknown orientation");
                }
                setFocusLocation(mFeatureRootView.getWidth() / 2, mFeatureRootView.getHeight() / 2);
            }
        });
    }

    public void removeFocusView() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFocusView != null) {
                    mFeatureRootView.removeView(mFocusView);
                    mFocusView = null;
                }
            }
        });
    }
    public IFocusView.FocusViewState getFocusState() {
        if (mFocusView == null) {
            return IFocusView.FocusViewState.STATE_IDLE;
        }
        makeSureViewOnTree();
        return mFocusView.getFocusState();
    }

    public void onPreviewChanged(RectF previewRect) {
        mPreviewRect = previewRect;
        if (mFocusView != null) {
            mFocusView.setPreviewRect(previewRect);
        }
    }

    public boolean isReadyTodoFocus() {
        if (mFocusView == null) {
            return false;
        }
        if (mFocusView.getWidth() == 0 || mFocusView.getHeight() == 0) {
            return false;
        }
        return true;
    }

    public boolean isActiveFocusRunning() {
        if (mFocusView == null) {
            return false;
        }
        return mFocusView.isActiveFocusRunning();
    }

    public void setOrientation(final int orientation) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFocusView == null) {
                    return;
                }
                int compensation = Camera2Utils.getDisplayRotation(mActivity);
                final int compensationOrientation = orientation + compensation;
                Camera2Utils.rotateViewOrientation(mFocusView, compensationOrientation, true);
            }
        });
    }

    public void setFocusLocation(int x, int y) {
        Log.d("setFocusLocation","setFocusLocation----->"+x+"     mFocusViewY---"+y);
        if (mFocusView == null) {
            return;
        }
        // Use margin to set the focus indicator to the touched area.
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mFocusView.getLayoutParams();
        int left = 0;
        int top = 0;
        left = x - mFocusView.getWidth() / 2;
        top = y - mFocusView.getHeight() / 2;
        p.setMargins(left, top, 0, 0);
        mFocusView.requestLayout();
    }

    /**
     * Make sure focus view be added to root view when it is not null.
     */
    public void makeSureViewOnTree() {
        // All focus and exposure are share one View, PIP may don't ensure add-remove life cycle.
        if (mFeatureRootView.findViewById(R.id.focus_view) == null && mFocusView != null) {
            mFeatureRootView.addView(mFocusView);
        }
    }
}
