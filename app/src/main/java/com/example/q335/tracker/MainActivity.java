package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final Context context = this;

    private List<String[]> Commands = new ArrayList<String[]>();

    private ListView LV;
    private List<Map<String,String>> LVentries;
    private SimpleAdapter LVadapter;

    public static final String MY_PREFS = "MyPrefsFile";
    SharedPreferences pref;
    SharedPreferences.Editor prefEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        prefEdit = pref.edit();

        String jsonText = pref.getString("commands", null);
        if (jsonText == null) {
            Commands.add(new String[]{"Log time", "The day of the year is %s!doy"});
            Commands.add(new String[]{"B", "Sample B"});
            Commands.add(new String[]{"C", "Sample C"});
            Commands.add(new String[]{"D", "Sample D"});
            Commands.add(new String[]{"E", "Sample E"});
            Commands.add(new String[]{"F", "Sampkle F"});
            Commands.add(new String[]{"G", "Smample G"});
        } else {
            Type listType = new TypeToken<List<String[]>>() {}.getType();
            Commands = new Gson().fromJson(jsonText, listType);
        }

        LV = (ListView) findViewById(R.id.LV);
        initializeLVAdapter();
        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                if (position< Commands.size()) {
                    String text = ((TextView) (view.findViewById(android.R.id.text1))).getText().toString();
                    Log(Commands.get(position)[1], "log.txt");
                }
            }
        });
        LV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.prompts, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptView);

            final EditText labelInput = (EditText) promptView.findViewById(R.id.promptTextView);
            final EditText commandInput = (EditText) promptView.findViewById(R.id.userInput);
            final int listIndex = pos;

            if (listIndex >= Commands.size()) {
                alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Commands.add(new String[] {labelInput.getText().toString(), commandInput.getText().toString()});
                        final Map<String, String> listItem = new HashMap<String, String>();
                        listItem.put("label", Commands.get(listIndex)[0]);
                        listItem.put("syntax", Commands.get(listIndex)[1]);
                        LVentries.add(listIndex, listItem);
                        writeCommandsToPrefs();
                        LVadapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int id) {
                        dialog.cancel();
                    }
                });
            } else {
                labelInput.setText(((TextView)(view.findViewById(android.R.id.text1))).getText().toString());
                commandInput.setText(((TextView)(view.findViewById(android.R.id.text2))).getText().toString());
                alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listIndex >= Commands.size()) {
                            Commands.add(new String[] {labelInput.getText().toString(), commandInput.getText().toString()});
                            final Map<String, String> listItem = new HashMap<String, String>();
                            listItem.put("label", Commands.get(listIndex)[0]);
                            listItem.put("syntax", Commands.get(listIndex)[1]);
                            LVentries.add(listIndex, listItem);
                        } else {
                            Commands.set(listIndex, new String[]{labelInput.getText().toString(), commandInput.getText().toString()});
                            final Map<String, String> listItem = new HashMap<String, String>();
                            listItem.put("label", Commands.get(listIndex)[0]);
                            listItem.put("syntax", Commands.get(listIndex)[1]);
                            LVentries.set(listIndex, listItem);
                        }
                        writeCommandsToPrefs();
                        LVadapter.notifyDataSetChanged();
                    }
                })
                .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Commands.remove(listIndex);
                        LVentries.remove(listIndex);
                        writeCommandsToPrefs();
                        LVadapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int id) {
                        dialog.cancel();
                    }
                });
            }
            AlertDialog alertD = alertDialogBuilder.create();
            alertD.show();
            return true;
            }
        });
    }

    private void writeCommandsToPrefs() {
        prefEdit.putString("commands",new Gson().toJson(Commands));
        prefEdit.apply();
    }
    private void initializeLVAdapter() {
        LVentries = new ArrayList<Map<String,String>>();
        for (String[] s: Commands) {
            final Map<String,String> listItem = new HashMap<String,String>();
            listItem.put("label", s[0]);
            listItem.put("syntax", s[1]);
            LVentries.add(listItem);
        }
            final Map<String,String> listItem = new HashMap<String,String>();
            listItem.put("label", "New Command");
            listItem.put("syntax", "Long press to add a new command");
            LVentries.add(listItem);
        LVadapter = new SimpleAdapter(this, LVentries,android.R.layout.simple_list_item_2,
                new String[] {"label", "syntax"},new int[] {android.R.id.text1, android.R.id.text2});
        LV.setAdapter(LVadapter);
        LVadapter.notifyDataSetChanged();
    }

    public static void writeString (File file, String data) throws Exception{
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(data.getBytes());
        } finally {
            stream.close();
        }
    }
    public static String readString (File file) throws Exception{
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } finally {
            in.close();
        }
        return new String(bytes);
    }
    public static void copyFile(File src, File dst) throws Exception {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemExport: {
                String extStorPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                File directory = new File(extStorPath);
                directory.mkdirs();
                File outputLog = new File(extStorPath,"log.txt");
                File outputCmd = new File(extStorPath,"commands.json");
                try {
                    copyFile(new File(getFilesDir(), "log.txt"),outputLog);
                    writeString(outputCmd, pref.getString("commands",""));
                    Toast.makeText(context, "log.txt and commands.json exported to " + extStorPath, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Writing log.txt, commands.json to " + extStorPath
                            + "failed: Make sure this App has write permission! (Settings > Apps)", Toast.LENGTH_SHORT).show();
                }
                break;}
            case R.id.menuItemImport: {
                String extStorPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                File directory = new File(extStorPath);
                if (!directory.isDirectory()) {
                    Toast.makeText(context, "Import error: " + extStorPath + "not found!", Toast.LENGTH_SHORT).show();
                } else {
                    File inputCmd = new File(extStorPath, "commands.json");
                    //TODO: Log import if exists
                    try {
                        String jsonText = readString(inputCmd);
                        if (jsonText == null) {
                            Toast.makeText(context, "Import failed: empty file", Toast.LENGTH_SHORT).show();
                        } else {
                            Type listType = new TypeToken<List<String[]>>() {
                            }.getType();
                            Commands = new Gson().fromJson(jsonText, listType);
                            initializeLVAdapter();
                            writeCommandsToPrefs();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Import failed:" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(context, "Imported " + extStorPath + "commands.json", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            case R.id.menuItemGraph:
                //TODO: Graph
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
        //TODO: Request access
    }

    public boolean Log(String data, String fname) {
        String[] commands = data.split("!");
        int comlen = commands.length;
        String entry;

        Calendar now = Calendar.getInstance();
        for (int i = 1; i < comlen; i++) {
            switch (commands[i]) {
                case "stp":
                    commands[i] = Long.toString(System.currentTimeMillis() / 1000);
                    break;
                case "doy":
                    commands[i] = Integer.toString(now.get(Calendar.DAY_OF_YEAR));
                    break;
                case "year":
                    commands[i] = Integer.toString(now.get(Calendar.YEAR));
                    break;
                case "hour":
                    commands[i] = Integer.toString(now.get(Calendar.HOUR_OF_DAY));
                    break;
                case "min":
                    commands[i] = Integer.toString(now.get(Calendar.MINUTE));
                    break;
                case "sec":
                    commands[i] = Integer.toString(now.get(Calendar.SECOND));
                    break;
                case "dow":
                    commands[i] = Integer.toString(now.get(Calendar.DAY_OF_WEEK));
                    break;
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