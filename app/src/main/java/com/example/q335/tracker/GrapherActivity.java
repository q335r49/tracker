package com.example.q335.tracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GrapherActivity extends Activity {
    private CalendarView CV;
    private final String LOG_FILE_NAME = "log.txt";
    //Log syntax: [HEADING]>Label|Color|Pos|comment
    //TODO: finalize Log formatting
    //TODO: start logging exceptions in logcat rather than Toasting, tag Debug

    public List<String> Log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grapher);

//        Log = Arrays.asList(
//                "1421299830>1-15-2015 5:30:30>s|L1|red|comment",
//                "1421303400>1-15-2015 6:30:00>s|L1|blue|comment",
//                "1421314200>1-15-2015 9:30:00>s|L2|green|com",
//                "1421319600>1-15-2015 11:00:00>s|L3|grey|com",
//                "1421460000>1-17-2015 2:00:00>s|L4|red|comment"
//        );
        Log = read_file(getApplicationContext(),LOG_FILE_NAME);
        if (Log == null) {
            Toast.makeText(this, "Cannot read from log file", Toast.LENGTH_SHORT).show();
            return;
        }

        CV = new CalendarView(1421280000L,-1,-1,10,4);
        CV.processLog(Log);
        setContentView(new MainView(this));
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
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
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
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    CV.setStatusText("X:" + eventX + " Y:" + eventY);

                    break;
            }
            invalidate();
            return true;
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
    public boolean setColor(String color) {
        try {
            paint.setColor(Color.parseColor(color));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public void draw(CalendarView cv, Canvas canvas) {
        int[] rectC1;
        int[] rectC2;

        if (start == -1 || end == -1)
            return;
        long rect0 = start;
        for (long nextMidnight = start-(start-cv.orig +4611686018427360000L)%86400+86399; nextMidnight < end; nextMidnight+=86400) {
            rectC1 = cv.conv_ts_screen(rect0);
            rectC2 = cv.conv_ts_screen(nextMidnight);
            canvas.drawRect(rectC1[0], rectC1[1], rectC2[0] + cv.getUnitWidth(), rectC2[1], paint);
            rect0 = nextMidnight+1;
        }
        rectC1 = cv.conv_ts_screen(rect0);
        rectC2 = cv.conv_ts_screen(end);
        canvas.drawRect(rectC1[0], rectC1[1], rectC2[0] + cv.getUnitWidth(), rectC2[1], paint);
    }
    @Override
    public String toString() {
        return "CalendarShape>start:" + start + ",end:" + end + ",mark:" + mark + ",label:" + label + ",comment:" + comment;
    }
}

class CalendarView {
    public long orig;

    private int screenH;
    private int screenW;
    private float g0x;
    private float g0y;
    private float gridW;
    private float gridH;
    private float unit_width;
    private String statusText;

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
        statusText = "";
    }
    public void updateCanvas(int width, int height) {
        this.screenW = width;
        this.screenH = height;
        this.unit_width = width/ gridW;
    }
    public void processLog(List<String> log) {
        CalendarShape curTD = new CalendarShape();
        shapes.add(curTD);
        long ts;

        //TODO: determine initial window from Log
        for (String line : log) {
            String[] LogParts = line.split(">",-1);
            try {
                ts = Long.parseLong(LogParts[0]);
            } catch (NumberFormatException e) {
                continue;
            }
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
                            curTD.setColor(ArgParts[2]);
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
                            markTD.setColor(ArgParts[2]);
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
            //TODO: Better date labeling
            int[] lblXY = conv_grid_screen((float) -0.5,(float) (startDate+i+0.5));
            canvas.drawText(new SimpleDateFormat("MMM d").format(new Date(conv_grid_ts(-1,startDate+i)*1000)), lblXY[0], lblXY[1], textStyle);
            int[] l0 = conv_grid_screen(0,startDate+i);
            int[] l1 = conv_grid_screen(7,startDate+i);
            canvas.drawLine(l0[0],l0[1],l1[0],l1[1],textStyle);
        }
        for (int i=0; i<8; i++) {
            int[] l0 = conv_grid_screen(i,g0y);
            int[] l1 = conv_grid_screen(i,g0y+gridH);
            canvas.drawLine(l0[0],l0[1],l1[0],l1[1],textStyle);
        }
        if (!statusText.isEmpty())
            canvas.drawText(statusText,20,screenH-50,textStyle);
    }
    public float getUnitWidth() {
        return unit_width;
    }
    public void setStatusText(String s) {
        statusText = s;
    }
    public String getStatusText() {
        return statusText;
    }

    public int[] conv_ts_screen(long ts) {
        long days = ts > orig ? (ts - orig)/86400 : (ts - orig) / 86400 - 1;
        float dow = (float) ((days + 4611686018427387900L)%7);
        float weeks = (float) (days >= 0? days/7 : (days + 1) / 7 - 1) + ((float) ((ts - orig +4611686018427360000L)%86400) / 86400);
        return new int[] {(int) ((dow - g0x)/ gridW * screenW), (int) ((weeks - g0y)/ gridH * screenH)};
    }
    public int[] conv_grid_screen(float x, float y) {
       return new int[] {(int) ((x - g0x)/ gridW * screenW), (int) ((y - g0y)/ gridH * screenH)};
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