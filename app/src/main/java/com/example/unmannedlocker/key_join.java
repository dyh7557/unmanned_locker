package com.example.unmannedlocker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class key_join extends AppCompatActivity {

    private ListView listView;
    private list_key_adaper adapter;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_join);

        listView = findViewById(R.id.list_key);
        back = findViewById(R.id.button_keyBack);

        adapter = new list_key_adaper(this);
        listView.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new RetrieveData().execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(key_join.this, popup_key.class);
                list_key clickedItem = (list_key) parent.getItemAtPosition(position);
                String name = clickedItem.getKeyName();
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });
    }

    private class RetrieveData extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://jukson.dothome.co.kr/get_lockers.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return result.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String uid = jsonObject.getString("use_uid");
                        if (uid.equals("ff412f")) {
                            String locationName = jsonObject.getString("location_name");
                            String address = jsonObject.getString("address");
                            String id = jsonObject.getString("id");
                            String endDate = jsonObject.getString("end_date");

                            adapter.add_list(locationName, address + " 사물함" + id, "만료일자: " + endDate);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(key_join.this, "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(key_join.this, "데이터를 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
