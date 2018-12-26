package com.example.wangchao.androidbase2fragment.imp;

import com.example.wangchao.androidbase2fragment.app.ICameraImp;

public interface ICameraMode {
    /**
     * Define the mode type to distinguish mode is photo mode
     * or video mode.
     */
    enum ModeType {
        PHOTO,
        VIDEO,
    }

    void init(ICameraImp iCameraImp);

    void resume();

    void pause();

    void unInit();
}
