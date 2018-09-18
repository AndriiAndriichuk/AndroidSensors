package com.example.andrii.android_sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    TextView edit_x,edit_y,edit_z,edit_light,edit_air_hum,edit_air_temp,edit_pressure,edit_steps,edit_battery;
    SensorManager sensorManager_1, sensorManager_2;
    Sensor sensor_1,sensor_2;

    SensorManager sensorManager;
    Sensor sensorAccel,sensorMagnet,sensorStepC,sensorPressure,sensorTemperature;

    boolean isLight = false,isHumidity = false;
    float[] rotationVector = new float[9];
    Timer timer;
    float[] valuesAccel = new float[3];
    float[] valuesMagnet = new float[3];
    float[] valuesResult = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(MainActivity.this.mBatInfoReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        sensorManager_1 = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager_2 = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorLight = sensorManager_1.getSensorList(Sensor.TYPE_LIGHT);
        List<Sensor> sensorHumidity = sensorManager_2.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorStepC = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        edit_x = (TextView) findViewById(R.id.edit_x);
        edit_y = (TextView) findViewById(R.id.edit_y);
        edit_z = (TextView) findViewById(R.id.edit_z);
        edit_light = (TextView) findViewById(R.id.edit_light);
        edit_air_hum = (TextView) findViewById(R.id.edit_air_hum);
        edit_air_temp = (TextView) findViewById(R.id.edit_air_temp);
        edit_pressure = (TextView) findViewById(R.id.edit_pressure);
        edit_steps = (TextView) findViewById(R.id.edit_steps);
        edit_battery = (TextView) findViewById(R.id.edit_battery);

        if(sensorLight.size()>0){
            isLight = true;
            isHumidity = false;
            sensor_1 = sensorLight.get(0);
        }
        if(sensorHumidity.size()>0){
            isHumidity = true;
            isLight = false;
            sensor_2 = sensorHumidity.get(0);
        }
    }


    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            int voltage = intent.getIntExtra("voltage",0);
            int temperature = intent.getIntExtra("temperature",0);
            edit_battery.setText(level + "%  \nTeмпература : " + temperature/10 + "°C\nНапруга : " + voltage + " mV");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager_1.registerListener(sen_1, sensor_1, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager_2.registerListener(sen_2,sensor_2,SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorStepC, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorTemperature, SensorManager.SENSOR_DELAY_NORMAL);



        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDeviceOrientation();
                        showInfo();
                    }
                });
            }
        };
        timer.schedule(task, 0, 100);
    }

    void showInfo() {
        double x = valuesResult[0];
        double y = valuesResult[1];
        double z = valuesResult[2];


        float[] rMat = new float[9];
        float[] iMat = new float[9];
        float[] orientation = new float[3];

        double azimuth = 0;
        double pitch = Math.atan(x/Math.sqrt(Math.pow(y,2) + Math.pow(z,2)));
        double roll = Math.atan(y/Math.sqrt(Math.pow(x,2) + Math.pow(z,2)));
        pitch = precise(pitch * (180.0/3.14) , 1);
        roll = precise(roll * (180.0/3.14), 1);

        if ( SensorManager.getRotationMatrix( rMat, iMat, valuesAccel, valuesMagnet ) )
            azimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
        edit_x.setText(azimuth + "");
        edit_y.setText(pitch + "");
        edit_z.setText(roll + "");
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(isLight)
            sensorManager_1.unregisterListener(sen_1);

        if (isHumidity)
            sensorManager_2.unregisterListener(sen_2);

        sensorManager.unregisterListener(listener);
        timer.cancel();



    }
    SensorEventListener sen_1 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                edit_light.setText(" " + String.valueOf(precise(x,10) + " lux"));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    SensorEventListener sen_2 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
                float hum = event.values[0];
                edit_air_hum.setText(" " + precise(hum,10) + " %");
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };


    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i=0; i < 3; i++)
                        valuesAccel[i] = event.values[i];
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for (int i=0; i < 3; i++)
                        valuesMagnet[i] = event.values[i];
                    break;
                case Sensor.TYPE_PRESSURE:
                    double temp = event.values[0];
                    temp *= 0.7500637554192;
                    int res = (int) precise(temp,1);
                    edit_pressure.setText(" " + res + " мм.рт.ст");
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    edit_steps.setText(/*current_steps*/event.values[0] + " кроків");
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    temp = event.values[0];
                    edit_air_temp.setText(" " + precise(temp,10) + " °C");
                    break;
            }
        }
    };

    void getDeviceOrientation() {
        SensorManager.getRotationMatrix(rotationVector, null, valuesAccel, valuesMagnet);
        SensorManager.getOrientation(rotationVector, valuesResult);

        valuesResult[0] = (float) Math.toDegrees(valuesResult[0]);
        valuesResult[1] = (float) Math.toDegrees(valuesResult[1]);
        valuesResult[2] = (float) Math.toDegrees(valuesResult[2]);
        return;
    }

    public static double precise(double value, int precise){
        value *= precise;
        int i = (int)Math.round(value);
        value = (double)i/precise;
        return value;

    }


}
