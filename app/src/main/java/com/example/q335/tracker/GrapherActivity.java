package com.example.q335.tracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrapherActivity extends Activity {
    private MainView View;
    private ArrayList<CalendarShape> CS = new ArrayList<CalendarShape>();
    private CalendarView CV;

    //TODO: Log syntax: [HEADING]>Label|Color|Pos|comment
    //TODO: Invisible (i?)
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

        CalendarShape curTD = new CalendarShape();
        CS.add(curTD);

        for (int i=0; i<TestLog.size(); i++) {
            String[] LogParts = TestLog.get(i).split(">",-1);
            long ts = Long.parseLong(LogParts[0]);
            String[] ArgParts = LogParts[2].split("\\|",-1);
            if (ArgParts.length > 0) {
                switch (ArgParts[0]) {
                    case "s":
                        curTD.end = ts;
                        curTD = new CalendarShape();
                        CS.add(curTD);
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
                        break;
                }
            }
        }

        CV = new CalendarView(100,100,1421280000L,-1,-1,10,2);
        View = new MainView(this);
        setContentView(View);
    }

    private class MainView extends View {
        public MainView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            CV.updateCanvas(canvas.getWidth(), canvas.getHeight());
            for (int i=0; i<CS.size(); i++) {
                CS.get(i).draw(CV,canvas);
            }
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
    public void draw(CalendarView cw, Canvas canvas) {
        if (start == -1 || end == -1)
            return;

        int[] rectTL = cw.toScreenCoord(start);
        int[] rectBR = cw.toScreenCoord(end);
        canvas.drawRect(rectTL[0], rectTL[1], rectBR[0] + cw.getUnitWidth(), rectBR[1], paint);
        Log.d("Tag",rectTL[0] + "," + rectTL[1] + "," + rectBR[0] + "," + rectBR[1]);
    }
    @Override
    public String toString() {
        return "CalendarShape>start:" + start + ",end:" + end + ",mark:" + mark + ",label:" + label + ",comment:" + comment;
    }
}

class CalendarView {
    private int height;
    private int width;
    private long zero;
    private float v0x;
    private float v0y;
    private float vw;
    private float vh;
    private float unit_width;

    public CalendarView(int width, int height, long zero, float v0x, float v0y, float vw, float vh) {
        this.height = height;
        this.width = width;
        this.zero = zero;
        this.v0x = v0x;
        this.v0y = v0y;
        this.vw = vw;
        this.vh = vh;
        this.unit_width = width/vw;
    }
    public void updateCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.unit_width = width/vw;
    }
    public float getUnitWidth() {
        return unit_width;
    }
    public int[] toScreenCoord(long ts) {
        long days = ts > zero ? (ts-zero)/86400 : (ts-zero)/86400 - 1;
        float dow = (float) ((days+4611686018427387900L)%7);
        float weeks = (float) (days >= 0? days/7 : (days+1)/7-1) + ((float) ((ts-zero+4611686018427360000L)%86400) / 86400);
        int[] ret = {(int) ((dow-v0x)/vw*width), (int) ((weeks-v0y)/vh*height)};
        return ret;
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