package com.example.wangchao.androidbase2fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import com.example.wangchao.androidbase2fragment.event.GlobalHandler;

public abstract class BaseActivity extends AppCompatActivity implements GlobalHandler.HandleMsgListener {

    protected GlobalHandler globalHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        globalHandler = GlobalHandler.getInstance();
        globalHandler.setHandleMsgListener(this);
        setSystemUIChange();
        initDataManager();
        initView(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setStickyStyle(getWindow()); //焦点改变 Home键退出->进入
    }
    /**
     *  获取布局的Id
     * @return
     */
    protected abstract int getLayoutId();
    /**
     * 初始化
     * @param savedInstanceState
     */
    protected abstract void initView(Bundle savedInstanceState);
    /**
     * 获取相关Manager
     */
    protected abstract void initDataManager();

    /**
     * 监听系统UI的显示，进行特殊处理
     */
    private void setSystemUIChange() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                //当系统UI显示的时候，再次隐藏
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    setStickyStyle(getWindow());
                }
            }
        });
    }
    /**
     * 隐藏NavigatoinBar和StatusBar
     * @param window
     */
    public void setStickyStyle(Window window){
        int flag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        window.getDecorView().setSystemUiVisibility(flag);
    }
}
