package com.example.thor.cambot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity implements OnItemSelectedListener {
    Button b1,b2,b3,b4;
    private BluetoothAdapter BA;
    private BluetoothDevice device;
    private Set<BluetoothDevice>pairedDevices;
    private BluetoothSocket btSocket = null;
    private OutputStream tmpOut = null;
    public enum command {
        init, start, stop, clockwise, anticlockwise, angle30, angle60, angle90, angle120, angle150, angle180, forward, backward,
    }
    command angle;
    private Spinner spinner;
    private static final String[]paths = {"30", "60", "90", "120", "150", "180"};
    //public ConnectedThread mConnectedThread;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item,paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        b1 = (Button) findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        b4=(Button)findViewById(R.id.button4);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);
    }

    public void onNothingSelected(AdapterView<?> parent){
        //Nothing
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                angle = command.angle30;
                break;
            case 1:
                angle = command.angle60;
                break;
            case 2:
                angle = command.angle90;
                break;
            case 3:
                angle = command.angle120;
                break;
            case 4:
                angle = command.angle150;
                break;
            case 5:
                angle = command.angle180;
                break;
        }
    }

    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }


    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                //Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    public void list(View v){
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) list.add(bt.getName() + "\n" + bt.getAddress());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        //TextView v;
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TextView v();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                device = BA.getRemoteDevice(address);
                try {
                    btSocket = createBluetoothSocket(device);

                } catch (IOException e) {
                    //errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
                }
                BA.cancelDiscovery();
                // Establish the connection.  This will block until it connects.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "...Connecting...");
                        try {

                            btSocket.connect();
                            //Log.d(TAG, "....Connection ok...");
                            // Create a data stream so we can talk to server.
                            //Log.d(TAG, "...Create Socket...");

                            //mConnectedThread = new ConnectedThread(btSocket);
                            //mConnectedThread.start();
                        } catch (IOException e) {
                            //Log.d(TAG, "....Entering Catch...");
                            e.printStackTrace();
                            try {
                                //Log.d(TAG, "....Closing Socket...");
                                btSocket.close();
                            } catch (IOException e2) {
                                //Log.d(TAG, "....Fatal Error...");
                                //errorExit("Fatal Error", "close socket during connection failure" + e2.getMessage() + ".");
                            }
                        }
                    }
                });
                try{
                    tmpOut = btSocket.getOutputStream();
                }
                catch (IOException e) {
                    //Log.d(TAG, "..IOException : " + e.getMessage() + "...");
                }
                try {
                    tmpOut.write(0);
                }
                catch (IOException e) {
                    //Log.d(TAG, "..IOException : " + e.getMessage() + "...");
                }
                Toast.makeText(getApplicationContext(), "Item clicked " + address ,Toast.LENGTH_LONG).show();
                /*while(true)
                {
                    try {
                        tmpOut.write(5);
                    }
                    catch (IOException e) {
                        //Log.d(TAG, "..IOException : " + e.getMessage() + "...");
                    }
                }*/
            }
        });
    }
    public void start(View v){
        //BA.disable();
        try {
            tmpOut.write(command.start.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Start Data sent" ,Toast.LENGTH_LONG).show();
    }
    public void stop(View v){
        //BA.disable();
        try {
            tmpOut.write(command.stop.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Stop Data sent" ,Toast.LENGTH_LONG).show();
    }
    public void anticlockwise(View v){
        //BA.disable();
        try {
            tmpOut.write(command.anticlockwise.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Anticlockwise Command sent" ,Toast.LENGTH_LONG).show();
    }
    public void clockwise(View v){
        //BA.disable();
        try {
            tmpOut.write(command.clockwise.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Clockwise Command sent" ,Toast.LENGTH_LONG).show();
    }
    public void setangle(View v){
        //BA.disable();
        try {
            tmpOut.write(angle.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Angle Data sent" ,Toast.LENGTH_LONG).show();
    }
    public void forward(View v){
        //BA.disable();
        try {
            tmpOut.write(command.forward.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Forward Command sent" ,Toast.LENGTH_LONG).show();
    }
    public void backward(View v){
        //BA.disable();
        try {
            tmpOut.write(command.backward.ordinal());
        }
        catch(IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to Send", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(), "Backward Command sent" ,Toast.LENGTH_LONG).show();
    }

}
