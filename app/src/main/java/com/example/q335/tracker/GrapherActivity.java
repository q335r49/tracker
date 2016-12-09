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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GrapherActivity extends Activity {
    private CalendarView CV;
    private final String LOG_FILE_NAME = "log.txt";
    //TODO: start logging exceptions in logcat rather than Toasting, tag Debug

    public List<String> Log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grapher);
        Log = read_file(getApplicationContext(),LOG_FILE_NAME);
        if (Log == null) {
            Toast.makeText(this, "Cannot read from log file", Toast.LENGTH_SHORT).show();
            return;
        }

        CV = new CalendarView(1421280000L,-1,-1,10,4);
        CV.log_to_shapes(Log);
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
            return false; //TODO: catlog
        }
    }
    public void draw(CalendarView cv, Canvas canvas) {
        int[] rectC1;
        int[] rectC2;
        if (start==-1 || end==-1)
            return;
        //TODO: Draw marks
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
        float getUnitWidth() {
    return unit_width;
}
    int[] conv_ts_screen(long ts) {
        long days = ts > orig ? (ts - orig)/86400 : (ts - orig) / 86400 - 1;
        float dow = (float) ((days + 4611686018427387900L)%7);
        float weeks = (float) (days >= 0? days/7 : (days + 1) / 7 - 1) + ((float) ((ts - orig +4611686018427360000L)%86400) / 86400);
        return new int[] {(int) ((dow - g0x)/ gridW * screenW), (int) ((weeks - g0y)/ gridH * screenH)};
    }
    int[] conv_grid_screen(float x, float y) {
        return new int[] {(int) ((x - g0x)/ gridW * screenW), (int) ((y - g0y)/ gridH * screenH)};
    }
    float conv_grid_num(float x, float y) {
        float dow = x < 0 ?  0 : x >= 6 ? 6 : x;
        float weeks = (float) Math.floor(y)*7;
        return (float) (weeks + dow + (y-Math.floor(y)));
    }
    long conv_grid_ts(float x, float y) {
        return (long) (conv_grid_num(x,y)*86400) + orig;
    }
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

    private ArrayList<CalendarShape> shapes = new ArrayList<CalendarShape>();
    //TS>READABLE>COLOR>S>E>COMMENT
    private final static int TIMESTAMP_POS = 0;
    private final static int READABLE_POS = 1;
    private final static int COLOR_POS = 2;
    private final static int START_POS = 3;
    private final static int END_POS = 4;
    private final static int COMMENT_POS = 5;
    private final static int ARG_LEN = 6;
    void log_to_shapes(List<String> log) {
        CalendarShape curTD = new CalendarShape();
        shapes.add(curTD);
        long ts;
        //TODO: determine initial window from Log
        for (String line : log) {
            String[] args = line.split(">",-1);
            if (args.length < ARG_LEN) {
                continue; //TODO: catlog
            }
            try {
                ts = Long.parseLong(args[TIMESTAMP_POS]);
                if (args[END_POS].isEmpty()) {
                    if (!args[START_POS].isEmpty()) {
                        if (curTD.end == -1)
                            curTD.end = ts + Long.parseLong(args[START_POS]);
                        curTD = new CalendarShape();
                        shapes.add(curTD);
                        curTD.start = ts + Long.parseLong(args[START_POS]);
                        curTD.setColor(args[COLOR_POS]);
                        curTD.comment = args[COMMENT_POS];
                    } else {
                        //TODO: log error condition
                    }
                } else if (args[START_POS].isEmpty()) {
                    curTD.end = ts + Long.parseLong(args[END_POS]);
                } else {
                    CalendarShape markTD = new CalendarShape();
                    markTD.start = ts + Long.parseLong(args[START_POS]);
                    markTD.end = ts + Long.parseLong(args[END_POS]);
                    markTD.setColor(args[COLOR_POS]);
                    markTD.comment = args[COMMENT_POS];
                    shapes.add(markTD);
                }
            } catch (IllegalArgumentException e) {
                //TODO: log bad number
            }
        }
    }

    private String statusText;
        void setStatusText(String s) {
            statusText = s;
        }
        private Paint textStyle;
    void draw(Canvas canvas) {
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
}