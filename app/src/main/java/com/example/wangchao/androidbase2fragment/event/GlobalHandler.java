package com.example.wangchao.androidbase2fragment.event;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 全局Handler消息处理
 */
public class GlobalHandler extends Handler {
    private HandleMsgListener listener;
    private static final String TAG = GlobalHandler.class.getSimpleName();
    private static GlobalHandler instance;
    private GlobalHandler(){
    }

    public static GlobalHandler getInstance(){
        if(instance == null){
            synchronized(GlobalHandler.class){
                if(instance == null){
                    instance = new GlobalHandler();
                }
            }
        }
        return instance;
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (getHandleMsgListener() != null){
            getHandleMsgListener().handleMsg(msg);
        }else {
            Log.e(TAG,"请传入HandleMsgListener对象");
        }
    }

    public interface HandleMsgListener{
        void handleMsg(Message msg);
    }

    public void setHandleMsgListener(HandleMsgListener listener){
        this.listener = listener;
    }

    public HandleMsgListener getHandleMsgListener(){
        return listener;
    }
}
