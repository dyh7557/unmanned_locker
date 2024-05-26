package com.example.unmannedlocker;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Button button_lockerJoin;
    private Button button_cardJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            // NFC를 지원하지 않는 경우
            Toast.makeText(this, "이 디바이스는 NFC를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        } else {
            // NFC 상태 확인
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC가 꺼져 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        button_lockerJoin = (Button)findViewById(R.id.button_lockerJoin);
        button_cardJoin = (Button)findViewById(R.id.button_cardJoin);

        //사물함 조회는 인탠트로 전달할 내용이 없음
        button_lockerJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, locker_join.class);
                startActivity(intent);
            }
        });

        //키 조회는 자신의 키를 액티비티 전환을 하면서 띄워야 해서 DB에서 자신이 보유한 키를 받아 올 필요가 있음
        button_cardJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, key_join.class);
                startActivity(intent);
            }
        });
    }
}
//커스텀 리스트 3개 1. 이름(대제목), 장소(소제목) 2. 보관함명(대제목) + 번호, 사용가능여부(오른 정렬) 3. 키이름(대제목), 장소(소제목) + 보관함명(소제목) + 번호, 사용가능기간(오른 정렬)