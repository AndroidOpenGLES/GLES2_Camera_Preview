package com.le.xiaxl;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private MainGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.activity_main);

        //初始化GLSurfaceView
        mGLSurfaceView = (MainGLSurfaceView) findViewById(R.id.main_gl_surfaceview);
        mGLSurfaceView.requestFocus();//获取焦点
        mGLSurfaceView.setFocusableInTouchMode(true);//设置为可触控

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLSurfaceView.onDestroy();
    }
}