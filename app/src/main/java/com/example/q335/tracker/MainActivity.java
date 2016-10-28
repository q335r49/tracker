package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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

    SharedPreferences pref;
    SharedPreferences.Editor prefEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences("TrackerPrefs", MODE_PRIVATE);
        prefEdit = pref.edit();

        String jsonText = pref.getString("commands", null);
        if (jsonText == null) {
            Commands.add(new String[]{"Day of year, Hour, Minute", "%s!dhm"});
            Commands.add(new String[]{"Day of year, hour:minute:sec", "%s %s:%s:%s!doy!hour!min!sec"});
            Commands.add(new String[]{"Timestamp", "timestamp: %s!ts"});
            Commands.add(new String[]{"Day of Week, Minute of Day", "%s,%s!dow!mod"});
            Commands.add(new String[]{"Text Input, Number Input", "Text:%s Numb%s!text,Enter text!number,Enter number"});
            Commands.add(new String[]{"Pick, Pick prompt", "Just pick:%s Prompt pick:%s!pick,1,2,3,4!pickPrompt:pick,Number 1-5:,1,2,3,4,5"});
            Commands.add(new String[]{"Just some text", "some text"});
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
        boolean choiceLog = false;
        boolean choiceCmd = false;

        switch (item.getItemId()) {
            case R.id.menuItemExport: {
                final String extStorPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                final File directory = new File(extStorPath);
                directory.mkdirs();
                final File outputLog = new File(extStorPath,"log.txt");
                final File outputCmd = new File(extStorPath,"commands.json");

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Choose files to import")
                        .setMessage("Exporting to "+extStorPath)
                        .setNeutralButton("command.json", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    writeCommandsToPrefs();
                                    writeString(outputCmd, pref.getString("commands",""));
                                    Toast.makeText(context, "commands.json exported to " + extStorPath, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("log.txt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    copyFile(new File(getFilesDir(), "log.txt"),outputLog);
                                    Toast.makeText(context, "log.txt exported to " + extStorPath, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setPositiveButton("Both", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    copyFile(new File(getFilesDir(), "log.txt"),outputLog);
                                    writeCommandsToPrefs();
                                    writeString(outputCmd, pref.getString("commands",""));
                                    Toast.makeText(context, "log.txt and commands.json exported to " + extStorPath, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                break;}
            case R.id.menuItemImport: {
                final String extStorPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                final File directory = new File(extStorPath);
                final File inputCmd = new File(extStorPath, "commands.json");
                final File inputLog = new File(extStorPath, "log.txt");

                if (!directory.isDirectory()) {
                    Toast.makeText(context, "Import error: " + extStorPath + "not found!", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Choose files to import")
                        .setMessage("Importing from " + extStorPath)
                        .setNeutralButton("commands.json", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                        Toast.makeText(context, "commands.json import successful", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Import failed:" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("log.txt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!inputLog.exists()) {
                                    Toast.makeText(context, "Import failed: empty file", Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        copyFile(inputLog, new File(getFilesDir(), "log.txt"));
                                        Toast.makeText(context, "log.txt import successful", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                        Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .setPositiveButton("Both", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                        Toast.makeText(context, "commands.json import successful", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Import failed:" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                                if (!inputLog.exists()) {
                                    Toast.makeText(context, "log.txt failed: no file", Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        copyFile(inputLog, new File(getFilesDir(), "log.txt"));
                                        Toast.makeText(context, "log.txt import successful", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                        Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .show();
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

    private int logState_modal_dialogs_remaining = 0;
    private String[] logCom;
    private int logComLen;
    private String LogFile;
    public boolean Log(String data, String fname) {
        logCom = data.split("!");
        logComLen = logCom.length;
        logState_modal_dialogs_remaining = 0;
        LogFile = fname;

        Calendar now = Calendar.getInstance();
        for (int i = 1; i < logComLen; i++) {
            String[] parsedLC = logCom[i].split(",",2);
            switch (parsedLC[0]) {
                case "dhm":
                    logCom[i] = now.get(Calendar.DAY_OF_YEAR) + "," + now.get(Calendar.HOUR_OF_DAY) + "," + now.get(Calendar.MINUTE);
                    break;
                case "ts":
                    logCom[i] = Long.toString(System.currentTimeMillis() / 1000);
                    break;
                case "doy":
                    logCom[i] = Integer.toString(now.get(Calendar.DAY_OF_YEAR));
                    break;
                case "year":
                    logCom[i] = Integer.toString(now.get(Calendar.YEAR));
                    break;
                case "hour":
                    logCom[i] = Integer.toString(now.get(Calendar.HOUR_OF_DAY));
                    break;
                case "min":
                    logCom[i] = Integer.toString(now.get(Calendar.MINUTE));
                    break;
                case "sec":
                    logCom[i] = Integer.toString(now.get(Calendar.SECOND));
                    break;
                case "dow":
                    logCom[i] = Integer.toString(now.get(Calendar.DAY_OF_WEEK));
                    break;
                case "mod":
                    logCom[i] = Integer.toString(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE));
                    break;
                case "text":
                case "number": {
                    AlertDialog.Builder b = new AlertDialog.Builder(context);
                    b.setTitle(parsedLC.length > 1 ? parsedLC[1] : "Enter text");
                    final EditText input = new EditText(this);
                    input.setInputType(parsedLC[0].equals("text")? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
                    b.setView(input);
                    logState_modal_dialogs_remaining++;
                    final int j = i;
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logCom[j] = input.getText().toString();
                            logState_modal_dialogs_remaining--;
                            if (logState_modal_dialogs_remaining == 0)
                                writeLog(LogFile);
                        }
                    });
                    b.show();
                    break;
                }
                case "pickPrompt":
                case "pick": {
                    if (parsedLC.length <= 1)
                        break;
                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    logState_modal_dialogs_remaining++;
                    final int j = i;
                    final String[] types;
                    if (parsedLC[0].equals("pick"))
                        types = parsedLC[1].split(",");
                    else {
                        String[] titleAndChoices = parsedLC[1].split(",",2);
                        if (titleAndChoices.length <=1)
                            break;
                        b.setTitle(titleAndChoices[0]);
                        types = titleAndChoices[1].split(",");
                    }
                    b.setItems(types, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            logCom[j] = types[which];
                            logState_modal_dialogs_remaining--;
                            if (logState_modal_dialogs_remaining == 0)
                                writeLog(LogFile);
                        }
                    });
                    b.show();
                    break;
                }
            }
        }
        if (logState_modal_dialogs_remaining == 0)
            writeLog(LogFile);
        return true;
    }

    private void writeLog(String fname) {
        String entry = null;
        try {
            switch (logComLen) {
                case 1:
                    entry = logCom[0];
                    break;
                case 2:
                    entry = String.format(logCom[0], logCom[1]);
                    break;
                case 3:
                    entry = String.format(logCom[0], logCom[1], logCom[2]);
                    break;
                case 4:
                    entry = String.format(logCom[0], logCom[1], logCom[2], logCom[3]);
                    break;
                case 5:
                    entry = String.format(logCom[0], logCom[1], logCom[2], logCom[3], logCom[4]);
                    break;
                default:
                    entry = logCom[0];
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(context, "Syntax error: wrong number of parameters", Toast.LENGTH_SHORT).show();
        }
        if (entry != null) {
            File internalFile = new File(getFilesDir(), fname);
            try {
                FileOutputStream out = new FileOutputStream(internalFile, true);
                out.write(entry.getBytes());
                out.write(System.getProperty("line.separator").getBytes());
                out.close();
                Toast.makeText(this, "Logged: " + entry, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //e.printStackTrace();
                Toast.makeText(this, "Internal Storage Write Error!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}