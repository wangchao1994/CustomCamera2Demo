package com.example.wangchao.androidbase2fragment.utils.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class AnimatorBuilder {
    /**
     * 创建周期性的闪现动画
     * @param view
     * @return
     */
    public static Animator createFlashAnimator(View view){
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(view,"alpha",1.0f,0.1f);
        objectAnimator.setDuration(1000);
        //反向效果，重复的时候
      //  objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        return  objectAnimator;
    }
}
