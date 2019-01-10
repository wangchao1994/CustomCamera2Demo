package com.example.wangchao.androidbase2fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wangchao.androidbase2fragment.base.BaseActivity;

public class PictureActivity extends BaseActivity {
    public static final String TAG = PictureActivity.class.getSimpleName();
    @Override
    protected int getLayoutId() {
        return R.layout.activity_picture_show;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null & bundle.containsKey(TAG)) {
            String url = bundle.getString(TAG);
            ImageView imageView = (ImageView) findViewById(R.id.picture_show_iv);
            Glide.with(this).asBitmap().load(url).into(imageView);
        }
    }

    @Override
    protected void initDataManager() {

    }

    public static void openActivity(Context context, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(PictureActivity.TAG, url);
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void handleMsg(Message msg) {

    }
}
