#include <ArduinoJson.h>
#include <ArduinoJson.hpp>
#include <SoftwareSerial.h>
#include <MFRC522v2.h>
#include <MFRC522DriverSPI.h>
#include <MFRC522DriverPinSimple.h>
#include <MFRC522Debug.h>

#define SDA_PIN 53  // RFID-RC522
#define RST_PIN 5
#define BT_RXD 12  // ESP-01
#define BT_TXD 13
#define TRIG_PIN 9  // 초음파 센서
#define ECHO_PIN 10
#define LOCKER_DISTANCE 12  // 사물함 사이즈에 맞춰 조정

const uint8_t SERVO_PIN{ 8U };  // 서보모터


enum RGB {  //RGB_LED
  RED = 22,
  GREEN,
  BLUE
};

#define SSID "incheon"       //네트워크 이름
#define PASSWORD "12345678"  //네트워크 비밀번호

String cmd;
String cardUID;
int mode = 0;

MFRC522DriverPinSimple sda_pin(SDA_PIN);
MFRC522DriverSPI driver = sda_pin;
MFRC522 mfrc522(driver);

SoftwareSerial ESP_wifi(BT_RXD, BT_TXD);

void rgb_set(uint8_t red, uint8_t green, uint8_t blue) {  // rgb_led 세팅용 함수
  analogWrite(RED, red);
  analogWrite(GREEN, green);
  analogWrite(BLUE, blue);
}

boolean db_key(String char_input) {  // DB에 키값이 있는지 확인
  delay(100);
  Serial.println("connect TCP..");
  cmd = "AT+CIPSTART=\"TCP\",\"jukson.dothome.co.kr\",21";
  ESP_wifi.println(cmd);
  Serial.println(cmd);

  Serial.println("Send data...");
  String uid = char_input;
  cmd = "GET /select_key.php?uid=" + uid + " HTTP/1.0\r\n\r\n";
  ESP_wifi.print("AT+CIPSEND=");
  ESP_wifi.println(cmd.length());
  Serial.print("AT+CIPSEND=");
  Serial.println(cmd.length());
  if (ESP_wifi.find(">")) {
    Serial.print(">");
  } else {
    ESP_wifi.println("AT+CIPCLOSE");
    Serial.println("select_key.php 연결실패");
    delay(1000);
    return false;
  }

  delay(500);

  ESP_wifi.println(cmd);
  Serial.println(cmd);
  delay(100);
  ESP_wifi.println("AT_CIPCLOSE");
  delay(100);
  if (ESP_wifi.find("Yes")) return true;  // DB에 일치하는 키값이 있으면 true, 없으면 false 반환
  else return false;
}

void is_something(String char_input, String some) { // 물건이 들어있으면 DB에 TRUE, 없으면 FALSE 보냄
  delay(100);
  Serial.println("connect TCP..");
  cmd = "AT+CIPSTART=\"TCP\",\"jukson.dothome.co.kr\",21";
  ESP_wifi.println(cmd);
  Serial.println(cmd);

  Serial.println("Send data...");
  String uid = char_input;
  cmd = "GET /is_something.php?uid=" + uid + "&some=" + some + " HTTP/1.0\r\n" + "Host: jukson.dothome.co.kr/r/n" + "Connection: close\r\n\r\n";
  ESP_wifi.print("AT+CIPSEND=");
  ESP_wifi.println("3," + cmd.length());
  Serial.print("AT+CIPSEND=");
  Serial.println(cmd.length());
  if (ESP_wifi.find(">")) {
    Serial.print(">");
  } else {
    ESP_wifi.println("AT+CIPCLOSE");
    Serial.println("is_something.php 연결실패");
    delay(1000);
    return;
  }
  delay(500);

  ESP_wifi.println(cmd);
  Serial.println(cmd);
  delay(100);
  ESP_wifi.println("AT_CIPCLOSE");
  delay(100);
}


// DynamicJsonDocument doc(1024); // Adjust buffer size as needed

// void parseLockersJson(String json) { // get_lockers.php에서 use_uid 뽑기
//   // Parse the JSON string
//   deserializeJson(doc, json);

//   // Check for parsing errors
//   if (deserializeJson(doc, json) == JsonDeserializationError) {
//     Serial.println(F("Failed to parse JSON"));
//     return;
//   }

