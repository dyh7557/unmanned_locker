package com.example.unmannedlocker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class locker_join extends AppCompatActivity {
    private ListView listView;
    private Button locker_search;
    private TextView search_hint;
    private EditText locker_id_input;
    private list_key_adaper adapter;
    private ArrayList<String> addedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_join);

        listView = findViewById(R.id.locker_list);
        locker_search = findViewById(R.id.locker_search);
        search_hint = findViewById(R.id.search_hint);
        locker_id_input = findViewById(R.id.locker_id);

        // 어댑터 초기화
        adapter = new list_key_adaper(this);
        listView.setAdapter(adapter);
        addedItems = new ArrayList<>();

        locker_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.remove_list();
                addedItems.clear();
                new RetrieveData().execute();
            }
        });
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list_key clickedItem = (list_key) parent.getItemAtPosition(position);
                String locationName = clickedItem.getKeyName(); // 위치 이름
                String address = clickedItem.getPlace(); // 주소
                Intent intent = new Intent(locker_join.this, card_infomation.class);
                intent.putExtra("name", locationName);
                intent.putExtra("place", address);
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
                    String locker_id = locker_id_input.getText().toString().trim(); // Get user input

                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String locationName = jsonObject.getString("location_name");
                        String address = jsonObject.getString("address");
                        int status = jsonObject.getInt("status");

                        // Check if the address contains the user input keyword
                        if (address.contains(locker_id)) {
                            String item = locationName + address;
                            if (!addedItems.contains(item)) {
                                int availableCount = 0;
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    JSONObject innerObject = jsonArray.getJSONObject(j);
                                    if (innerObject.getString("location_name").equals(locationName) && innerObject.getInt("status") == 0) {
                                        availableCount++;
                                    }
                                }
                                adapter.add_list(locationName, address, "여유 " + availableCount + "개");
                                addedItems.add(item);
                            }
                        }
                    }
                    // 어댑터에 변경 사항을 알림
                    adapter.notifyDataSetChanged();
                    search_hint.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(locker_join.this, "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 데이터를 가져오지 못한 경우 처리
                Toast.makeText(locker_join.this, "데이터를 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
