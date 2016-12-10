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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalendarFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFrag extends Fragment {
    private CalendarView CV;
    private static final String LOG_FILE = "log.txt";
    public List<String> logEntries;
    Context context;
    View mView;

    public void processNewEntry(String E) {
        CV.addLogEntry(E);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CalendarFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFrag.
     */
    // TODO: Rename and change types and number of parameters
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
        //TODO: Less naive update function
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //inflater.inflate(R.layout.activity_grapher, container, false);
        //Toast.makeText(context, "Grapher activity: OnCreateView", Toast.LENGTH_LONG).show();
        //TODO: receive communications from CommandsFrag / MainActivity (eg, "Import") and update view
        //update();
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
        }
        @Override
        protected void onDraw(Canvas canvas) {
            //Toast.makeText(getActivity().getApplicationContext(), "Draw!!!!", Toast.LENGTH_SHORT).show();
            //TODO: Investigate why draw is happening multiple times
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

    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

class CalendarShape {
    long start=-1;
    long end=-1;
    String comment=null;
    private Paint paint;

    public CalendarShape() {
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
    public void draw(CalendarView cv, Canvas canvas) {
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
    }
    public void updateCanvas(int width, int height) {
        this.screenW = width;
        this.screenH = height;
        this.unit_width = width/ gridW;
    }

    private ArrayList<CalendarShape> shapes;
    //TS>READABLE>COLOR>S>E>COMMENT
    private final static int TIMESTAMP_POS = 0;
    private final static int COLOR_POS = 2;
    private final static int START_POS = 3;
    private final static int END_POS = 4;
    private final static int COMMENT_POS = 5;
    private final static int ARG_LEN = 6;
    private CalendarShape curTD;

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
                    curTD = new CalendarShape();
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
                CalendarShape markTD = new CalendarShape();
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
        curTD = new CalendarShape();
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
                        curTD = new CalendarShape();
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
                    CalendarShape markTD = new CalendarShape();
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
            canvas.drawText(statusText,20,screenH-150,textStyle);
    }
}