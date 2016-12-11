package com.example.q335.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ScaleView extends View {
    private static final String LOG_FILE = "log.txt";  //TODO: Make it a passed parameter?
    CalendarWin CV;
    ScaleListener SL;
    ScaleGestureDetector mScaleDetector;
    Context appContext;

    public static String MESS_RELOAD_LOG = "IM#RELOAD LOG";
    public void procMess(String s) {
        if (s == MESS_RELOAD_LOG) {
            loadCalendarView(appContext);
        } else {
            CV.loadEntry(s);
            invalidate();
        }
    }

    public ScaleView(Context context) {
        super(context);
        SL = new ScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, SL);
        loadCalendarView(context);
        appContext = context;
    }
    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SL = new ScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, SL);
        loadCalendarView(context);
        appContext = context;
    }
    public static List<String> read_file(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            ArrayList<String> sb = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.add(line);
            }
            return sb;
        } catch (FileNotFoundException e) {
            Log.e("tracker:","Log file not found!");
            return null;
        } catch (UnsupportedEncodingException e) {
            Log.e("tracker:","Log file bad encoding!");
            return null;
        } catch (IOException e) {
            Log.e("tracker:","Log file IO exception!");
            return null;
        }
    }
    public void loadCalendarView(Context context) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis()-86400000L*7);
        cal.set(Calendar.DAY_OF_WEEK,1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        CV = new CalendarWin(cal.getTimeInMillis()/1000,-1,-1,10,4);
        CV.log_to_shapes(read_file(context.getApplicationContext(), LOG_FILE));
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

