package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
    final static int COMMENT_POS = 0;
    final static int COLOR_POS = 1;
    final static int START_POS = 2;
    final static int END_POS = 3;
    Context context;

    //Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CommandsFrag() {
        // Required empty public constructor
    }

    public static CommandsFrag newInstance(String param1, String param2) {
        CommandsFrag fragment = new CommandsFrag();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_commands);
        View view = inflater.inflate(R.layout.fragment_commands,container, false);
        mainView = (GridView) view.findViewById(R.id.GV);

        sprefs = context.getSharedPreferences("TrackerPrefs", MODE_PRIVATE);
        String jsonText = sprefs.getString("commands", "");
        if (jsonText.isEmpty()) {
            commands.add(new String[]{"Work now", "red", "0", ""});
            commands.add(new String[]{"Play1", "blue", "0", ""});
        } else {
            Type listType = new TypeToken<List<String[]>>() {
            }.getType();
            commands = new Gson().fromJson(jsonText, listType);
        }
        makeView();
        return view;
    }

    public void loadCommands(String com) {
        Type listType = new TypeToken<List<String[]>>() {
        }.getType();
        commands = new Gson().fromJson(com, listType);
        makeView();
    }


    public static int manipulateColor(int color, float factor) {
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
        List<Map<String, String>> LVentries = new ArrayList<Map<String, String>>();
        for (String[] s : commands) {
            final Map<String, String> listItem = new HashMap<String, String>();
            listItem.put("label", s[0]);
            listItem.put("syntax", "s:" + s[2] + " e:" + s[3]);
            LVentries.add(listItem);
        }
        final Map<String, String> listItem = new HashMap<String, String>();
        listItem.put("label", "New Command");
        listItem.put("syntax", "Long press to add");
        LVentries.add(listItem);
        SimpleAdapter LVadapter = new SimpleAdapter(getContext(), LVentries, R.layout.gv_list_item,
                new String[]{"label", "syntax"}, new int[]{R.id.text1, R.id.text2}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position < commands.size()) {
                    int testColor;
                    try {
                        testColor = Color.parseColor(commands.get(position)[COLOR_POS]);
                    } catch (IllegalArgumentException e) {
                        Log.d("tracker:","Bad background color @ " + position);
                        testColor = Color.parseColor("black");
                    }
                    final int bg = testColor;
                    final int finalPosition = position;
                    view.setBackgroundColor(bg);
                    view.setOnTouchListener(new View.OnTouchListener() {
                        private final int bg_normal=bg;
                        private final int bg_pressed=CommandsFrag.manipulateColor(bg,0.7f);
                        private Rect rect;
                        private final int pos = finalPosition;
                        private ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        private float offset;
                        private float duration;
                        boolean offset_mode = false;
                        float offset_0x;
                        float offset_0y;

                        private final Handler handler = new Handler();
                        private Runnable mLongPressed;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getActionMasked()) {
                                case MotionEvent.ACTION_DOWN:
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                    v.setBackgroundColor(bg_pressed);
                                    rect = new Rect(v.getLeft(),v.getTop(),v.getRight(),v.getBottom());
                                    offset_mode = false;
                                    final View finalView = v;
                                    mLongPressed = new Runnable() {
                                        public void run() {
                                            finalView.setBackgroundColor(bg_normal);
                                            Context context = getContext();
                                            LayoutInflater layoutInflater = LayoutInflater.from(context);

                                            View promptView = layoutInflater.inflate(R.layout.prompts, null);

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                            alertDialogBuilder.setView(promptView);

                                            final EditText commentEntry = (EditText) promptView.findViewById(R.id.commentInput);
                                            final EditText colorEntry = (EditText) promptView.findViewById(R.id.colorInput);
                                            final EditText startEntry = (EditText) promptView.findViewById(R.id.startInput);
                                            final EditText endEntry = (EditText) promptView.findViewById(R.id.endInput);
                                            final int commandsIx = pos;
                                            commentEntry.setText(commands.get(pos)[COMMENT_POS]);
                                            colorEntry.setText(commands.get(pos)[COLOR_POS]);
                                            startEntry.setText(commands.get(pos)[START_POS]);
                                            endEntry.setText(commands.get(pos)[END_POS]);
                                            alertDialogBuilder
                                                    .setCancelable(true)
                                                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            commands.set(commandsIx, new String[]{commentEntry.getText().toString(), colorEntry.getText().toString(), startEntry.getText().toString(), endEntry.getText().toString()});
                                                            makeView();
                                                        }
                                                    })
                                                    .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            commands.remove(commandsIx);
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
                                    handler.postDelayed(mLongPressed,2000);
                                    return true;
                                case MotionEvent.ACTION_MOVE:
                                    if(offset_mode || !rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                                        handler.removeCallbacks(mLongPressed);
                                        if (!offset_mode) {
                                            offset_mode = true;
                                            offset_0x = event.getX();
                                            offset_0y = event.getY();
                                            ab.setBackgroundDrawable(new ColorDrawable(bg_normal));
                                        } else {
                                            offset = event.getX()-offset_0x;
                                            offset = offset > 100 ? offset-100 : offset < -100 ? offset + 100 : 0;
                                            duration = event.getY()-offset_0y;
                                            duration = duration > 100 ? duration-100 : duration < -100 ? duration + 100 : 0;
                                            int iOff = (int) offset;
                                            int iDur = (int) duration;
                                            String timeStatus = (iOff > 0 ? "delay: " + Integer.toString(iOff) : iOff < 0 ? "already: " + Integer.toString(-iOff) : "") +
                                                    (iDur > 0 ? " until: " + Integer.toString(iDur) : iDur < 0 ? " until: " + Integer.toString(iDur) : "");
                                            ab.setTitle(timeStatus.isEmpty()? commands.get(pos)[0] : timeStatus);
                                        }
                                    }
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    handler.removeCallbacks(mLongPressed);
                                    v.setBackgroundColor(bg_normal);
                                    if (pos < commands.size()) {
                                        ab.setBackgroundDrawable(new ColorDrawable(bg_normal));
                                        int intDuration = (int) duration;
                                        int intOffset = (int) offset;
                                        ab.setTitle(commands.get(pos)[0] + "Dela");
                                        newLogEntry(pos);
                                        //TODO: Initialize action bar with current actiivty
                                    }
                                    return false;
                                case MotionEvent.ACTION_CANCEL:
                                    handler.removeCallbacks(mLongPressed);
                                    v.setBackgroundColor(bg_normal);
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
                            final EditText startEntry = (EditText) promptView.findViewById(R.id.startInput);
                            final EditText endEntry = (EditText) promptView.findViewById(R.id.endInput);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                            alertDialogBuilder.setView(promptView);
                            alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    commands.add(new String[]{commentEntry.getText().toString(), colorEntry.getText().toString(), startEntry.getText().toString(), endEntry.getText().toString()});
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
    private void newLogEntry(int position) {
        String[] args = commands.get(position);
        Date now = new Date();
        String entry = Long.toString(System.currentTimeMillis() / 1000) + ">" + now.toString() + ">" + args[COLOR_POS] + ">" + args[START_POS] + ">" + args[END_POS] + ">" + args[COMMENT_POS];
        File internalFile = new File(context.getFilesDir(), LOG_FILE);
        try {
            FileOutputStream out = new FileOutputStream(internalFile, true);
            out.write(entry.getBytes());
            out.write(System.getProperty("line.separator").getBytes());
            out.close();
        } catch (Exception e) {
            Toast.makeText(context, "Cannot write entry to internal storage", Toast.LENGTH_LONG).show();
        }
        mListener.processNewLogEntry(entry);
    }

    //Rename method, loadCalendarView argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

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

    public interface OnFragmentInteractionListener {
        void processNewLogEntry(String E);
    }
}