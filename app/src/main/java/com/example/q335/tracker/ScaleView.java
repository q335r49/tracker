package com.example.q335.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ScaleView extends View {
    CalendarWin CV;
    ScaleListener SL;
    ScaleGestureDetector mScaleDetector;
    public void setCV(CalendarWin CV) { this.CV = CV; }

    public ScaleView(Context context) {
        super(context);
        SL = new ScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, SL);
    }
    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SL = new ScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, SL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //TODO: Investigate why draw is happening multiple times
        super.onDraw(canvas);
        CV.updateCanvas(canvas.getWidth(), canvas.getHeight());
        CV.draw(canvas);
    }

    private float mLastTouchX=-1;
    private float mLastTouchY=-1;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x-mLastTouchX) + Math.abs(y-mLastTouchY) < 150)
                    CV.shiftWindow(x-mLastTouchX,y-mLastTouchY);
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            case MotionEvent.ACTION_UP:
                //CV.setStatusText("X:" + eventX + " Y:" + eventY);
                break;
        }
        invalidate();
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            CV.reScale(detector.getScaleFactor(),detector.getFocusX(),detector.getFocusY());
            return true;
        }
    }
}

