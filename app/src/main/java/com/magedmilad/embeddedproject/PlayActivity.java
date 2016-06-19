package com.magedmilad.embeddedproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.zerokol.views.JoystickView;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class PlayActivity extends AppCompatActivity {

    private JoystickView joystick;
    private TextView angleTextView,powerTextView,PostionTextView;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

//    private static final UUID MY_UUID = UUID.fromString("ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public String newAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        angleTextView = (TextView) findViewById(R.id.angle_text_view);
        powerTextView = (TextView) findViewById(R.id.power_text_view);
        PostionTextView = (TextView) findViewById(R.id.postion_text_view);

        joystick = (JoystickView) findViewById(R.id.joystick_view);
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                angle -= 90;
                angle *= -1;
                if (angle < 0)
                    angle += 360;
                angleTextView.setText("angle: " + String.valueOf(angle) + "°");
                powerTextView.setText("power: " + String.valueOf(power) + "%");
                int x = (int) (power * Math.cos(Math.toRadians(angle)));
                int y = (int) (power * Math.sin(Math.toRadians(angle)));
                PostionTextView.setText("postion: (" + x + "," + y + ")");
                sendData(x + " " + y+"\n");
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            btSocket.close();
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Can't close the connection", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "ERROR - Could not create Insecure RFComm Connection", Toast.LENGTH_SHORT).show();
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }



    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        newAddress = intent.getStringExtra(Intent.EXTRA_UID);


        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);


        try {
//            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }


        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
//                btSocket = createBluetoothSocket(device);
//                btSocket.connect();
//                Toast.makeText(getBaseContext(), "ERROR - Connection Failed", Toast.LENGTH_SHORT).show();
//                Toast.makeText(getBaseContext(), "ERROR - "+e.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), "ERROR - "+e.getMessage(), Toast.LENGTH_SHORT).show();

            } catch (IOException e2) {
//                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), "ERROR - "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }


        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try     {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkBTState() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
