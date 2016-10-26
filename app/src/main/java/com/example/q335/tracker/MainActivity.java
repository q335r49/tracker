package com.example.q335.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences Events;

    private ListView LV;
    List<Map<String,String>> LVentries;
    SimpleAdapter LVad;

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

        LVentries = new ArrayList<Map<String,String>>();

        Map<String,?> keys = Events.getAll();
        for (Map.Entry<String,?> entry : keys.entrySet()) {
            LVentries.add(createEntry("button",entry.getKey()));
        }

        LV = (ListView) findViewById(R.id.LV);
        LVad = new SimpleAdapter(this,LVentries,android.R.layout.simple_list_item_1,new String[] {"button"},new int[] {android.R.id.text1});
        LV.setAdapter(LVad);
        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //TODO: Implement long-press settings!
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                TextView clickedView = (TextView) view;
                String text = clickedView.getText().toString();
                if (Events.contains(text)) {
                    Log(Events.getString(text, ""), "log.txt");
                } else {
                    Toast.makeText(MainActivity.this, "No Event Associated with Button A", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                TextView clickedView = (TextView) arg1;
                String text = clickedView.getText().toString();
                Toast.makeText(MainActivity.this, "Long Click:" + text, Toast.LENGTH_SHORT).show();
                return true;
            }

        });

        //TODO: Floating menus
        //TODO: Export and edit log and import
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

