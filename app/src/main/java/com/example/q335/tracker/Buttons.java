package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Buttons.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Buttons#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Buttons extends Fragment {
    SharedPreferences sprefs;
    private GridView mainView;
    private List<String[]> commands = new ArrayList<>();
    private static final String LOG_FILE = "log.txt";
    final static int COMMENT_POS = 0;
    final static int COLOR_POS = 1;
    final static int START_POS = 2;
    final static int END_POS = 3;
    Context context;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Buttons() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Buttons.
     */
    // TODO: Rename and change types and number of parameters
    public static Buttons newInstance(String param1, String param2) {
        Buttons fragment = new Buttons();
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
        //setContentView(R.layout.activity_main);
        View view = inflater.inflate(R.layout.activity_main,container, false);
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

        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                if (position < commands.size())
                    newLogEntry(position);
            }
        });
        mainView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
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
                if (commandsIx >= commands.size()) {
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    commands.add(new String[]{commentEntry.getText().toString(), colorEntry.getText().toString(),startEntry.getText().toString(),endEntry.getText().toString()});
                                    makeView();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                } else {
                    commentEntry.setText(commands.get(pos)[COMMENT_POS]);
                    colorEntry.setText(commands.get(pos)[COLOR_POS]);
                    startEntry.setText(commands.get(pos)[START_POS]);
                    endEntry.setText(commands.get(pos)[END_POS]);
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    commands.set(commandsIx, new String[]{commentEntry.getText().toString(), colorEntry.getText().toString(),startEntry.getText().toString(),endEntry.getText().toString()});
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
                }
                alertDialogBuilder.create().show();
                return true;
            }
        });
        // Inflate the layout for this fragment
        Toast.makeText(context, "Executed wholes startup routine!", Toast.LENGTH_LONG).show();
        return view;
    }

    public void loadCommands(String com) {
        Type listType = new TypeToken<List<String[]>>() {
        }.getType();
        commands = new Gson().fromJson(com, listType);
        makeView();
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
                    try {
                        view.setBackgroundColor(Color.parseColor(commands.get(position)[COLOR_POS]));
                    } catch (IllegalArgumentException e) {
                        Log.d("tracker:","Bad background color @ " + position);
                    }
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

    // TODO: Rename method, update argument and hook method into UI event
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
        void processNewLogEntry(String E);
    }
}
