package com.example.q335.tracker;


import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.opengles.GL10;

public class GrapherActivity extends Activity {
    private com.example.q335.tracker.TrackerGrapher mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grapher);

        mGLView = new com.example.q335.tracker.TrackerGrapher(this);
        setContentView(mGLView);
    }
}

class TrackerGrapher extends GLSurfaceView {
    private final com.example.q335.tracker.TrackerRenderer mRenderer;

    public TrackerGrapher(Context context){
        super(context);

        setEGLContextClientVersion(2);
        mRenderer = new com.example.q335.tracker.TrackerRenderer();
        setRenderer(mRenderer);
    }
}

class TrackerRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}