//   // Get the JSON array
//   JsonArray lockers = doc["data"]; // Assuming "data" is the key for the array

//   // Iterate through each object in the array
//   for (int i = 0; i < lockers.size(); i++) {
//     JsonObject locker = lockers[i];

//     // Extract the use_uid
//     String useUid = locker["use_uid"];

//     // Process the useUid data (e.g., print it)
//     Serial.println(useUid);
//   }
// }


void setup() {
  Serial.begin(9600);
  ESP_wifi.begin(9600);
  ESP_wifi.setTimeout(10000);
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);
  mfrc522.PCD_Init();
  MFRC522Debug::PCD_DumpVersionToSerial(mfrc522, Serial);
  Serial.println(F("SCAN PICC to see UID, SAK, Type and data block..."));
  delay(1000);

  int connected = 0;
  cmd = "AT+CWJAP=\"";
  cmd += SSID;
  cmd += "\",\"";
  cmd += PASSWORD;
  cmd += "\"";
  for (int i = 0; i < 10; i++) {  // 네트워크 연결시도 (10번
    ESP_wifi.println(cmd);
    delay(2000);
    if (ESP_wifi.find("OK")) {  // 연결에 성공하면
      Serial.println("인터넷 연결 완료");
      connected = 1;
      break;
    } else
      Serial.println("인터넷 연결중...");
  }
  if (!connected) {
    Serial.println("인터넷 연결 실패");
    while (1)
      ;
  }
}

void loop() {
  if (!mfrc522.PICC_IsNewCardPresent()) return;  // RFID 인식
  if (!mfrc522.PICC_ReadCardSerial()) return;
  String uidString = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    uidString += String(mfrc522.uid.uidByte[i], HEX);
  }
  Serial.print(F("Card UID: "));
  Serial.println(uidString);

  if (mode == 0) {  // 사물함 문 여는 코드
    // DB 조회 시 use_uid는 일치하는데 end_date가 지나있으면 DB에서 삭제(코드 + php구현필요)

    if (db_key(uidString)) {  // DB 확인 후 카드키 조회if (db_key(uidString))
      Serial.println("문이 열립니다");
      cardUID = uidString;
      rgb_set(0, 0, 150);
      for (int i{ 0 }; i < 255; i += 5) {
        analogWrite(SERVO_PIN, i);
        delay(50);
      }
      delay(1000);
      mode = 1;
    } else { // DB에 없으면 
      Serial.println("DB에 키없음");
      for (int i{ 0 }; i < 3; i++) {
        rgb_set(150, 0, 0);
        delay(500);
        rgb_set(0, 0, 0);
        delay(500);
        mfrc522.PICC_HaltA();
        delay(1000);
      }
    }
  }

  else if (mode == 1) {  //사물함 문 닫는 코드
    //문 닫음
    Serial.println("문이 닫힙니다");
    for (int i{ 255 }; i >= 0; i -= 5) {
      analogWrite(SERVO_PIN, i);
      delay(50UL);
    }
    delay(1000UL);
    rgb_set(0, 0, 0);
    //초음파센서로 물건여부 확인(약 40cm)
    int count = 0;
    for (int i = 0; i < 10; i++) {
      digitalWrite(TRIG_PIN, LOW);
      delayMicroseconds(2);
      digitalWrite(TRIG_PIN, HIGH);
      delayMicroseconds(10);
      digitalWrite(TRIG_PIN, LOW);

      long duration = pulseIn(ECHO_PIN, HIGH);
      long distance = duration * 0.034 / 2;

      Serial.print("Distance : ");
      Serial.print(distance);
      Serial.println(" cm");
      delay(100);
      if (distance <= LOCKER_DISTANCE) {
        Serial.println("Object is close!");
        count++;
      } else {
        Serial.println("Object is far away.");
      }
    }
    //DB 연결 후 물건 있는지 업데이트(업데이트용 php 구현 예정) - is_something.php
    String isSomething;
    if(count > 5) isSomething = "TRUE"; // 사물함에 물건이 있으면
    else isSomething = "FALSE";
    is_something(cardUID, isSomething);
    //종료
    mfrc522.PICC_HaltA();
    delay(1000);
  }
}
