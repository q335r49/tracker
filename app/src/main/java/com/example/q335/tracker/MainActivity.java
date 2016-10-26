package com.example.q335.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private List<String[]> Events = new ArrayList<String[]>();
    final Context context = this;
    private List<Map<String,String>> LVCommands;
    private SimpleAdapter LVadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Events.add(new String[] {"A", "Another thing"});
        Events.add(new String[] {"B", "Banother thing"});
        Events.add(new String[] {"C", "Canother thing"});
        Events.add(new String[] {"D", "Danother thing"});
        Events.add(new String[] {"E", "Enother thing"});
        Events.add(new String[] {"F", "Fanother thing"});
        Events.add(new String[] {"G", "Ganother thing"});

        LVCommands = new ArrayList<Map<String,String>>();
        for (String[] s: Events) {
            final Map<String,String> listItem = new HashMap<String,String>();
            listItem.put("label", s[0]);
            listItem.put("syntax", s[1]);
            LVCommands.add(listItem);
        }
        final Map<String,String> listItem = new HashMap<String,String>();
        listItem.put("label", "New Command");
        listItem.put("syntax", "Long press to add a new command");
        LVCommands.add(listItem);

        ListView LV = (ListView) findViewById(R.id.LV);
        LVadapter = new SimpleAdapter(this, LVCommands,android.R.layout.simple_list_item_2,
                new String[] {"label", "syntax"},new int[] {android.R.id.text1, android.R.id.text2});
        LV.setAdapter(LVadapter);
        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                if (position<Events.size()) {
                    String text = ((TextView) (view.findViewById(android.R.id.text1))).getText().toString();
                    Log(Events.get(position)[1], "log.txt");
                }
            }
        });
        LV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                final TextView listLabel = ((TextView)(view.findViewById(android.R.id.text1)));
                final TextView listCommand = ((TextView)(view.findViewById(android.R.id.text2)));
                final String label = listLabel.getText().toString();
                final String command = listCommand.getText().toString();
                final int listIndex = pos;

                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.prompts, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptView);

                final EditText labelInput = (EditText) promptView.findViewById(R.id.promptTextView);
                final EditText commandInput = (EditText) promptView.findViewById(R.id.userInput);
                labelInput.setText(label);
                commandInput.setText(command);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (listIndex >= Events.size()) {
                                    Events.add(new String[] {labelInput.getText().toString(), commandInput.getText().toString()});
                                    final Map<String, String> listItem = new HashMap<String, String>();
                                        listItem.put("label", Events.get(listIndex)[0]);
                                        listItem.put("syntax", Events.get(listIndex)[1]);
                                    LVCommands.add(listIndex, listItem);
                                } else {
                                    Events.set(listIndex, new String[]{labelInput.getText().toString(), commandInput.getText().toString()});
                                    final Map<String, String> listItem = new HashMap<String, String>();
                                    listItem.put("label", Events.get(listIndex)[0]);
                                    listItem.put("syntax", Events.get(listIndex)[1]);
                                    LVCommands.set(listIndex, listItem);
                                }
                                LVadapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,	int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertD = alertDialogBuilder.create();
                alertD.show();
                return true;
            }
        });
        //TODO: NEW ITEM entry
        //TODO: Delete entry
        //TODO: Convert Event to array
        //TODO: handle initialization
        //TODO: Delete item in prompt
        //TODO: JSON: Export and edit log and import
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String dstPath = "";
        File src;
        File dst;
        boolean success;

        switch (item.getItemId()) {
            case R.id.menuItemExport:
                dstPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                dst = new File(dstPath,"log.txt");
                src = new File(getFilesDir(),"log.txt");
                try {
                    exportFile(src, dst);
                } catch (Exception e) {
                    Toast.makeText(this, "Log Export Failed!", Toast.LENGTH_SHORT).show();
                }
                success = saveSharedPreferencesToFile(new File(dstPath,"prefs.txt"));
                if (!success)
                    Toast.makeText(this, "Export preferences failed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menuItemImport:
                dstPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                src = new File(dstPath,"prefs.txt");
                success = loadSharedPreferencesFromFile(src);
                if (!success)
                    Toast.makeText(this, "Import failed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menuItemGraph:
                //TODO: Graph
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    private File exportFile(File src, File dst) throws IOException {
        //TODO: Request Permissions

        //if folder does not exist
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return dst;
    }

    private boolean saveSharedPreferencesToFile(File dst) {
//        boolean res = false;
//        ObjectOutputStream output = null;
//        try {
//            output = new ObjectOutputStream(new FileOutputStream(dst));
//            output.writeObject(pref.getAll());
//            res = true;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (output != null) {
//                    output.flush();
//                    output.close();
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return res;
        return true;
    }

    @SuppressWarnings({ "unchecked" })
    private boolean loadSharedPreferencesFromFile(File src) {
//        boolean res = false;
//        ObjectInputStream input = null;
//        try {
//            input = new ObjectInputStream(new FileInputStream(src));
//            SharedPreferences.Editor prefEdit = Events.edit();
//            prefEdit.clear();
//            Map<String, ?> entries = (Map<String, ?>) input.readObject();
//            for (Map.Entry<String, ?> entry : entries.entrySet()) {
//                Object v = entry.getValue();
//                String key = entry.getKey();
//
//                if (v instanceof Boolean)
//                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
//                else if (v instanceof Float)
//                    prefEdit.putFloat(key, ((Float) v).floatValue());
//                else if (v instanceof Integer)
//                    prefEdit.putInt(key, ((Integer) v).intValue());
//                else if (v instanceof Long)
//                    prefEdit.putLong(key, ((Long) v).longValue());
//                else if (v instanceof String)
//                    prefEdit.putString(key, ((String) v));
//            }
//            prefEdit.commit();
//            res = true;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }finally {
//            try {
//                if (input != null) {
//                    input.close();
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return res;
        return true;
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