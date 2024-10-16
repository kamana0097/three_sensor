package com.example.accelprint;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, magnetometer;
    private FileWriter fileWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize sensor manager and sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Set up a custom sample rate (50 Hz = 20000 microseconds)
        int customSampleRateInMicroseconds = 20000;  // 50 Hz

        // Register the sensor listeners with the custom sample rate
        sensorManager.registerListener(this, accelerometer, customSampleRateInMicroseconds);
        sensorManager.registerListener(this, gyroscope, customSampleRateInMicroseconds);
        sensorManager.registerListener(this, magnetometer, customSampleRateInMicroseconds);

        // Set up file writer to store data in app-specific external storage
        try {
            File file = new File(getFilesDir(), "sensor_data.csv");
            fileWriter = new FileWriter(file, true);
            // Write header to file if the file is new
            if (file.length() == 0) {
                fileWriter.append("Timestamp,Accelerometer_X,Accelerometer_Y,Accelerometer_Z,Gyroscope_X,Gyroscope_Y,Gyroscope_Z,Magnetometer_X,Magnetometer_Y,Magnetometer_Z\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();
        String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(timestamp));

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                logSensorData("Accelerometer", event.values);
                writeToCsv(timeString, event.values, null, null); // Log accelerometer data
                break;
            case Sensor.TYPE_GYROSCOPE:
                logSensorData("Gyroscope", event.values);
                writeToCsv(timeString, null, event.values, null); // Log gyroscope data
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                logSensorData("Magnetometer", event.values);
                writeToCsv(timeString, null, null, event.values); // Log magnetometer data
                break;
        }
    }

    // Helper function to log sensor data to the console
    private void logSensorData(String sensorType, float[] values) {
        Log.d(sensorType, "X: " + values[0] + ", Y: " + values[1] + ", Z: " + values[2]);
    }

    // Helper function to write sensor data to CSV
    private void writeToCsv(String timestamp, float[] accelerometerData, float[] gyroscopeData, float[] magnetometerData) {
        try {
            StringBuilder builder = new StringBuilder(timestamp + ",");
            // Append accelerometer data or empty fields
            if (accelerometerData != null) {
                builder.append(accelerometerData[0]).append(",").append(accelerometerData[1]).append(",").append(accelerometerData[2]).append(",");
            } else {
                builder.append(",,");
            }

            // Append gyroscope data or empty fields
            if (gyroscopeData != null) {
                builder.append(gyroscopeData[0]).append(",").append(gyroscopeData[1]).append(",").append(gyroscopeData[2]).append(",");
            } else {
                builder.append(",,");
            }

            // Append magnetometer data or empty fields
            if (magnetometerData != null) {
                builder.append(magnetometerData[0]).append(",").append(magnetometerData[1]).append(",").append(magnetometerData[2]);
            } else {
                builder.append(",,");
            }

            builder.append("\n");
            fileWriter.append(builder.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
