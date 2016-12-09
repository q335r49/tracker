package com.example.q335.tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final Context context = this;
    SharedPreferences sprefs;
    private GridView mainView;
    private List<String[]> commands = new ArrayList<String[]>();
    private static final String LOG_FILE = "log.txt";
    private static final String COMMANDS_FILE = "commands.json";
    private static final String EXT_STORAGE_DIR = "tracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = (GridView) findViewById(R.id.GV);
        sprefs = getApplicationContext().getSharedPreferences("TrackerPrefs", MODE_PRIVATE);

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
        SimpleAdapter LVadapter = new SimpleAdapter(this, LVentries, R.layout.gv_list_item,
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

    public static void writeString(File file, String data) throws Exception {
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(data.getBytes());
        } finally {
            stream.close();
        }
    }
    public static String readString(File file) throws Exception {
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
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemExport: {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                final String extStorPath = Environment.getExternalStorageDirectory() + File.separator + EXT_STORAGE_DIR + File.separator;
                final File directory = new File(extStorPath);
                directory.mkdirs();
                final File outputLog = new File(extStorPath, LOG_FILE);
                final File outputCmd = new File(extStorPath, COMMANDS_FILE);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Choose files to import")
                        .setMessage("Exporting to " + extStorPath)
                        .setNeutralButton("command.json", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    writeString(outputCmd, sprefs.getString("commands", ""));
                                    Toast.makeText(context, COMMANDS_FILE + " exported to " + extStorPath, Toast.LENGTH_SHORT).show();
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
                                    copyFile(new File(getFilesDir(), "log.txt"), outputLog);
                                    Toast.makeText(context, LOG_FILE + " exported to " + extStorPath, Toast.LENGTH_SHORT).show();
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
                                    copyFile(new File(getFilesDir(), "log.txt"), outputLog);
                                    writeString(outputCmd, sprefs.getString("commands", ""));
                                    Toast.makeText(context, LOG_FILE + " and commands.json exported to " + extStorPath, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                break;
            }
            case R.id.menuItemImport: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                final String extStorPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                final File directory = new File(extStorPath);
                final File inputCmd = new File(extStorPath, COMMANDS_FILE);
                final File inputLog = new File(extStorPath, LOG_FILE);

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
                                            commands = new Gson().fromJson(jsonText, listType);
                                            makeView();
                                            Toast.makeText(context, COMMANDS_FILE + " import successful", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(context, LOG_FILE + " import successful", Toast.LENGTH_SHORT).show();
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
                                            commands = new Gson().fromJson(jsonText, listType);
                                            makeView();
                                            Toast.makeText(context, COMMANDS_FILE + " import successful", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(context, "Import failed:" + e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    if (!inputLog.exists()) {
                                        Toast.makeText(context, LOG_FILE + " failed: no file", Toast.LENGTH_SHORT).show();
                                    } else {
                                        try {
                                            copyFile(inputLog, new File(getFilesDir(), "log.txt"));
                                            Toast.makeText(context, LOG_FILE + " import successful", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                            Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                            .show();
                }
                break;
            }
            case R.id.menuItemGraph:
                startActivity(new Intent(this, GrapherActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    final static int COMMENT_POS = 0;
    final static int COLOR_POS = 1;
    final static int START_POS = 2;
    final static int END_POS = 3;
//    private String[] logComList;
//    private Queue<AlertDialog> promptStack;

    public boolean newLogEntry(int position) {
        String[] args = commands.get(position);
        Date now = new Date();
        String entry = Long.toString(System.currentTimeMillis() / 1000) + ">" + now.toString() + ">" + args[COLOR_POS] + ">" + args[START_POS] + ">" + args[END_POS] + ">" + args[COMMENT_POS];
        File internalFile = new File(getFilesDir(), LOG_FILE);
        try {
            FileOutputStream out = new FileOutputStream(internalFile, true);
            out.write(entry.getBytes());
            out.write(System.getProperty("line.separator").getBytes());
            out.close();
            getSupportActionBar().setTitle(args[COMMENT_POS]);
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Internal Storage Write Error!", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
        /*
        String ErrorCondition = "";
        logComList = args[COMMENT_POS].split("!");
        promptStack = new LinkedList<>();
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
            File internalFile = new File(getFilesDir(), LOG_FILE);
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
*/