/*
 * Companion for Band
 * Copyright (C) 2016  Adithya J
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.pimp.companionforband.utils.band.listeners;

import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.opencsv.CSVWriter;
import com.pimp.companionforband.R;
import com.pimp.companionforband.activities.main.MainActivity;
import com.pimp.companionforband.fragments.sensors.SensorActivity;
import com.pimp.companionforband.fragments.sensors.SensorsFragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class AccelerometerEventListener implements BandAccelerometerEventListener {

    private TextView textView;
    private boolean graph;

    public void setViews(TextView textView, boolean graph) {
        this.textView = textView;
        this.graph = graph;
    }

    @Override
    public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
        if (event != null) {
            if (graph)
                MainActivity.sActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SensorActivity.chartAdapter.add(event.getAccelerationX());
                    }
                });

            SensorsFragment.appendToUI(String.format(" X = %.3f (m/s²) \n Y = %.3f (m/s²)\n Z = %.3f (m/s²)",
                    event.getAccelerationX(),
                    event.getAccelerationY(),
                    event.getAccelerationZ()), textView);

            if (MainActivity.sharedPreferences.getBoolean("log", false)) {
                MainActivity.bandSensorData.setAccelerometerData(event);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CompanionForBand" + File.separator + "Accelerometer");
                if (file.exists() || file.isDirectory()) {
                    try {
                        Date date = new Date();
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                        if (new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CompanionForBand" + File.separator + "Accelerometer" + File.separator + "Accelerometer_" + DateFormat.getDateInstance().format(date) + ".csv").exists()) {
                            String str = DateFormat.getDateTimeInstance().format(event.getTimestamp());
                            CSVWriter csvWriter = new CSVWriter(new FileWriter(path + File.separator + "CompanionForBand" + File.separator + "Accelerometer" + File.separator + "Accelerometer_" + DateFormat.getDateInstance().format(date) + ".csv", true));
                            csvWriter.writeNext(new String[]{String.valueOf(event.getTimestamp()),
                                    str, String.valueOf(event.getAccelerationX()),
                                    String.valueOf(event.getAccelerationY()),
                                    String.valueOf(event.getAccelerationZ())});

                            csvWriter.close();
                        } else {
                            CSVWriter csvWriter = new CSVWriter(new FileWriter(path + File.separator + "CompanionForBand" + File.separator + "Accelerometer" + File.separator + "Accelerometer_" + DateFormat.getDateInstance().format(date) + ".csv", true));
                            csvWriter.writeNext(new String[]{MainActivity.sContext.getString(R.string.timestamp), MainActivity.sContext.getString(R.string.date_time), "X", "Y", "Z"});
                            csvWriter.close();
                        }
                    } catch (IOException e) {
                        Log.e("CSV", e.toString());
                    }
                } else {
                    file.mkdirs();
                }
            }
        }
    }
}
