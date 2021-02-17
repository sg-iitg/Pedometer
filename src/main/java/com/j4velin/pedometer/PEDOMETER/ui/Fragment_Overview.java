/*
 * Copyright 2014 Thomas Hoffmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.j4velin.pedometer.PEDOMETER.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.j4velin.pedometer.BuildConfig;
import com.j4velin.pedometer.MainActivity;
import com.j4velin.pedometer.PEDOMETER.Database;
import com.j4velin.pedometer.R;
import com.j4velin.pedometer.PEDOMETER.SensorListener;
import com.j4velin.pedometer.PEDOMETER.util.API26Wrapper;
import com.j4velin.pedometer.PEDOMETER.util.Logger;
import com.j4velin.pedometer.PEDOMETER.util.Util;

public class Fragment_Overview extends Fragment implements SensorEventListener {

    private TextView stepsView, totalView, averageView;
    private PieModel sliceGoal, sliceCurrent;
    private PieChart pg;
    File imagepath;
    File file=null;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    Button share;
    String Date;
     Button button;
    private TextView progresstext;
    int steps = 0;
    String currentDateTimeString;
    int perc;
    Dialog dialog;

    private int todayOffset, total_start, goal, since_boot, total_days;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
    private boolean showSteps = true;
    private Button stopbtn;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (Build.VERSION.SDK_INT >= 26) {
            API26Wrapper.startForegroundService(getActivity(),
                    new Intent(getActivity(), SensorListener.class));
        } else {
            getActivity().startService(new Intent(getActivity(), SensorListener.class));
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_overview, null);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        stepsView = (TextView) v.findViewById(R.id.steps);
        totalView = (TextView) v.findViewById(R.id.total);
        averageView = (TextView) v.findViewById(R.id.average);
        stopbtn=v.findViewById(R.id.stopbtn);
        pg = (PieChart) v.findViewById(R.id.graph);
        ImageView img = (ImageView) v.findViewById(R.id.imageView);
        //Button btn = v.findViewById(R.id.animate);
        SharedPreferences run = getActivity().getSharedPreferences("Database", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = run.edit();
        if (run.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            run.edit().putBoolean("firstrun", false).apply();
            editor.putBoolean("today",false).apply();
            editor.putBoolean("tomorrow",false).apply();
            editor.putBoolean("dayaftertomorrow",false).apply();
            editor.putLong("MinimumTime",10000000).apply();
        }
         currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        Date=currentDateTimeString.substring(4,6);
        Log.d("vipin",currentDateTimeString+"/"+Date);
        Toast.makeText(getActivity(), "TODAY's Date = "+currentDateTimeString, Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(this.getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Animation animation = AnimationUtils.loadAnimation(getActivity(),
//                        R.anim.fadein);
//
//                img.setVisibility(View.VISIBLE);
//                img.startAnimation(animation);
//
//            }
//        });

        share=v.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);}
//                Bitmap bitmap=takescreen();
//                saveBitmap(bitmap);
//                shareit();
                View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                Bitmap bmp=getScreenShot(rootView);
                store(bmp,"file.png");
                shareImage(file);
            }
        });

        Chronometer mStopWatch = (Chronometer) v.findViewById(R.id.chronometer);
        mStopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                if(s>20){stopbtn.setEnabled(false); stopbtn.setClickable(false);}
                if(Integer.parseInt(stepsView.getText().toString())>30){
                    Toast.makeText(getActivity(), "You Completed Your Journey in "+hh+":"+mm+":"+ss+" time!", Toast.LENGTH_LONG).show();
                    //makedialog();
                    button.setClickable(false);
                    button.setEnabled(false);
                    stopbtn.setClickable(false);
                    stopbtn.setEnabled(false);
                    mStopWatch.stop();
                    long MinimumTime=run.getLong("MinimumTime",1000000);
                    MinimumTime= Math.min(MinimumTime, time);
                     h   = (int)(MinimumTime /3600000);
                     m = (int)(MinimumTime - h*3600000)/60000;
                     s= (int)(MinimumTime - h*3600000- m*60000)/1000 ;
                     hh = h < 10 ? "0"+h: h+"";
                     mm = m < 10 ? "0"+m: m+"";
                     ss = s < 10 ? "0"+s: s+"";

                    addToDatabase(firebaseAuth.getUid(),"vipin",hh+":"+mm+":"+ss);
                    if(Date.equals("28")) {
                        editor.putBoolean("today", true).apply();

                        editor.putLong("MinimumTime",MinimumTime);
                    }
                    else if(Date.equals("11")) {

                        editor.putBoolean("tomorrow", true).apply();
                        editor.putLong("MinimumTime",MinimumTime);
                    }
                    else if(Date.equals("12")) {
                        editor.putLong("MinimumTime",MinimumTime);
                        editor.putBoolean("dayaftertomorrow", true).apply();
                    }
                    if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    {ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);}
//                Bitmap bitmap=takescreen();
//                saveBitmap(bitmap);
//                shareit();
                    View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                    Bitmap bmp=getScreenShot(rootView);
                    store(bmp,"file.png");
                    shareImage(file);
                    //UPLOAD TO DATABASE TIME
                    //String time=hh+":"+mm+":"+ss;
                }
                cArg.setText(hh+":"+mm+":"+ss);
            }


        });

        mStopWatch.setBase(SystemClock.elapsedRealtime());
         button  =  v.findViewById(R.id.startstop);
        button.setTag(1);
        button.setText("START");
        stopbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                    mStopWatch.setBase(SystemClock.elapsedRealtime());
                    mStopWatch.stop();
                    button.setText("START");
                    set_to_zero();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        set_to_zero();
                    }
                }, 2000);
            }
        });
        ////checking values for shared prefrenece
        boolean today=run.getBoolean("today",true);
        boolean tomorrow=run.getBoolean("today",true);

        boolean dayaftertomorrow=run.getBoolean("today",true);
        if(Date.equals("28") && !today)
        {
            button.setEnabled(true);
            button.setClickable(true);

            stopbtn.setClickable(true);
            stopbtn.setEnabled(true);
        }
        else if(Date.equals("11") && !tomorrow)
        {
            button.setEnabled(true);
            button.setClickable(true);
            stopbtn.setClickable(true);
            stopbtn.setEnabled(true);
        }
        else if(Date.equals("12") && !dayaftertomorrow)
        {
            button.setEnabled(true);
            button.setClickable(true);
            stopbtn.setClickable(true);
            stopbtn.setEnabled(true);
        }
        else
        {
            button.setEnabled(false);
            button.setClickable(false);
            stopbtn.setClickable(false);
            stopbtn.setEnabled(false);
        }


        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                button.setText("RESTART");
                final int status =(Integer) v.getTag();
                mStopWatch.setBase(SystemClock.elapsedRealtime());
                mStopWatch.stop();

                if(Date.equals("28")) {
                    if (!today) {
                        Toast.makeText(getActivity(), "I am here", Toast.LENGTH_SHORT).show();
                        mStopWatch.start();

                        v.setTag(0);
                        start_again();
                    }
                }
                else if(Date.equals("11"))
                {
                    if(!tomorrow)
                    {
                        mStopWatch.start();

                        v.setTag(0);
                        start_again();
                    }
                }
                else if(Date.equals("12"))
                {
                    if(!dayaftertomorrow)
                    {
                        mStopWatch.start();

                        v.setTag(0);
                        start_again();
                    }
                }
            }
        });
        // slice for the steps taken today
        sliceCurrent = new PieModel("", 0, Color.parseColor("#99CC00"));
        pg.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        sliceGoal = new PieModel("", Fragment_Settings.DEFAULT_GOAL, Color.parseColor("#CC0000"));
        pg.addPieSlice(sliceGoal);

        pg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        });

        pg.setDrawValueInPie(false);
        pg.setUsePieRotation(true);
        pg.startAnimation();
        return v;
    }
    public void set_to_zero(){
        totalView.setText(String.valueOf(0));
        stepsView.setText(String.valueOf(0));
        averageView.setText(String.valueOf(0));


        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);

        try {
            Database db = Database.getInstance(getActivity());
            SensorListener.getNotification_to_Zero(getActivity());

            db.deleteAll();
            db.addToLastEntry(0);
            db.saveCurrentSteps(0);
            db.close();
        }catch(Exception ex){
            Toast.makeText(getActivity(), "FAILED!!", Toast.LENGTH_SHORT).show();
        }

//        API26Wrapper.startForeground(NOTIFICATION_ID, SensorListener.getNotification_to_Zero(getActivity()));

    }
    public void start_again(){
        set_to_zero();

        Database db = Database.getInstance(getActivity());

        if (BuildConfig.DEBUG) db.logState();
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
        since_boot = db.getCurrentSteps();
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                    .setMessage(R.string.no_sensor_explain)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialogInterface) {
                            getActivity().finish();
                        }
                    }).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        } else {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

        stepsDistanceChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
  //      getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
//
//        Database db = Database.getInstance(getActivity());
//
//        if (BuildConfig.DEBUG) db.logState();
//        // read todays offset
//        todayOffset = db.getSteps(Util.getToday());
//
//        SharedPreferences prefs =
//                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
//
//        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
//        since_boot = db.getCurrentSteps();
//        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);
//
//        // register a sensorlistener to live update the UI if a step is taken
//        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
//        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        if (sensor == null) {
//            new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
//                    .setMessage(R.string.no_sensor_explain)
//                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(final DialogInterface dialogInterface) {
//                            getActivity().finish();
//                        }
//                    }).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(final DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            }).create().show();
//        } else {
//            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
//        }
//
//        since_boot -= pauseDifference;
//
//        total_start = db.getTotalWithoutToday();
//        total_days = db.getDays();
//
//        db.close();
//
//        stepsDistanceChanged();
    }

    /**
     * Call this method if the Fragment should update the "steps"/"km" text in
     * the pie graph as well as the pie and the bars graphs.
     */
    private void stepsDistanceChanged() {
        if (showSteps) {
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
        } else {
            String unit = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                    .getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT);
            if (unit.equals("cm")) {
                unit = "km";
            } else {
                unit = "mi";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
        }

        updatePie();
        updateBars();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
        }
        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_split_count:
                Dialog_Split.getDialog(getActivity(),
                        total_start + Math.max(todayOffset + since_boot, 0)).show();
                return true;
            default:
                return ((MainActivity) getActivity()).optionsItemSelected(item);
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // won't happen
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (BuildConfig.DEBUG) Logger.log(
                "UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                        event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            Database db = Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(), (int) event.values[0]);
            db.close();
        }
        since_boot = (int) event.values[0];
        updatePie();
    }

    /**
     * Updates the pie graph to show todays steps/distance as well as the
     * yesterday and total values. Should be called when switching from step
     * count to distance.
     */
    private void updatePie() {
        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);
        sliceCurrent.setValue(steps_today);
        if (goal - steps_today > 0) {
            // goal not reached yet
            if (pg.getData().size() == 1) {
                // can happen if the goal value was changed: old goal value was
                // reached but now there are some steps missing for the new goal
                pg.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(goal - steps_today);
        } else {
            // goal reached
            pg.clearChart();
            pg.addPieSlice(sliceCurrent);
        }
        pg.update();
        if (showSteps) {
            stepsView.setText(formatter.format(steps_today));
            totalView.setText(formatter.format(total_start + steps_today));
            averageView.setText(formatter.format((total_start + steps_today) / total_days));
        } else {
            // update only every 10 steps when displaying distance
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            float stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            float distance_today = steps_today * stepsize;
            float distance_total = (total_start + steps_today) * stepsize;
            if (prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm")) {
                distance_today /= 100000;
                distance_total /= 100000;
            } else {
                distance_today /= 5280;
                distance_total /= 5280;
            }
            stepsView.setText(formatter.format(distance_today));
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
    }

    /**
     * Updates the bar graph to show the steps/distance of the last week. Should
     * be called when switching from step count to distance.
     */
    private void updateBars() {
        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        BarChart barChart = (BarChart) getView().findViewById(R.id.bargraph);
        if (barChart.getData().size() > 0) barChart.clearChart();
        int steps;
        float distance, stepsize = Fragment_Settings.DEFAULT_STEP_SIZE;
        boolean stepsize_cm = true;
        if (!showSteps) {
            // load some more settings if distance is needed
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            stepsize_cm = prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm");
        }
        barChart.setShowDecimal(!showSteps); // show decimal in distance view only
        BarModel bm;
        Database db = Database.getInstance(getActivity());
        List<Pair<Long, Integer>> last = db.getLastEntries(8);
        db.close();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            if (steps > 0) {
                bm = new BarModel(df.format(new Date(current.first)), 0,
                        steps > goal ? Color.parseColor("#99CC00") : Color.parseColor("#0099cc"));
                if (showSteps) {
                    bm.setValue(steps);
                } else {
                    distance = steps * stepsize;
                    if (stepsize_cm) {
                        distance /= 100000;
                    } else {
                        distance /= 5280;
                    }
                    distance = Math.round(distance * 1000) / 1000f; // 3 decimals
                    bm.setValue(distance);
                }
                barChart.addBar(bm);
            }
        }
        if (barChart.getData().size() > 0) {
            barChart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Dialog_Statistics.getDialog(getActivity(), since_boot).show();
                }
            });
            barChart.startAnimation();
        } else {
            barChart.setVisibility(View.GONE);
        }
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;

    }
    public void store(Bitmap bm, String fileName){
        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();

            Toast toast = Toast.makeText(getActivity(),"FAILED",Toast.LENGTH_SHORT);
        }
    }
    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveBitmap(Bitmap bitmap){
        imagepath=new File(Environment.getExternalStorageDirectory() +"/Pictures/"+"screenshot.jpg");
        FileOutputStream fos;
        String path;
        //File file=new File(path);
        try{
            fos=new FileOutputStream(imagepath);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.flush();
            fos.close();
        } catch(IOException ignored){
        }
    }
    public void shareit(){
        Uri path= FileProvider.getUriForFile(getActivity(),"com.alcheringa.circularprogressbar",imagepath);
        Intent share=new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = Uri.fromFile(imagepath);
        share.putExtra(Intent.EXTRA_STREAM, uri);
//        share.putExtra(Intent.EXTRA_STREAM,path);
        share.setType("image/*");
        startActivity(Intent.createChooser(share,"Share..."));
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri uri = Uri.fromFile(imagepath);
//        intent.setDataAndType(uri,"image/jpeg");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);

    }

    private void makedialog(){
       AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        View mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_option, null);
        Button closebtn;
        closebtn=mView.findViewById(R.id.sports);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }});}


    private void addToDatabase(String uuid,String steps,String time) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("Name",firebaseAuth.getCurrentUser().getDisplayName());
        hashMap.put("Steps",steps);
        hashMap.put("Time", time);
        hashMap.put("Email",firebaseAuth.getCurrentUser().getEmail());
        firebaseDatabase.getReference().child("USERS").child(uuid).updateChildren(hashMap).addOnSuccessListener(aVoid ->
                Toast.makeText(getActivity(), "Succesful", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show());

    }



}
