package com.example.q335.tracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrapherActivity extends Activity {
    private CalendarGraph CG;
    private ArrayList<CalendarShape> CS = new ArrayList<CalendarShape>();
    private CalendarWindow CW;

    //TODO: Log syntax: [HEADING]>Label|Color|Pos|comment
    public List<String> TestLog = Arrays.asList(
        "1421299830>1-15-2015 5:30:30>t1|red|s|comment",
        "1421303400>1-15-2015 6:30:00>t1|red|e|comment",
        "1421314200>1-15-2015 9:30:00>t2|blue|s|com",
        "1421319600>1-15-2015 11:00:00>t2|blue|s|com",
        "1421319600>1-15-2015 11:00:00>s|label1|color|comment"
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
            String[] ArgParts = LogParts[2].split("|",-1);
            if (ArgParts.length > 0) {
                switch (ArgParts[0]) {
                    case "s":
                        curTD.end = ts;
                        curTD = new CalendarShape();
                        CS.add(curTD);
                        curTD.start = ts;
                        if (ArgParts.length > 1)
                            curTD.label = LogParts[1];
                        if (ArgParts.length > 2)
                            curTD.color = Color.parseColor(LogParts[2]);
                        if (ArgParts.length > 3)
                            curTD.comment = LogParts[3];
                        break;
                    case "e":
                        curTD.end = ts;
                        break;
                    case "m":
                        CalendarShape markTD = new CalendarShape();
                        markTD.mark = ts;
                        if (ArgParts.length > 1)
                            markTD.label = LogParts[1];
                        if (ArgParts.length > 2)
                            markTD.color = Color.parseColor(LogParts[2]);
                        if (ArgParts.length > 3)
                            markTD.comment = LogParts[3];
                        break;
                }
            }
        }

        CG = new CalendarGraph(this);
        setContentView(CG);
    }

    private class CalendarGraph extends View {
        public CalendarGraph(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //TODO: initialize CW

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            for (int i=0; i<CS.size(); i++) {
                CS.get(i).draw(CW,canvas,paint);
            }
        }
    }
}

class CalendarShape {
    long start=-1;
    long end=-1;
    long mark=-1;
    int color=-1;
    String label=null;
    String comment=null;

    public void draw(CalendarWindow cw, Canvas canvas, Paint paint) {
        if (start == -1 || end == -1)
            return;

        int[] rectTL = cw.toScreenCoord(start);
        int[] rectBR = cw.toScreenCoord(end);
        canvas.drawRect(rectTL[0], rectTL[1], rectBR[0] + cw.getUnitWidth(), rectBR[1], paint);
    }
}

class CalendarWindow {
    private int height;
    private int width;
    private long zero;
    private float v0x;
    private float v0y;
    private float vw;
    private float vh;
    private float unit_width;

    public CalendarWindow(int height, int width, long zero, float v0x, float v0y, float vw, float vh) {
        this.height = height;
        this.width = width;
        this.zero = zero;
        this.v0x = v0x;
        this.v0y = v0y;
        this.vw = vw;
        this.vh = vh;
        this.unit_width = width/vw;
    }
    public float getUnitWidth() {
        return unit_width;
    }
    public int[] toScreenCoord(long ts) {
        long days = ts > zero ? (ts-zero)/86400 : (ts-zero)/86400 - 1;
        float dow = (float) (days+4611686018427387900L)%7;
        float weeks = (days >= 0? days/7 : (days+1)/7-1) + (float) (ts+4611686018427360000L)%86400 / 86400;
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