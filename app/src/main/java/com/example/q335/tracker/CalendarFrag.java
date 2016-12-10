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
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class CalendarFrag extends Fragment {
    private CalendarView CV;
    private static final String LOG_FILE = "log.txt";
    public List<String> logEntries;
    Context context;
    View mView;

    public void processNewEntry(String E) {
        CV.addLogEntry(E);
    }

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CalendarFrag() {
        // Required empty public constructor
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        context = getActivity().getApplicationContext();
        update();
    }
    public void update() {
        logEntries = read_file(getActivity().getApplicationContext(), LOG_FILE);
        if (logEntries == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Cannot read from log file", Toast.LENGTH_LONG).show();
            return;
        }
        CV = new CalendarView(1421280000L,-1,-1,10,4);
        CV.log_to_shapes(logEntries);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate view?
        mView = new MainView(getContext());
        return mView;
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
    private class MainView extends View {
        public MainView(Context context) {
            super(context);
            mScaleDetector = new ScaleGestureDetector(getActivity().getApplicationContext(), new ScaleListener());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //Toast.makeText(getActivity().getApplicationContext(), "Draw!!!!", Toast.LENGTH_SHORT).show();
            //TODO: Investigate why draw is happening multiple times
            super.onDraw(canvas);
            CV.updateCanvas(canvas.getWidth(), canvas.getHeight());
            CV.draw(canvas);
        }

        ScaleGestureDetector mScaleDetector;
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
                    //final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    CV.shiftWindow(x-mLastTouchX,y-mLastTouchY);
                    invalidate();
                    mLastTouchX = x;
                    mLastTouchY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    //CV.setStatusText("X:" + eventX + " Y:" + eventY);
                    break;
            }
            return true;
        }
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //TODO: Scale and zoom at once
            CV.reScale(detector.getScaleFactor());
            mView.invalidate();
            return true;
        }
    }

    // Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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

class CalendarView {
    private long orig;
    public void setOrig(long orig) { this.orig = orig; }
    public long getOrig() { return orig; }
    private int screenH;
    private int screenW;
    private float g0x;
    private float g0y;
    private float gridW;
    private float gridH;
    private float unit_width;
    private float ratio_grid_screen_W;
    private float ratio_grid_screen_H;
        float getUnitWidth() {
        return unit_width;
    }

    int[] conv_ts_screen(long ts) {
        long days = ts > orig ? (ts - orig)/86400 : (ts - orig) / 86400 - 1;
        float dow = (float) ((days + 4611686018427387900L)%7);
        float weeks = (float) (days >= 0? days/7 : (days + 1) / 7 - 1) + ((float) ((ts - orig +4611686018427360000L)%86400) / 86400);
        return new int[] {(int) ((dow - g0x)/ ratio_grid_screen_W), (int) ((weeks - g0y)/ ratio_grid_screen_H)};
    }
    int[] conv_grid_screen(float x, float y) {
        return new int[] {(int) ((x - g0x)/ ratio_grid_screen_W), (int) ((y - g0y)/ ratio_grid_screen_H)};
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
        ratio_grid_screen_W = this.gridW/screenW;
        ratio_grid_screen_H = this.gridH/screenH;

    }
    public CalendarView() {
        this.screenH = 100;
        this.screenW = 100;
        this.orig = System.currentTimeMillis() / 1000;
        this.g0x = -1;
        this.g0y = -2;
        this.gridW = 10;
        this.gridH = 4;

        unit_width = screenW / gridW;
        textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        statusText = "";
        ratio_grid_screen_W = this.gridW/screenW;
        ratio_grid_screen_H = this.gridH/screenH;
    }
    public void updateCanvas(int width, int height) {
        this.screenW = width;
        this.screenH = height;
        this.unit_width = width/ gridW;
        ratio_grid_screen_W = gridW/screenW;
        ratio_grid_screen_H = gridH/screenH;
    }

    public void shiftWindow(float x, float y) {
        //TODO: Limit horizontal pan
        g0x -= x * ratio_grid_screen_W;
        g0y -= y * ratio_grid_screen_H;
    }
    public void reScale(float scale) {
        gridW /=scale;
        gridH /=scale;
        ratio_grid_screen_W = gridW/screenW;
        ratio_grid_screen_H = gridH/screenH;
        //TODO: Reset view in menu
        //TODO: Import COMMAND in Commands screen, import LOG in calendar screen!!
    }

    private ArrayList<CalendarRect> shapes;
    //TS>READABLE>COLOR>S>E>COMMENT
    private final static int TIMESTAMP_POS = 0;
    private final static int COLOR_POS = 2;
    private final static int START_POS = 3;
    private final static int END_POS = 4;
    private final static int COMMENT_POS = 5;
    private final static int ARG_LEN = 6;
    private CalendarRect curTD;

    public void addLogEntry(String line) {
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
                markTD.comment = args[COMMENT_POS];
                shapes.add(markTD);
            }
        } catch (IllegalArgumentException e) {
            Log.e("tracker:","Bad color or number format: "+line);
        }
    }
    void log_to_shapes(List<String> log) {
        shapes = new ArrayList<>();
        curTD = new CalendarRect();
        shapes.add(curTD);
        long ts=0;
        for (String line : log) {
            String[] args = line.split(">",-1);
            if (args.length < ARG_LEN) {
                Log.e("tracker:","Insufficient args: "+line);
                continue;
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
                    markTD.comment = args[COMMENT_POS];
                    shapes.add(markTD);
                }
            } catch (IllegalArgumentException e) {
                Log.e("tracker:","Bad color or number format: "+line);
            }
        }

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(ts*1000L-86400000L*7);
        cal.set(Calendar.DAY_OF_WEEK,0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        setOrig(cal.getTimeInMillis()/1000);
        //TODO: figure out initialization logic
    }

    private String statusText;
        void setStatusText(String s) { statusText = s; }
    private Paint textStyle;
    void draw(Canvas canvas) {
        //TODO: fix grid drawing bug
        for (CalendarRect s : shapes) {
            s.draw(this,canvas);
        }
        float startDate = (float) Math.floor(g0y);
        for (int i = 0; i< gridH +1; i++ ) {
            //TODO: Better date labeling
            int[] lblXY = conv_grid_screen((float) -0.5,(float) (startDate+i+0.5));
            canvas.drawText((new SimpleDateFormat("MMM d").format(new Date(conv_grid_ts(-1,startDate+i)*1000))), screenW/10, lblXY[1], textStyle);
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
            Log.e("tracker:","Bad color format: "+color);
        }
    }
    void draw(CalendarView cv, Canvas canvas) {
        int[] rectC1;
        int[] rectC2;
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