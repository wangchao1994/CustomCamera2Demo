package com.example.wangchao.androidbase2fragment.view.focus;
public interface FocusIndicator {
    void needDistanceInfoShow(boolean needShow);
    void showStart();
    void showSuccess(boolean timeout);
    void showFail(boolean timeout);
    void clear();
}
