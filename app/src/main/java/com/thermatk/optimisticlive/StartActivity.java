package com.thermatk.optimisticlive;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thermatk.optimisticlive.exo.ExoPlayerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class StartActivity extends AppCompatActivity {

    private Handler handler;
    List<Map<String, Object>> loadedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        handler = new Handler();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendRequest();
    }


    protected void addItem(List<Map<String, Object>> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }

    private void sendRequest() {
        AsyncHttpClient client = new AsyncHttpClient();

        client.addHeader("Accept", "application/tvrain.api.2.8+json");
        client.addHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        client.addHeader("Accept-Encoding", "gzip, deflate");
        client.addHeader("X-User-Agent", "TV Client (Browser); API_CONSUMER_KEY=a908545f-80af-4f99-8dac-fb012cec");
        client.addHeader("Content-Type", "application/x-www-form-urlencoded");
        client.addHeader("X-Result-Define-Thumb-Width", "200");
        client.addHeader("X-Result-Define-Thumb-height", "110");
        client.addHeader("Referer", "http://smarttv.tvrain.ru/");
        client.addHeader("Origin", "http://smarttv.tvrain.ru");
        client.addHeader("Connection", "keep-alive");

        RequestParams params = new RequestParams();
        client.get("https://api.tvrain.ru/api_v2/live/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Ответ от серверов Дождя получен",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                JSONArray qualities = null;
                try {
                    qualities = response.getJSONArray("HLS_SMARTTV_TEST");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadedData = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < qualities.length(); i++) {
                    JSONObject json_data = null;
                    try {
                        json_data = qualities.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String labelq = null;
                    try {
                        labelq = json_data.getString("label");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String urlq = null;
                    try {
                        urlq = json_data.getString("url");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    addItem(loadedData, labelq, new Intent(getApplicationContext(), ExoPlayerActivity.class).setData(Uri.parse(urlq)));
                    Log.d("TVRAINXD", labelq + " " + urlq);
                }
                ListView myList = (ListView) findViewById(R.id.listView);

                myList.setAdapter(new SimpleAdapter(getApplicationContext(), loadedData, android.R.layout.simple_list_item_1, new String[]{"title"}, new int[]{android.R.id.text1}));
                myList.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> arg0, View view,
                                                    int position, long id) {
                                Map<String, Object> map = (Map<String, Object>) arg0.getItemAtPosition(position);
                                Intent intent = (Intent) map.get("intent");
                                startActivity(intent);
                            }
                        }
                );
            }
        });
    }
}
