package com.example.garagedooropener;

 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	   
	  Button btnOpenLeft, btnOpenRight, btnConnect, btnDisconnect;
	  TextView status;
	   
	  private static final int REQUEST_ENABLE_BT = 1;
	  private BluetoothAdapter btAdapter = null;
	  private BluetoothSocket btSocket = null;
	  private OutputStream outStream = null;
	  private InputStream inputStream = null;

	  // Well known SPP UUID
	  private static final UUID MY_UUID =
	      UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	 
	  // Insert your server's MAC address
	  String address = "00:14:03:11:37:22";
	  String deviceName = "HC-05";


	/** Called when the activity is first created. */

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	    btnOpenLeft = (Button) findViewById(R.id.btnOpen);
	    btnOpenRight = (Button) findViewById(R.id.btnClose);
	    btnConnect = (Button) findViewById(R.id.btnConnect);
	    btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
	    status = (TextView) findViewById(R.id.txtStatus);
	     
	    btAdapter = BluetoothAdapter.getDefaultAdapter();
	    checkBTState();
	    	 
	    btnOpenLeft.setOnClickListener(new OnClickListener() {
	        @SuppressLint("NewApi")
			public void onClick(View v) {
	      	  
	      	if (btSocket.isConnected()){
	      		sendData("1");
	      	}
	      	else{
	      		
	      	    status.setText("Please connect to Arduino");
	      	}
	        }
	      });
	    btnOpenRight.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	      	  if (btSocket.isConnected()){
	        		sendData("2");
	        	}
	        	else{
	        		
	        		status.setText("Please connect to Arduino");
	        	}
	        }
	      });
	    btnConnect.setOnClickListener(new OnClickListener(){
	      	public void onClick(View v){
	      		
	      		connect();
	      		
	      	}
	      });
	    btnDisconnect.setOnClickListener(new OnClickListener(){
	    	  public void onClick(View v){
	      		
	      		try {
	  				btSocket.close();
	  				status.setText("...Disconnected...");
	  			} catch (IOException e) {
	  				// TODO Auto-generated catch block
	  				e.printStackTrace();
	  			}
	    	  }
	      });

	}
	public void connect() {
		
		//Timer timer;
		//timer = new Timer();
		
		checkBTState();

	    status.setText("...Attempting to connect to Arduino...");
	    // Set up a pointer to the remote node using it's address.

		address = "00:00:00:00:00:00";

		Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
		if (devices != null) {
			for (BluetoothDevice device : devices) {
				if (deviceName.equals(device.getName())) {
					address = device.getAddress();
				}
			}
		}

		BluetoothDevice device = btAdapter.getRemoteDevice(address);

	    // Two things are needed to make a connection:
	    //   A MAC address, which we got above.
	    //   A Service ID or UUID.  In this case we are using the
	    //     UUID for SPP.
	    try {
	      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
	    } catch (IOException e) {
	      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
	    }
	   
	    // Discovery is resource intensive.  Make sure it isn't going on
	    // when you attempt to connect and pass your message.
	    btAdapter.cancelDiscovery();
	   
	    // Establish the connection.  This will block until it connects.
	    try {
	      btSocket.connect();
	      status.setText("...Connected established..."); 	      
	    } catch (IOException e) {
	      try {
	        btSocket.close();
	      } catch (IOException e2) {
	        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
	      }
	    }
	     
	    // Create a data stream so we can talk to server.
	    try {
	      outStream = btSocket.getOutputStream();
	      inputStream = btSocket.getInputStream();
	    } catch (IOException e) {
	      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
	    }
	  }
	   
	  private void checkBTState() {
	    // Check for Bluetooth support and then check to make sure it is turned on
	 
	    // Emulator doesn't support Bluetooth and will return null
	    if(btAdapter==null) { 
	      errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
	    } else {
	      if (btAdapter.isEnabled()) {
	        
	      } else {
	    	  
	    	  //Prompt user to turn on Bluetooth
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			
	      }
	    }
	  }
	 
	  private void errorExit(String title, String message){
	    Toast msg = Toast.makeText(getBaseContext(),
	        title + " - " + message, Toast.LENGTH_SHORT);
	    msg.show();
	    finish();
	  }
	 
	  private void sendData(String message) {
	    byte[] msgBuffer = message.getBytes();
	    try {
	      outStream.write(msgBuffer);

            boolean end = false;
            String dataString = "";
            int length = msgBuffer.length;
            while(!end)
            {
                int bytesRead = inputStream.read(msgBuffer);
                dataString += new String(msgBuffer, 0, bytesRead);
                if (dataString.length() == length)
                {
                    end = true;
                }
            }

            Toast.makeText(this, dataString, Toast.LENGTH_LONG).show();


	    } catch (IOException e) {
	      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
	      if (address.equals("00:00:00:00:00:00")) 
	        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
	      msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
	       
	      errorExit("Fatal Error", msg);       
	    }
	  }      

}
