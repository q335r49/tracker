package com.example.q335.tracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GrapherActivity extends Activity {
    private CalendarView CV;
    //TODO: Log syntax: [HEADING]>Label|Color|Pos|comment
    //TODO: month, day, labels
    //TODO: labels on tapping
    //TODO: further testing
    //TODO: multi-day activities
    //TODO: further testing
    //TODO: drag

    public List<String> TestLog = Arrays.asList(
        "1421299830>1-15-2015 5:30:30>s|L1|red|comment",
        "1421303400>1-15-2015 6:30:00>s|L1|blue|comment",
        "1421314200>1-15-2015 9:30:00>s|L2|green|com",
        "1421319600>1-15-2015 11:00:00>s|L3|grey|com",
        "1421319600>1-15-2015 11:00:00>s|L4|red|comment"
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grapher);

        CV = new CalendarView(1421280000L,-1,-1,10,4);
        CV.processLog(TestLog);
        setContentView(new MainView(this));
    }
    private class MainView extends View {
        public MainView(Context context) {
            super(context);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            CV.updateCanvas(canvas.getWidth(), canvas.getHeight());
            CV.draw(canvas);
        }
    }
}

class CalendarShape {
    long start=-1;
    long end=-1;
    long mark=-1;
    String label=null;
    String comment=null;
    private Paint paint;

    public CalendarShape() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }
    public boolean setColor(int color) {
        try {
            paint.setColor(color);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public void draw(CalendarView cv, Canvas canvas) {
        if (start == -1 || end == -1)
            return;
        int[] rectTL = cv.conv_ts_screen(start);
        int[] rectBR = cv.conv_ts_screen(end);
        canvas.drawRect(rectTL[0], rectTL[1], rectBR[0] + cv.getUnitWidth(), rectBR[1], paint);
        Log.d("Tag",rectTL[0] + "," + rectTL[1] + "," + rectBR[0] + "," + rectBR[1]);
    }
    @Override
    public String toString() {
        return "CalendarShape>start:" + start + ",end:" + end + ",mark:" + mark + ",label:" + label + ",comment:" + comment;
    }
}

class CalendarView {
    private int screenH;
    private int screenW;
    private long orig;
    private float g0x;
    private float g0y;
    private float gridW;
    private float gridH;
    private float unit_width;

    private ArrayList<CalendarShape> shapes = new ArrayList<CalendarShape>();

    private Paint textStyle;

    public CalendarView(long orig, float g0x, float g0y, float gridW, float gridH) {
        this.screenH = 100;
        this.screenW = 100;
        this.orig = orig;
        this.g0x = g0x;
        this.g0y = g0y;
        this.gridW = gridW;
        this.gridH = gridH;

        unit_width = screenW / gridW;
        textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
    }
    public void updateCanvas(int width, int height) {
        this.screenW = width;
        this.screenH = height;
        this.unit_width = width/ gridW;
    }
    public void processLog(List<String> log) {
        CalendarShape curTD = new CalendarShape();
        shapes.add(curTD);

        for (String line : log) {
            String[] LogParts = line.split(">",-1);
            long ts = Long.parseLong(LogParts[0]);
            String[] ArgParts = LogParts[2].split("\\|",-1);
            if (ArgParts.length > 0) {
                switch (ArgParts[0]) {
                    case "s":
                        curTD.end = ts;
                        curTD = new CalendarShape();
                        shapes.add(curTD);
                        curTD.start = ts;
                        if (ArgParts.length > 1)
                            curTD.label = ArgParts[1];
                        if (ArgParts.length > 2)
                            curTD.setColor(Color.parseColor(ArgParts[2]));
                        if (ArgParts.length > 3)
                            curTD.comment = ArgParts[3];
                        break;
                    case "e":
                        curTD.end = ts;
                        break;
                    case "m":
                        CalendarShape markTD = new CalendarShape();
                        markTD.mark = ts;
                        if (ArgParts.length > 1)
                            markTD.label = ArgParts[1];
                        if (ArgParts.length > 2)
                            markTD.setColor(Color.parseColor(ArgParts[2]));
                        if (ArgParts.length > 3)
                            markTD.comment = ArgParts[3];
                        shapes.add(markTD);
                        break;
                }
            }
        }
    }
    public void draw(Canvas canvas) {
        for (CalendarShape s : shapes) {
            s.draw(this,canvas);
        }
        float startDate = (float) Math.floor(g0x);
        for (int i = 0; i< gridH +1; i++ ) {
            //TODO
            int[] lblXY = conv_grid_screen(-1,startDate+i);
            canvas.drawText(new SimpleDateFormat("MMM d").format(new Date(conv_grid_ts(-1,startDate+i)*1000)), lblXY[0], lblXY[1], textStyle);
        }
    }
    public float getUnitWidth() {
        return unit_width;
    }

    public int[] conv_ts_screen(long ts) {
        long days = ts > orig ? (ts - orig)/86400 : (ts - orig) / 86400 - 1;
        float dow = (float) ((days + 4611686018427387900L)%7);
        float weeks = (float) (days >= 0? days/7 : (days + 1) / 7 - 1) + ((float) ((ts - orig +4611686018427360000L)%86400) / 86400);
        int[] ret = {(int) ((dow - g0x)/ gridW * screenW), (int) ((weeks - g0y)/ gridH * screenH)};
        return ret;
    }
    public int[] conv_grid_screen(float x, float y) {
       int[] ret = {(int) ((x - g0x)/ gridW * screenW), (int) ((y - g0y)/ gridH * screenH)};
       return ret;
    }
    public float conv_grid_num(float x, float y) {
        float dow = x < 0 ?  0 : x >= 6 ? 6 : x;
        float weeks = (float) Math.floor(y)*7;
        return (float) (weeks + dow + (y-Math.floor(y)));
    }
    public long conv_grid_ts(float x, float y) {
        return (long) (conv_grid_num(x,y)*86400) + orig;
    }
}

/*
            // custom drawing code here
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);

            // make the entire canvas white
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            // draw blue circle with anti aliasing turned off
            paint.setAntiAlias(false);
            paint.setColor(Color.BLUE);
            canvas.drawCircle(20, 20, 15, paint);

            // draw green circle with anti aliasing turned on
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            canvas.drawCircle(60, 20, 15, paint);

            // draw red rectangle with anti aliasing turned off
            paint.setAntiAlias(false);
            paint.setColor(Color.RED);
            canvas.drawRect(100, 5, 200, 30, paint);

            // draw the rotated text
            canvas.rotate(-45);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("Graphics Rotation", 40, 180, paint);

            //undo the rotate
            //canvas.restore();
 */