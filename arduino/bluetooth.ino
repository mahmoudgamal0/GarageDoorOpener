#include <SoftwareSerial.h>
#include <Servo.h>

#include <Wire.h>
#include <LCD.h>
#include <LiquidCrystal_I2C.h>

LiquidCrystal_I2C  lcd(0x27, 2, 1, 0, 4, 5, 6, 7);


SoftwareSerial BTserial(8, 9); // RX | TX
Servo servo;

const long baudRate = 9600;
const char OPEN = '1';
const char CLOSE = '2';
const char OERR = '3';
const char CERR = '4';
const int OPEN_ANGLE = 0;
const int CLOSE_ANGLE = 90;
const int SERVO_DELAY = 10;
int curr_servo_angle;
char c = ' ';
boolean NL = true;
int servoPin = 11;

void setup()
{
  Serial.begin(9600);
  Serial.print("Sketch:   ");   Serial.println(__FILE__);
  Serial.print("Uploaded: ");   Serial.println(__DATE__);
  Serial.println(" ");

  BTserial.begin(baudRate);
  Serial.print("BTserial started at "); Serial.println(baudRate);
  Serial.println(" ");

  servo.attach(servoPin);
  servo.write(0);
  curr_servo_angle = OPEN_ANGLE;
  Serial.print("Servo attached at pin"); Serial.print(servoPin);
  Serial.println(" ");


  lcd.setBacklightPin(3, POSITIVE);
  lcd.setBacklight(HIGH); // NOTE: You can turn the backlight off by setting it to LOW instead of HIGH
  lcd.begin(16, 2);
  lcd.clear();
  Serial.print("LCD started");
  Serial.println(" ");

}

void loop()
{

  // Read from the Bluetooth module and send to the Arduino Serial Monitor
  if (BTserial.available())
  {
    c = BTserial.read();
    if (c == OPEN) {
      open();
    }
    else if (c == CLOSE) {
      close();
    }

  }

}


void open() {
  if(is_open()){
    error("G already open", OERR);
    return;
  }
  Serial.println("Opening Garage..\n");
  write_lcd("Opening Garage..");
  write_servo(OPEN_ANGLE, 1);
  write_lcd("Garage is open");
  Serial.println("Garage is open\n");
  BTserial.write(OPEN);
}


void close() {
  if(!is_open()){
    error("G already closed", CERR);
    return;
  }
  Serial.println("Closing Garage..\n");
  write_lcd("Closing Garage..");
  write_servo(CLOSE_ANGLE, 1);
  write_lcd("Garage is closed");
  Serial.println("Garage is close\n");
  BTserial.write(CLOSE);
}

void write_servo(int destination_angle, int sstep) {
  int stp = destination_angle > curr_servo_angle ? 1 : -1;
  stp *= sstep;
  for (int i = curr_servo_angle; i != destination_angle; i = i + stp) {
    servo.write(i);
    delay(SERVO_DELAY);
  }
  curr_servo_angle = destination_angle;
}

void write_lcd(String msg) {
  lcd.clear();
  lcd.setCursor(0, 1);
  lcd.print(msg);
}

bool is_open(){
  return curr_servo_angle == OPEN_ANGLE;
}
void error(String message, const char ERR) {
  Serial.print("ERROR:"); Serial.print(message);
  Serial.println(" ");
  BTserial.write(ERR);
  write_lcd(message);
}
