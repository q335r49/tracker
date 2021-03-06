package com.example.q335.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CalendarFrag extends Fragment {
    private ScaleView mView;
    private Queue<String> EntryBuffer = new LinkedList<>();
    public void procMess(String E) {
        if (mView == null)
            EntryBuffer.add(E);
        else {
            for (String s = EntryBuffer.poll(); s!=null; EntryBuffer.poll())
                mView.procMess(s);
            mView.procMess(E);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar,container,false);
        mView = (ScaleView) (view.findViewById(R.id.drawing));
        return view;
    }

    public CalendarFrag() { } // Required empty public constructor
    private OnFragmentInteractionListener mListener;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    public static CalendarFrag newInstance(String param1, String param2) {
        CalendarFrag fragment = new CalendarFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
            mListener = (OnFragmentInteractionListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
class CalendarWin {
    private ArrayList<CalendarRect> shapes;
    private CalendarRect curTD;
    private long orig;
        long getOrig() { return orig; }
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
    private float ratio_grid_screen_W;
    private float ratio_grid_screen_H;
    private Paint textStyle;
    private String statusText;
        void setStatusText(String s) { statusText = s; }
    float[] conv_ts_screen(long ts) {
        long days = ts > orig ? (ts - orig)/86400 : (ts - orig) / 86400 - 1;
        float dow = (float) ((days + 4611686018427387900L)%7);
        float weeks = (float) (days >= 0? days/7 : (days + 1) / 7 - 1) + ((float) ((ts - orig +4611686018427360000L)%86400) / 86400);
        return new float[] {(dow - g0x)/ ratio_grid_screen_W, (weeks - g0y)/ ratio_grid_screen_H};
    }
    float[] conv_grid_screen(float gx, float gy) {
        return new float[] { (gx - g0x)/ ratio_grid_screen_W, (gy - g0y)/ ratio_grid_screen_H};
    }
    float[] conv_screen_grid(float sx, float sy) {
        return new float[] {sx*ratio_grid_screen_W+g0x, sy*ratio_grid_screen_H+g0y};
    }
    float conv_grid_num(float gx, float gy) {
        float dow = gx < 0 ?  0 : gx >= 6 ? 6 : gx;
        float weeks = (float) Math.floor(gy)*7;
        return (float) (weeks + dow + (gy-Math.floor(gy)));
    }
    long conv_grid_ts(float gx, float gy) {
        return (long) (conv_grid_num(gx,gy)*86400) + orig;
    }

    CalendarWin(long orig, float g0x, float g0y, float gridW, float gridH) {
        shapes = new ArrayList<>();
        curTD = new CalendarRect();
        shapes.add(curTD);
        this.orig = orig;
        this.g0x = g0x;
        this.g0y = g0y;
        this.gridW = gridW;
        this.gridH = gridH;
        textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setTextSize(24f); //TODO: Dynamically set text size
        statusText = "";
    }
    void shiftWindow(float x, float y) {
        //TODO: Limit horizontal pan range
        g0x -= x * ratio_grid_screen_W;
        g0y -= y * ratio_grid_screen_H;
    }
    void reScale(float scale, float x0, float y0) {
        float[] newGridOrig = conv_screen_grid(x0-x0/scale,y0-y0/scale);
        g0x = newGridOrig[0];
        g0y = newGridOrig[1];
        gridW /=scale;
        gridH /=scale;
        ratio_grid_screen_W = gridW/screenW;
        ratio_grid_screen_H = gridH/screenH;
    }

    //TS>READABLE>COLOR>S>E>COMMENT
    private final static int TIMESTAMP_POS = 0;
    private final static int COLOR_POS = 2;
    private final static int START_POS = 3;
    private final static int END_POS = 4;
    private final static int COMMENT_POS = 5;
    private final static int ARG_LEN = 6;
    void loadEntry(String line) {
        long ts;
        String[] args = line.split(">",-1);
        if (args.length < ARG_LEN) {
            Log.e("tracker:","Insufficient args: "+line);
            return;
        }
        try {
            ts = Long.parseLong(args[TIMESTAMP_POS]);
            if (args[END_POS].isEmpty()) {
                if (!args[START_POS].isEmpty()) {
                    if (curTD.end == -1)
                        curTD.end = ts + Long.parseLong(args[START_POS]);
                    curTD = new CalendarRect();
                    shapes.add(curTD);
                    curTD.start = ts + Long.parseLong(args[START_POS]);
                    curTD.setColor(args[COLOR_POS]);
                    //TODO: add color to palette
                    curTD.comment = args[COMMENT_POS];
                } else {
                    Log.e("tracker:","Empty start and end: "+line);
                }
            } else if (args[START_POS].isEmpty()) {
                curTD.end = ts + Long.parseLong(args[END_POS]);
            } else {
                CalendarRect markTD = new CalendarRect();
                markTD.start = ts + Long.parseLong(args[START_POS]);
                markTD.end = ts + Long.parseLong(args[END_POS]);
                markTD.setColor(args[COLOR_POS]);
                //TODO: add color to palette
                markTD.comment = args[COMMENT_POS];
                shapes.add(markTD);
                //TODO: Review logic now that there is no more mark
            }
        } catch (IllegalArgumentException e) {
            Log.e("tracker:","Bad color or number format: "+line);
        }
    }
    void loadAllEntries(List<String> log) {
        shapes = new ArrayList<>();
        curTD = new CalendarRect();
        shapes.add(curTD);
        for (String line : log) {
            loadEntry(line);
        }
    }
    void draw(Canvas canvas) {
        screenW = canvas.getWidth();
        screenH = canvas.getHeight();
        this.unit_width = screenW/ gridW;
        ratio_grid_screen_W = gridW/screenW;
        ratio_grid_screen_H = gridH/screenH;
        for (CalendarRect s : shapes) {
            s.draw(this,canvas);
        }
        float startDate = (float) Math.floor(g0y);
        for (int i = 0; i< gridH +1; i++ ) {
            float[] lblXY = conv_grid_screen((float) -0.5,(float) (startDate+i+0.5));
            //TODO: Change grid and labeling based on scale
            canvas.drawText((new SimpleDateFormat("MMM d").format(new Date(conv_grid_ts(-1,startDate+i)*1000))), 25, lblXY[1], textStyle);
            float[] l0 = conv_grid_screen(0,startDate+i);
            float[] l1 = conv_grid_screen(7,startDate+i);
            canvas.drawLine(l0[0],l0[1],l1[0],l1[1],textStyle);
        }
        for (int i=0; i<8; i++) {
            float[] l0 = conv_grid_screen(i,g0y);
            float[] l1 = conv_grid_screen(i,g0y+gridH);
            canvas.drawLine(l0[0],l0[1],l1[0],l1[1],textStyle);
        }
        if (!statusText.isEmpty())
            canvas.drawText(statusText,20,screenH-150,textStyle);
    }
}
class CalendarRect {
    long start=-1;
    long end=-1;
    String comment=null;
    private Paint paint;

    CalendarRect() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }
    public void setColor(String color) {
        try {
            paint.setColor(Color.parseColor(color));
        } catch (IllegalArgumentException e) {
            paint.setColor(Color.parseColor("darkgrey"));
            Log.e("tracker:","Bad color format: "+color);
        }
    }
    void draw(CalendarWin cv, Canvas canvas) {
        float[] rectC1;
        float[] rectC2;
        if (start==-1 || end==-1)
            return;
        long rect0 = start;
        for (long nextMidnight = start-(start-cv.getOrig() +4611686018427360000L)%86400+86399; nextMidnight < end; nextMidnight+=86400) {
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