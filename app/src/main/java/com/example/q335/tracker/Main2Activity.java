package com.example.q335.tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.List;

public class Main2Activity extends AppCompatActivity implements Buttons.OnFragmentInteractionListener, GrapherFragment.OnFragmentInteractionListener {
    public void processNewLogEntry(String E) {
        GF.processNewEntry(E);
    }
    public void onFragmentInteraction(Uri uri) {
        //TODO: Implement fragment listeners
    }

    Context context;
    SharedPreferences sprefs;

    GrapherFragment GF;
    Buttons BF;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        context = getApplicationContext();
        sprefs = getApplicationContext().getSharedPreferences("TrackerPrefs", MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        BF = new Buttons();
        GF = new GrapherFragment();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),BF,GF);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
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

    private static final String LOG_FILE = "log.txt";
    private static final String COMMANDS_FILE = "commands.json";
    private static final String EXT_STORAGE_DIR = "tracker";
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
                                            BF.loadCommands(jsonText);
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
                                            GF.update();
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
                                            BF.loadCommands(jsonText);
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
                                            GF.update();
                                            Toast.makeText(context, LOG_FILE + " import successful", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                            Toast.makeText(context, "Verify storage permission (Settings > Apps > tracker > Permissions)", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }).show();
                }
                break;
            }
            case R.id.menuItemGraph:
                //startActivity(new Intent(this, GrapherActivity.class));
                startActivity(new Intent(this, Main2Activity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Fragment F0;
        Fragment F1;

        public SectionsPagerAdapter(FragmentManager fm, Fragment F0, Fragment F1) {
            super(fm);
            this.F0 = F0;
            this.F1 = F1;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return BF;
            else
                return GF;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }
    }
}
