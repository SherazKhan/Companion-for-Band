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

package com.pimp.companionforband.fragments.cloud;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pimp.companionforband.R;
import com.pimp.companionforband.activities.cloud.CloudConstants;
import com.pimp.companionforband.activities.main.MainActivity;
import com.pimp.companionforband.utils.UIUtils;
import com.pimp.companionforband.utils.jsontocsv.parser.JsonFlattener;
import com.pimp.companionforband.utils.jsontocsv.writer.CSVWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    TextView profileTV, devicesTV;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileTV = (TextView) view.findViewById(R.id.profile_textview);
        devicesTV = (TextView) view.findViewById(R.id.devices_textview);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest profileRequest = new JsonObjectRequest(Request.Method.GET,
                CloudConstants.BASE_URL + CloudConstants.Profile_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JsonFlattener parser = new JsonFlattener();
                        CSVWriter writer = new CSVWriter();
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                        try {
                            List<LinkedHashMap<String, String>> flatJson = parser.parseJson(response.toString());
                            writer.writeAsCSV(flatJson, path + File.separator + "CompanionForBand"
                                    + File.separator + "profile.csv");
                        } catch (Exception e) {
                            Log.e("profileFragParseJson", e.toString());
                        }

                        Iterator<String> stringIterator = response.keys();
                        while (stringIterator.hasNext()) {
                            try {
                                String key = stringIterator.next();
                                profileTV.append(UIUtils.splitCamelCase(key) + " : ");
                                profileTV.append(response.get(key).toString() + "\n\n");
                            } catch (Exception e) {
                                profileTV.append(e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                profileTV.setText(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + MainActivity.sharedPreferences
                        .getString("access_token", "hi"));

                return headers;
            }
        };

        JsonObjectRequest devicesRequest = new JsonObjectRequest(Request.Method.GET,
                CloudConstants.BASE_URL + CloudConstants.Devices_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Iterator<String> stringIterator = response.keys();
                        while (stringIterator.hasNext()) {
                            try {
                                String key = stringIterator.next();
                                if (key.equals("deviceProfiles")) {
                                    JSONArray jsonArray = response.getJSONArray("deviceProfiles");

                                    JsonFlattener parser = new JsonFlattener();
                                    CSVWriter writer = new CSVWriter();
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                    try {
                                        List<LinkedHashMap<String, String>> flatJson = parser.parseJson(jsonArray.toString());
                                        writer.writeAsCSV(flatJson, path + File.separator + "CompanionForBand"
                                                + File.separator + "devices.csv");
                                    } catch (Exception e) {
                                        Log.e("profileDevicesParseJson", e.toString());
                                    }

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject device = jsonArray.getJSONObject(i);
                                        Iterator<String> iterator = device.keys();
                                        while (iterator.hasNext()) {
                                            key = iterator.next();
                                            devicesTV.append(UIUtils.splitCamelCase(key) + " : ");
                                            devicesTV.append(device.get(key).toString() + "\n");
                                        }
                                        devicesTV.append("\n\n");
                                    }
                                } else {
                                    devicesTV.append(UIUtils.splitCamelCase(key) + " : ");
                                    devicesTV.append(response.get(key).toString() + "\n\n");
                                }
                            } catch (Exception e) {
                                devicesTV.append(e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                devicesTV.setText(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + MainActivity.sharedPreferences
                        .getString("access_token", "hi"));

                return headers;
            }
        };

        queue.add(profileRequest);
        queue.add(devicesRequest);
    }
}
