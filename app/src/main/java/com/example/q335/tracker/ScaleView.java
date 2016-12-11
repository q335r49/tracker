package com.example.q335.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ScaleView extends View {
    CalendarWin CV;
        public void setCV(CalendarWin CV) { this.CV = CV; }
    ScaleListener SL;
    ScaleGestureDetector mScaleDetector;

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
        super.onDraw(canvas);
        CV.draw(canvas); //TODO: Investigate why draw is happening multiple times
    }

    private float mLastTouchX=-1;
    private float mLastTouchY=-1;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        float x = ev.getX();
        float y = ev.getY();
        if (ev.getAction() == MotionEvent.ACTION_MOVE && (Math.abs(x-mLastTouchX) + Math.abs(y-mLastTouchY) < 150)) {
            CV.shiftWindow(x-mLastTouchX,y-mLastTouchY);
            invalidate();
        }
        mLastTouchX = x;
        mLastTouchY = y;
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

