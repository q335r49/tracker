package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    final Context context = this;
    private List<String[]> commandList = new ArrayList<String[]>();
    private GridView GV;
    private final String LOG_FILE_NAME = "log.txt";
    private final String COMMAND_FILE_NAME = "commands.json";
    private final String EXT_STORAGE_DIR_NAME = "tracker";
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GV = (GridView) findViewById(R.id.GV);
        pref = getApplicationContext().getSharedPreferences("TrackerPrefs", MODE_PRIVATE);

        String jsonText = pref.getString("commands", "");
        if (jsonText == null) {
            commandList.add(new String[]{"02 Day of year, Hour, Minute", "%s!dhm"});
            commandList.add(new String[]{"03 Day of year, hour:minute:sec", "%s %s:%s:%s!doy!hour!min!sec"});
            commandList.add(new String[]{"04 Timestamp", "timestamp: %s!ts"});
            commandList.add(new String[]{"05 Day of Week, Minute of Day", "%s,%s!dow!mod"});
            commandList.add(new String[]{"06 Text Input, Number Input", "Text:%s Numb%s!text,Enter text!number,Enter number"});
            commandList.add(new String[]{"07 Pick, Pick prompt", "Just pick:%s Prompt pick:%s!pick,Numbers 1-4:,1,2,3,4!pick,Number 1-5:,1,2,3,4,5"});
            commandList.add(new String[]{"01 Just some text", "some text"});
            //generic forms for chart data
            commandList.add(new String[]{"Event Data", "label:dev,cat:blue,dhm:%s,already:%s,ts:%s!dhm!seek,already:,0,60!pick!ts"});
            commandList.add(new String[]{"Expense Data", "label:food,cat:res,amt:%s,pos:mark,ts:%s!seek,$:,0,50!ts"});
        } else {
            Type listType = new TypeToken<List<String[]>>() {}.getType();
            commandList = new Gson().fromJson(jsonText, listType);
        }
        makeGV();

        GV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                if (position< commandList.size())
                    Log(commandList.get(position)[1]);
            }
        });
        GV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.prompts, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptView);

                final EditText labelInput = (EditText) promptView.findViewById(R.id.promptTextView);
                final EditText commandInput = (EditText) promptView.findViewById(R.id.userInput);
                final int listIndex = pos;

                if (listIndex >= commandList.size()) {
                    alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            commandList.add(new String[] {labelInput.getText().toString(), commandInput.getText().toString()});
                            makeGV();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,	int id) {
                            dialog.cancel();
                        }
                    });
                } else {
                    labelInput.setText(commandList.get(pos)[0]);
                    commandInput.setText(commandList.get(pos)[1]);
                    alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (listIndex >= commandList.size())
                                commandList.add(new String[] {labelInput.getText().toString(), commandInput.getText().toString()});
                            else
                                commandList.set(listIndex, new String[]{labelInput.getText().toString(), commandInput.getText().toString()});
                            makeGV();
                        }
                    })
                    .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            commandList.remove(listIndex);
                            makeGV();
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

    private void makeGV() {
        Collections.sort(commandList, new Comparator<String[]>() {
            public int compare(String[] s1, String[] s2) {
                return s1[0].compareToIgnoreCase(s2[0]);
            }
        });
        List<Map<String,String>> LVentries = new ArrayList<Map<String,String>>();
        for (String[] s: commandList) {
            final Map<String,String> listItem = new HashMap<String,String>();
            listItem.put("label", String.format("%2.30s", s[0]));
            listItem.put("syntax",String.format("%2.30s", s[1]));
            LVentries.add(listItem);
        }
        final Map<String,String> listItem = new HashMap<String,String>();
        listItem.put("label", "New Command");
        listItem.put("syntax", "Long press to add a new command");
        LVentries.add(listItem);
        SimpleAdapter LVadapter = new SimpleAdapter(this, LVentries,R.layout.gv_list_item,
                new String[] {"label", "syntax"},new int[] {R.id.text1, R.id.text2}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView,parent);
                //TODO: Pre-parse entries

                if (position < commandList.size()) {
                    String entry = commandList.get(position)[1];
                    int ind = entry.lastIndexOf("!");
                    String[] FGBG;
                    if (ind < entry.length() - 1 && ind > 0) {
                        FGBG = entry.substring(ind+1,entry.length()).split(",");
                        try {
                            view.setBackgroundColor(Color.parseColor(FGBG[0]));
                            if (FGBG.length > 1) {
                                ((TextView) view.findViewById(R.id.text1)).setTextColor(Color.parseColor(FGBG[1]));
                                ((TextView) view.findViewById(R.id.text2)).setTextColor(Color.parseColor(FGBG[1]));
                            }
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
                return view;
            }
        };
        GV.setAdapter(LVadapter);
        LVadapter.notifyDataSetChanged();
        pref.edit().putString("commands",new Gson().toJson(commandList)).apply();
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
                final String extStorPath = Environment.getExternalStorageDirectory() + File.separator + EXT_STORAGE_DIR_NAME + File.separator;
                final File directory = new File(extStorPath);
                directory.mkdirs();
                final File outputLog = new File(extStorPath,LOG_FILE_NAME);
                final File outputCmd = new File(extStorPath,COMMAND_FILE_NAME);

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
                                    writeString(outputCmd, pref.getString("commands",""));
                                    Toast.makeText(context, COMMAND_FILE_NAME + " exported to " + extStorPath, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(context, LOG_FILE_NAME + " exported to " + extStorPath, Toast.LENGTH_SHORT).show();
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
                                    writeString(outputCmd, pref.getString("commands",""));
                                    Toast.makeText(context, LOG_FILE_NAME + " and commands.json exported to " + extStorPath, Toast.LENGTH_SHORT).show();
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
                final File inputCmd = new File(extStorPath, COMMAND_FILE_NAME);
                final File inputLog = new File(extStorPath, LOG_FILE_NAME);

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
                                        commandList = new Gson().fromJson(jsonText, listType);
                                        makeGV();
                                        Toast.makeText(context, COMMAND_FILE_NAME + " import successful", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(context, LOG_FILE_NAME + " import successful", Toast.LENGTH_SHORT).show();
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
                                        commandList = new Gson().fromJson(jsonText, listType);
                                        makeGV();
                                        Toast.makeText(context, COMMAND_FILE_NAME + " import successful", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Import failed:" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                                if (!inputLog.exists()) {
                                    Toast.makeText(context, LOG_FILE_NAME + " failed: no file", Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        copyFile(inputLog, new File(getFilesDir(), "log.txt"));
                                        Toast.makeText(context, LOG_FILE_NAME + " import successful", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                        Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .show();
                }
                break; }
            case R.id.menuItemGraph:
                startActivity(new Intent(this, GrapherActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
        //TODO: Request access / pick from tree
    }

    private String[] logComList;
    private Queue<AlertDialog> promptStack;
    public boolean Log(String data) {
        String ErrorCondition = "";
        logComList = data.split("!");
        Date now = new Date();
        logComList[0]=Long.toString(System.currentTimeMillis() / 1000) + ">" + now.toString() + ">" + logComList[0];
        promptStack = new LinkedList<>();
        //TODO: !!! Color picker, favorite colors in sharedPrefs
        //TODO: Parse log for favorite colors
        for (int i = 1; i < logComList.length; i++) {
            String[] f_arg = logComList[i].split(",");
            switch (f_arg[0]) {
                case "text": case "number": {   // text,prompt Text!number,prompt Text
                    if (f_arg.length < 2)
                        ErrorCondition += "> Insufficient args: text/number,prompt";
                    else {
                        AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setTitle(f_arg[1]);
                        final EditText input = new EditText(this);
                        input.setInputType(f_arg[0].equals("text") ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
                        b.setView(input);
                        final int j = i;
                        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logComList[j] = input.getText().toString();
                                if (promptStack.isEmpty())
                                    Log_helper();
                                else
                                    promptStack.remove().show();
                            }
                        });
                        promptStack.add(b.create());
                    }
                    break; }
                case "pick": {
                    if (f_arg.length < 4)
                        ErrorCondition += "> Insufficient args: pick,prompt,choice1,choice2,...";
                    else {
                        AlertDialog.Builder b = new AlertDialog.Builder(this);
                        b.setTitle(f_arg[1]);
                        final String[] choices = Arrays.copyOfRange(f_arg, 2, f_arg.length);
                        final int j = i;
                        b.setItems(choices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                logComList[j] = choices[which];
                                if (promptStack.isEmpty())
                                    Log_helper();
                                else
                                    promptStack.remove().show();
                            }
                        });
                        promptStack.add(b.create());
                    }
                    break; }
                case "seek": {
                    if (f_arg.length < 4)
                        ErrorCondition += "> Insufficient args: seek,prompt,min,max";
                    else {
                        final String prompt = f_arg[1];
                        final float MIN = Float.parseFloat(f_arg[2]);
                        final float STEP = (Float.parseFloat(f_arg[3]) - MIN)/100;
                        final boolean SEEK_INT = ((f_arg[2] + f_arg[3]).indexOf(".") < 0);
                        final SeekBar input = new SeekBar(this);
                        final int j = i;
                        AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setTitle(prompt + f_arg[2]);
                        b.setView(input);
                        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logComList[j] = SEEK_INT ? Integer.toString(Math.round(MIN + STEP * input.getProgress())) : String.format("%.02f", MIN + STEP * input.getProgress());
                                if (promptStack.isEmpty())
                                    Log_helper();
                                else
                                    promptStack.remove().show();
                            }
                        });
                        final AlertDialog handle = b.create();
                        input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                handle.setTitle(prompt + (SEEK_INT ? Integer.toString(Math.round(MIN + progress * STEP)) : String.format("%.02f", MIN + progress * STEP)));
                            }
                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                        promptStack.add(handle);
                    }
                    break;
                }
            }
        }
        if (!ErrorCondition.isEmpty())
            Toast.makeText(context, ErrorCondition, Toast.LENGTH_SHORT).show();
        else if (promptStack.isEmpty())
            Log_helper();
        else
            promptStack.remove().show();
        return true;
    }
    private void Log_helper() { //Prompt syntax: some string %s %s %s!text,lbl!number,lbl2!pick,lbl3,choice1,choice2!!!bgcolor,fgcolor
        String entry = null;
        try {
            switch (logComList.length) {
                case 1: entry = logComList[0]; break;
                case 2: entry = String.format(logComList[0], logComList[1]); break;
                case 3: entry = String.format(logComList[0], logComList[1], logComList[2]); break;
                case 4: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3]); break;
                case 5: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4]); break;
                case 6: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5]); break;
                case 7: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5],logComList[6]); break;
                case 8: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5],logComList[6],logComList[7]); break;
                case 9: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5],logComList[6],logComList[7],logComList[8]); break;
                case 10: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5],logComList[6],logComList[7],logComList[8],logComList[9]); break;
                case 11: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5],logComList[6],logComList[7],logComList[8],logComList[9],logComList[10]); break;
                case 12: entry = String.format(logComList[0], logComList[1], logComList[2], logComList[3], logComList[4],logComList[5],logComList[6],logComList[7],logComList[8],logComList[9],logComList[10],logComList[11]); break;
                default: entry = logComList[0]; break;
            }
            getSupportActionBar().setTitle(entry.split(">")[1]);
        } catch (NullPointerException npe) {
            //cannot change actionbar font
        } catch (Exception e) {
            Toast.makeText(context, "Syntax error: wrong number of parameters", Toast.LENGTH_SHORT).show();
        }
        if (entry != null) {
            File internalFile = new File(getFilesDir(), LOG_FILE_NAME);
            try {
                FileOutputStream out = new FileOutputStream(internalFile, true);
                out.write(entry.getBytes());
                out.write(System.getProperty("line.separator").getBytes());
                out.close();
            } catch (Exception e) {
                Toast.makeText(this, "Internal Storage Write Error!", Toast.LENGTH_LONG).show();
            }
        }
    }
}