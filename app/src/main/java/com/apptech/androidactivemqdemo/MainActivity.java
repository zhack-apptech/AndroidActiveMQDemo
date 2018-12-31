package com.apptech.androidactivemqdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String serverURI = "tcp://www.apptechgateway.io:1883";
    MqttAndroidClient client;
    String clientId = "android_app";
    String coordinates;
    LocationManager locationManager;
    Location currentLocation;
    Location lastKnownLocation;
    Location lastKnownLocation2;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateNewLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    TextView textView;
    Button getDetailsButton;
    Button sendDetailsButton;
    EditText timestampEditText;
    EditText coordinatesEditText;
    EditText mobileIdEditText;

    String details;

    OnClickListener getDetailsListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            getDetails();
        }
    };

    OnClickListener sendDetailsListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            sendDetails();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.maintextView);
        textView.setText("Running...");
        getDetailsButton = findViewById(R.id.getInfoButton);
        getDetailsButton.setOnClickListener(getDetailsListener);
        sendDetailsButton = findViewById(R.id.sendInfoButton);
        sendDetailsButton.setOnClickListener(sendDetailsListener);

        timestampEditText = findViewById(R.id.timestampText);
        coordinatesEditText = findViewById(R.id.coordinatesText);
        mobileIdEditText = findViewById(R.id.mobileIdText);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);



        Log.d("ACTIVEMQ", "Connecting...");
        Log.d("ACTIVEMQ", "Connecting...");
        Log.d("ACTIVEMQ", "Connecting...");
        Log.d("ACTIVEMQ", "Connecting...");
        connect();
    }
    // if location has changed
    private void updateNewLocation(Location location) {
        currentLocation = location;
    }
    // send details
    private void sendDetails(){
        String topic = "topic/analytics";
        String payload = "{'clientId'}";
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);

            client.publish(topic, message);

        } catch ( UnsupportedEncodingException | MqttException ex) {
            ex.printStackTrace();
        }

    }
    // get details method
    private void getDetails(){
        Log.d("DETAILS", "Getting phone timestamp...");
        Date date = new Date();
        timestampEditText.setText(date.toString());

        Log.d("DETAILS", "Getting phone coordinates...");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d("LOCATION:", "Requesting location updates ...");
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        lastKnownLocation2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Log.d("LOCATION", "Displaying last location ...");

        if(lastKnownLocation2 != null)
        {
            String coordinates = lastKnownLocation2.getLatitude() + ", " + lastKnownLocation2.getLongitude();
            coordinatesEditText.setText(coordinates);
        } else {
            if(lastKnownLocation != null) {
//                String coordinates = lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude();
                coordinates = currentLocation.getLatitude() + ", " + currentLocation.getLongitude();
                coordinatesEditText.setText(coordinates);
            }
        }

        // get device id
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getSubscriberId();
        mobileIdEditText.setText(Settings.Secure.ANDROID_ID + " Subscriber ID: " + deviceId);


    }

    // connect method
    private void connect() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);

        client = new MqttAndroidClient(this, serverURI, clientId);
        Log.d("ACTIVEMQ", "Client ready to connect...");
        Toast.makeText(MainActivity.this, "Client ready to connect...", Toast.LENGTH_SHORT).show();
        try {
            client.connect(connectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("ACTIVEMQ", "Client subscribing...");
                    Toast.makeText(MainActivity.this, "Client subscribing...", Toast.LENGTH_SHORT).show();
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                    Log.d("ACTIVEMQ", "Failed subscribing...");
                    Toast.makeText(MainActivity.this, "Failed subscribing...", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d("ACTIVEMQ", "Failed to connect...");
        }
    }

    // subscribe method
    private void subscribe() {
        String subscribeTopic = "topic/analytics";
        try {
            client.subscribe(subscribeTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ACTIVEMQ", "Message Arrived...");
                            Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_SHORT).show();
                            textView.setText(message.toString());
                        }
                    });
                }
            });
        } catch (MqttException e) {
            Log.d("ACTIVEMQ", "Failed subscribing...");
            Toast.makeText(MainActivity.this, "Error..." + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



}
