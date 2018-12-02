package com.apptech.androidactivemqdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    String serverURI = "tcp://192.168.1.120:1883";
    MqttAndroidClient client;
    String clientId = "android_app";
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.maintextView);
        textView.setText("Running...");
        Log.d("ACTIVEMQ", "Connecting...");
        Log.d("ACTIVEMQ", "Connecting...");
        Log.d("ACTIVEMQ", "Connecting...");
        Log.d("ACTIVEMQ", "Connecting...");
        connect();
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
        String subscribeTopic = "topicgeneral";
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
