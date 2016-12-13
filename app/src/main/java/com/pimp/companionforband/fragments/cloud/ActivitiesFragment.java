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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActivitiesFragment extends Fragment {

    TextView statusTV;
    ListView activitiesLV;
    ArrayAdapter<String> stringArrayAdapter;
    ArrayList<String> stringArrayList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activities, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activitiesLV = (ListView) view.findViewById(R.id.activities_listview);
        statusTV = (TextView) view.findViewById(R.id.status_textview);
        stringArrayList = new ArrayList<>();
        stringArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.activities_list_item,
                R.id.list_item_textView, stringArrayList);
        activitiesLV.setAdapter(stringArrayAdapter);

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest activitiesRequest = new JsonObjectRequest(Request.Method.GET,
                CloudConstants.BASE_URL + CloudConstants.Activities_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        statusTV.setText("CSV file can be found in CompanionForBand/Activities\n");

                        Iterator<String> stringIterator = response.keys();
                        while (stringIterator.hasNext()) {
                            try {
                                String key = stringIterator.next();
                                JSONArray jsonArray = response.getJSONArray(key);

                                String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                                        + File.separator + "CompanionForBand"
                                        + File.separator + "Activities";
                                File file = new File(path);
                                file.mkdirs();

                                JsonFlattener parser = new JsonFlattener();
                                CSVWriter writer = new CSVWriter();
                                try {
                                    List<LinkedHashMap<String, String>> flatJson = parser.parseJson(jsonArray.toString());
                                    writer.writeAsCSV(flatJson, path + File.separator + key + ".csv");
                                } catch (Exception e) {
                                    Log.e("ActivitiesParseJson", e.toString());
                                }

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject activity = jsonArray.getJSONObject(i);
                                    Iterator<String> iterator = activity.keys();
                                    String str = "";
                                    while (iterator.hasNext()) {
                                        key = iterator.next();
                                        str = str + UIUtils.splitCamelCase(key) + " : "
                                                + activity.get(key).toString() + "\n";
                                    }
                                    stringArrayAdapter.add(str);
                                }
                            } catch (Exception e) {
                                Log.e("Activities", e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
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

        queue.add(activitiesRequest);
    }
}
