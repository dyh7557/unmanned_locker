package com.example.unmannedlocker;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class card_infomation extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout lockerContainer;
    private TextView availableLockersText, name, place;
    private ScrollView scrollView; // ScrollView 변수 추가
    private GoogleMap map;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_infomation);

        name = (TextView)findViewById(R.id.infomationName);
        place = (TextView)findViewById(R.id.infomationPlace);
        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        place.setText(intent.getStringExtra("place"));

        lockerContainer = findViewById(R.id.lockerContainer);
        availableLockersText = findViewById(R.id.availableLockersText);
        scrollView = findViewById(R.id.scrollView); // ScrollView 초기화

        // 네트워크 작업 시작
        new RetrieveData().execute("http://jukson.dothome.co.kr/get_lockers.php");

        Button button_cardInfomationBack = findViewById(R.id.button_cardInfomationBack);
        button_cardInfomationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 기능
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLng PLACE = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(PLACE);
                name = (TextView)findViewById(R.id.infomationName);
                markerOptions.title(name.getText().toString());
                place = (TextView)findViewById(R.id.infomationPlace);
                markerOptions.snippet(place.getText().toString());

                map.addMarker(markerOptions);

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(PLACE, 17));
            }
        }, 1000);
    }

    private class RetrieveData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return getData(urls[0]);
            } catch (IOException e) {
                Log.e("RetrieveData", "데이터 가져오기 오류", e);
                return null;
            }
        }

        // RetrieveData의 onPostExecute 메서드 내에서 수정
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(card_infomation.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(result);
                int availableLockersCount = 0;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String locationName = object.getString("location_name"); // 데이터베이스의 location_name 가져오기

                    // 사용자가 선택한 name과 location_name이 일치하는 경우에만 처리
                    if (name.getText().toString().equals(locationName)) {
                        String id = object.getString("id");
                        String status = object.getString("status");
                        latitude = object.getDouble("latitude");
                        longitude = object.getDouble("longitude");

                        addButtonToContainer(id, status.equals("0"));
                        if (status.equals("0")) {
                            availableLockersCount++;
                        }
                    }
                }

                availableLockersText.setText("사용 가능한 사물함 " + availableLockersCount + "개");
            } catch (JSONException e) {
                Log.e("RetrieveData", "JSON 파싱 오류", e);
                Toast.makeText(card_infomation.this, "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private String getData(String urlString) throws IOException {
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.connect();

            inputStream = conn.getInputStream();
            return convertStreamToString(inputStream);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private void addButtonToContainer(String id, boolean available) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        button.setLayoutParams(params);
        button.setText("사물함 " + id + ": " + (available ? "사용 가능" : "사용 불가"));
        button.setEnabled(available); // 사용 불가능한 경우 버튼 비활성화

        if (available) {
            button.setOnClickListener(v -> {
                Intent intent = new Intent(card_infomation.this, popup_locker.class);
                intent.putExtra("locker_id", id);
                name = (TextView)findViewById(R.id.infomationName);
                intent.putExtra("name", name.getText());
                place = (TextView)findViewById(R.id.infomationPlace);
                intent.putExtra("place", place.getText());
                startActivityForResult(intent, 1);
            });
        }
        lockerContainer.addView(button);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1)
        {
            recreate();
        }
    }
}