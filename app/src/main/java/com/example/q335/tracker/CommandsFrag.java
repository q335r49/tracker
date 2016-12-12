package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class CommandsFrag extends Fragment {
    SharedPreferences sprefs;
    private GridView mainView;
    private List<String[]> commands = new ArrayList<>();
    private static final String LOG_FILE = "log.txt";
    private final static int COMMENT_IX = 0;
    private final static int COLOR_IX = 1;
    Context context;
    //TODO: Textcolor in commandviews should be white
    //TODO: Set foreground color
    //TODO: Handle bad color case

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        context = getActivity().getApplicationContext();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_commands,container, false);
        mainView = (GridView) view.findViewById(R.id.GV);
        sprefs = context.getSharedPreferences("TrackerPrefs", MODE_PRIVATE);
        String jsonText = sprefs.getString("commands", "");
        loadCommands(jsonText);
        return view;
    }
    public void loadCommands(String s) {
        if (s.isEmpty()) {
            commands.add(new String[]{"Work now", "red", "0", ""});
            commands.add(new String[]{"Play", "blue", "0", ""});
        } else {
            Type listType = new TypeToken<List<String[]>>() { }.getType();
            commands = new Gson().fromJson(s, listType);
        }
        makeView();
    }

    public static int darkenColor(int color, float factor) {
        return Color.argb(Color.alpha(color),
                Math.min(Math.round(Color.red(color) * factor),255),
                Math.min(Math.round(Color.green(color) * factor),255),
                Math.min(Math.round(Color.blue(color) * factor),255));
    }
    private void makeView() {
        Collections.sort(commands, new Comparator<String[]>() {
            public int compare(String[] s1, String[] s2) {
                return s1[0].compareToIgnoreCase(s2[0]);
            }
        });
        List<Map<String, String>> LVentries = new ArrayList<>();
        for (String[] s : commands) {
            final Map<String, String> listItem = new HashMap<>();
            listItem.put("label", s[0]);
            listItem.put("syntax", ""); //TODO: find better use for second row
            LVentries.add(listItem);
        }
        final Map<String, String> listItem = new HashMap<>();
        listItem.put("label", "New Command");
        listItem.put("syntax", "");
        LVentries.add(listItem);
        SimpleAdapter LVadapter = new SimpleAdapter(getContext(), LVentries, R.layout.gv_list_item, new String[]{"label", "syntax"}, new int[]{R.id.text1, R.id.text2}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position < commands.size()) {
                    int testColor;
                    try {
                        testColor = Color.parseColor(commands.get(position)[COLOR_IX]);
                    } catch (IllegalArgumentException e) {
                        Log.e("tracker:",e.toString());
                        testColor = Color.parseColor("darkgrey");
                    }
                    final int bg_Norm = testColor;
                    final int bg_Press = CommandsFrag.darkenColor(bg_Norm,0.7f);
                    final int pos = position;
                    view.setBackgroundColor(bg_Norm);
                    view.setOnTouchListener(new View.OnTouchListener() {
                        private ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        private Rect viewBounds;
                        private boolean offset_mode = false;
                        private float offset_0x;
                        private float offset_0y;
                        private final Handler handler = new Handler();
                        private Runnable mLongPressed;
                        boolean has_run = false;
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getActionMasked()) {
                                case MotionEvent.ACTION_DOWN:
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                    v.setBackgroundColor(bg_Press);
                                    viewBounds = new Rect(v.getLeft(),v.getTop(),v.getRight(),v.getBottom());
                                    offset_mode = false;
                                    final View finalView = v;
                                    mLongPressed = new Runnable() {
                                        public void run() {
                                            has_run = true;
                                            finalView.setBackgroundColor(bg_Norm);
                                            Context context = getContext();
                                            LayoutInflater layoutInflater = LayoutInflater.from(context);
                                            View promptView = layoutInflater.inflate(R.layout.prompts, null);
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                            alertDialogBuilder.setView(promptView);

                                            final EditText commentEntry = (EditText) promptView.findViewById(R.id.commentInput);
                                            final EditText colorEntry = (EditText) promptView.findViewById(R.id.colorInput);
                                            commentEntry.setText(commands.get(pos)[COMMENT_IX]);
                                            colorEntry.setText(commands.get(pos)[COLOR_IX]);

                                            final View curColorV = promptView.findViewById(R.id.CurColor);
                                            final FlexboxLayout paletteView = (FlexboxLayout) promptView.findViewById(R.id.paletteBox);
                                            final int childCount = paletteView.getChildCount();
                                            for (int i = 0; i < childCount ; i++) {
                                                View v = paletteView.getChildAt(i);
                                                final int bg = ((ColorDrawable) v.getBackground()).getColor();
                                                v.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        curColorV.setBackgroundColor(bg);
                                                    }
                                                })
                                            }


                                            alertDialogBuilder
                                                    .setCancelable(true)
                                                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            commands.set(pos, new String[]{commentEntry.getText().toString(), colorEntry.getText().toString(), "0", ""});
                                                            makeView();
                                                        }
                                                    })
                                                    .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            commands.remove(pos);
                                                            makeView();
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            alertDialogBuilder.create().show();
                                        }
                                    };
                                    handler.postDelayed(mLongPressed,1800);
                                    return true;
                                case MotionEvent.ACTION_MOVE:
                                    if (has_run)
                                        return false;
                                    if(offset_mode || !viewBounds.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                                        handler.removeCallbacks(mLongPressed);
                                        if (!offset_mode) {
                                            offset_mode = true;
                                            offset_0x = event.getX();
                                            offset_0y = event.getY();
                                            ab.setBackgroundDrawable(new ColorDrawable(bg_Norm));
                                        } else {
                                            int delay = (int) (event.getX()-offset_0x);
                                            int duration = (int) (event.getY()-offset_0y);
                                            delay = delay > 100 ? delay - 100 : delay < -100 ? delay + 100 : 0;
                                            duration = duration > 100 ? duration - 100 : duration < -100 ? duration + 100 : 0;
                                            String abString = "...";
                                            if (delay > 0)
                                                abString += " in  " + Integer.toString(delay / 60)  + ":" + String.format("%02d",delay % 60);
                                            else if (delay < 0)
                                                abString += " already " + Integer.toString(-delay  / 60) + ":" + String.format("%02d",-delay % 60);
                                            if (duration > 0)
                                                abString += " for " + Integer.toString(duration / 60) + ":" + String.format("%02d", duration % 60);
                                            else if (duration < 0)
                                                abString += " for " + Integer.toString(-duration / 60) + ":" + String.format("%02d", -duration % 60);
                                            ab.setTitle(abString.isEmpty()? commands.get(pos)[0] : abString);
                                        }
                                    }
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    if (has_run)
                                        return false;
                                    handler.removeCallbacks(mLongPressed);
                                    v.setBackgroundColor(bg_Norm);
                                    ab.setBackgroundDrawable(new ColorDrawable(bg_Norm));
                                    String start = "0";
                                    String end = "";
                                    String abString = commands.get(pos)[0] + "...";
                                    if (offset_mode) {
                                        int delay = (int) (event.getX() - offset_0x);
                                        int duration = (int) (event.getY() - offset_0y);
                                        delay = delay > 100 ? delay - 100 : delay < -100 ? delay + 100 : 0;
                                        duration = duration > 100 ? duration - 100 : duration < -100 ? duration + 100 : 0;
                                        if (delay > 0)
                                            abString += " in  " + Integer.toString(delay / 60) + ":" + String.format("%02d", delay % 60);
                                        else if (delay < 0)
                                            abString += " already " + Integer.toString(-delay / 60) + ":" + String.format("%02d", -delay % 60);
                                        if (duration > 0)
                                            abString += " for " + Integer.toString(duration / 60) + ":" + String.format("%02d", duration % 60);
                                        else if (duration < 0)
                                            abString += " for " + Integer.toString(-duration / 60) + ":" + String.format("%02d", -duration % 60);
                                        start = Integer.toString(delay * 60);
                                        end = duration == 0 ? "" : Integer.toString(duration > 0 ? 60 * (delay + duration) : 60 * (delay - duration));
                                    }
                                    ab.setTitle(abString);
                                    String entry = Long.toString(System.currentTimeMillis() / 1000) + ">" + (new Date()).toString() + ">" + commands.get(pos)[COLOR_IX] + ">" + start + ">" + end + ">" + commands.get(pos)[COMMENT_IX];
                                    File internalFile = new File(context.getFilesDir(), LOG_FILE);
                                    try {
                                        FileOutputStream out = new FileOutputStream(internalFile, true);
                                        out.write(entry.getBytes());
                                        out.write(System.getProperty("line.separator").getBytes());
                                        out.close();
                                    } catch (Exception e) {
                                        Log.e("tracker:",e.toString());
                                        Toast.makeText(context, "Cannot write to internal storage", Toast.LENGTH_LONG).show();
                                    }
                                    mListener.processNewLogEntry(entry);
                                    return false;
                                case MotionEvent.ACTION_CANCEL:
                                    handler.removeCallbacks(mLongPressed);
                                    v.setBackgroundColor(bg_Norm);
                                    return false;
                                default:
                                    return true;
                            }
                        }
                    });
                } else {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                            View promptView = layoutInflater.inflate(R.layout.prompts, null);
                            final EditText commentEntry = (EditText) promptView.findViewById(R.id.commentInput);
                            final EditText colorEntry = (EditText) promptView.findViewById(R.id.colorInput);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                            alertDialogBuilder.setView(promptView);
                            alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    commands.add(new String[]{commentEntry.getText().toString(), colorEntry.getText().toString(), "0", ""});
                                    makeView();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .create().show();
                            return false;
                        }
                    });
                }
                return view;
            }
        };
        mainView.setAdapter(LVadapter);
        LVadapter.notifyDataSetChanged();
        sprefs.edit().putString("commands", new Gson().toJson(commands)).apply();
    }
//    //Rename method, loadCalendarView argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
    public interface OnFragmentInteractionListener {
        void processNewLogEntry(String E);
    }
    public CommandsFrag() { } // Required empty public constructor
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public static CommandsFrag newInstance(String param1, String param2) {
        CommandsFrag fragment = new CommandsFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private OnFragmentInteractionListener mListener;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}