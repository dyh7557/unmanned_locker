package com.example.unmannedlocker;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class popup_locker extends Activity {

    private Button back, key_create;
    private TextView name, index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_locker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back = findViewById(R.id.popup_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        name = findViewById(R.id.popup_name);
        index = findViewById(R.id.popup_index);
        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        index.setText(intent.getStringExtra("place") + " 사물함" + intent.getStringExtra("locker_id"));

        key_create = findViewById(R.id.key_create);
        key_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lockerId = intent.getStringExtra("locker_id");
                insertData(lockerId);
                setResult(1, intent);
                finish();
            }
        });
    }

    private void insertData(String lockerId) {
        new InsertDataTask().execute(lockerId);
    }

    private String calculateEndDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String endDate = sdf.format(calendar.getTime());
        return endDate;
    }

    private class InsertDataTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            try {
                String lockerId = params[0];
                String useUid = "ff412f"; // Changed to the desired format
                String endDate = calculateEndDate();
                String status = "1"; // New status parameter

                String postData = URLEncoder.encode("locker_id", "UTF-8") + "=" + URLEncoder.encode(lockerId, "UTF-8")
                        + "&" + URLEncoder.encode("use_uid", "UTF-8") + "=" + URLEncoder.encode(useUid, "UTF-8")
                        + "&" + URLEncoder.encode("end_date", "UTF-8") + "=" + URLEncoder.encode(endDate, "UTF-8")
                        + "&" + URLEncoder.encode("status", "UTF-8") + "=" + URLEncoder.encode(status, "UTF-8"); // Added status

                URL url = new URL("http://jukson.dothome.co.kr/insert_locker_data.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                return conn.getResponseCode();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode != null && responseCode == HttpURLConnection.HTTP_OK) {
                // Data insertion successful
                Toast.makeText(popup_locker.this, "키 발급이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // Error handling
                Toast.makeText(popup_locker.this, "데이터 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
