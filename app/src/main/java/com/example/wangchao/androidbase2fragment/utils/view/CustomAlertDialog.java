package com.example.wangchao.androidbase2fragment.utils.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import com.example.wangchao.androidbase2fragment.R;

public class CustomAlertDialog  extends AlertDialog {
    Context mContext;
    public CustomAlertDialog(Context context) {
        super(context, R.style.MyDialog); // 自定义全屏style
        this.mContext=context;
    }
    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity=Gravity.BOTTOM;
        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height= WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }
}
