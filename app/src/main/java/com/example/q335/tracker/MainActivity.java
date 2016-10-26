package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences Events;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Events = PreferenceManager.getDefaultSharedPreferences(this);

        Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), Settings.class), 1);
            }
        });

        Button graphButton = (Button) findViewById(R.id.buttonGraph);
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Visualize
            }
        });

        List<Map<String,String>> LVentries = new ArrayList<Map<String,String>>();
        Map<String,?> keys = Events.getAll();
        for (Map.Entry<String,?> entry : keys.entrySet()) {
            //LVentries.add(createEntry("button",entry.getKey()));
            final Map<String,String> listItem = new HashMap<String,String>();
            listItem.put("label", entry.getKey());
            listItem.put("syntax", entry.getValue().toString());
            LVentries.add(listItem);
        }

        ListView LV = (ListView) findViewById(R.id.LV);
        SimpleAdapter LVad = new SimpleAdapter(this,LVentries,android.R.layout.simple_list_item_2,
                new String[] {"label", "syntax"},new int[] {android.R.id.text1, android.R.id.text2});
        LV.setAdapter(LVad);
        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                String text = ((TextView)(view.findViewById(android.R.id.text1))).getText().toString();
                if (Events.contains(text)) {
                    Log(Events.getString(text, ""), "log.txt");
                } else {
                    Toast.makeText(MainActivity.this, "No Event Associated with Button " + text, Toast.LENGTH_SHORT).show();
                }
            }
        });
        LV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                final String label = ((TextView)(view.findViewById(android.R.id.text1))).getText().toString();
                final String syntax = ((TextView)(view.findViewById(android.R.id.text2))).getText().toString();
                final View currentview = view;

                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.prompts, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptView);

                final EditText input = (EditText) promptView.findViewById(R.id.userInput);
                ((TextView)promptView.findViewById(R.id.promptTextView)).setText(label);
                input.setText(syntax);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = Events.edit();
                                editor.putString(label,input.getText().toString());
                                editor.apply();
                                ((TextView) currentview.findViewById(android.R.id.text2)).setText(input.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,	int id) {
                                        dialog.cancel();
                                    }
                                });
                // create an alert dialog
                AlertDialog alertD = alertDialogBuilder.create();
                alertD.show();
                return true;
            }
        });
        //TODO: Floating menus
        //TODO: JSON: Export and edit log and import
    }

    private HashMap<String,String> createEntry(String key, String name) {
        HashMap<String,String> entry = new HashMap<String,String>();
        entry.put(key,name);
        return entry;
    }

    public boolean Log(String data, String fname) {

        String[] commands = data.split("\\|");
        int comlen = commands.length;
        String entry;

        for (int i = 1; i < comlen; i++) {
            switch (commands[i]) {
                case "time":
                    commands[i] = Long.toString(System.currentTimeMillis() / 1000);
                    //TODO: human readable dates
                    break;
                //TODO: Expense Tracker
            }
        }

        switch (comlen) {
            case 1:
                entry = commands[0];
                break;
            case 2:
                entry = String.format(commands[0],commands[1]);
                break;
            case 3:
                entry = String.format(commands[0],commands[1],commands[2]);
                break;
            case 4:
                entry = String.format(commands[0],commands[1],commands[2],commands[3]);
                break;
            case 5:
                entry = String.format(commands[0],commands[1],commands[2],commands[3],commands[4]);
                break;
            default:
                entry = commands[0];
                break;
        }

        File internalFile = new File(getFilesDir(), fname);
        try {
            FileOutputStream out = new FileOutputStream(internalFile, true);
            out.write(entry.getBytes());
            out.write(System.getProperty("line.separator").getBytes());
            out.close();
            Toast.makeText(this, "Logged: " + entry, Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(this, "Internal Storage Write Error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

