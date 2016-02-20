package com.monklu.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.monklu.bluetooth.R;

public class BluetoothActivity extends DrawerActivity implements AdapterView.OnItemClickListener {

    public static BluetoothAdapter bluetoothAdapter;
    public static Set<BluetoothDevice> pairedDevices;

    public static ArrayAdapter<String> btArrayAdapter;

    //DO THIS ONE LATER WITH A CUSTOM ARRAY ADAPTER
    public static ArrayAdapter<BluetoothDeviceItem> BTItemAdapter;

    //public ArrayList<BluetoothDeviceItem> bluetoothItemList;
    //public ArrayList<BluetoothDevice> bluetoothItems;

    private ListView listDevices;
    private boolean visible_or_paired = false;

    //some constants
    final private int ACTIVITY_BLUETOOTH_ENABLE_CODE = 1;
    final private int ACTIVITY_BLUETOOTH_VISIBLE_CODE = 2;
    final private int VISIBILITY_TIME = 30;
    public static String DEVICE_ADDRESS = "com.monklu.bluetooth.deviceAddress";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        //getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setTitle("Smartie!");
        checkAndEnableBluetooth();

        listDevices = (ListView) findViewById(R.id.listDevices);
        listDevices.setOnItemClickListener(this);

        //BTItemAdapter = new ArrayAdapter<BluetoothDeviceItem>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        //listDevices.setAdapter(BTItemAdapter);

        btArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        listDevices.setAdapter(btArrayAdapter);

        turnBluetoothPaired();
//        Button button = (Button) findViewById(R.id.connect_button);
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//				if(mDrawerLayout.isDrawerOpen(mDrawerLayout)) {
//					mDrawerLayout.closeDrawer(mDrawerLayout);
//				}
//				else {
//					mDrawerLayout.openDrawer(mDrawerLayout);
//				}
//
//                //mDrawerLayout.openDrawer(Gravity.LEFT);
//                Log.e("does", "Work");
//            }
//        });
    }
    private void checkAndEnableBluetooth() {
        //check if the phone has a bluetooth
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Device could not enable Bluetooth device (might not support)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Turn on Bluetooth on app
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBluetoothOn, ACTIVITY_BLUETOOTH_ENABLE_CODE);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer, null);
        getLayoutInflater().inflate(layoutResID, mFrameLayout, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Enable Bluetooth if it's not enabled
            case R.id.action_enable:
                turnBluetoothOn();
                break;
            //Disable Bluetooth
            case R.id.action_disable:
                turnBluetoothOff();
                break;
            //Enable Bluetooth Visibility for some seconds
            case R.id.action_visible:
                turnBluetoothVisible();
                break;
            case R.id.action_paired:
		/*	FragmentManager manager = getFragmentManager();
			PairedDevicesDialog myDialog = new PairedDevicesDialog();
			myDialog.show(manager, "Paired Devices");
		  */
                turnBluetoothPaired();
                break;
            //Go into Bluetooth settings to pair with the device
            case R.id.action_configure:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e("Bluetooth Visible", "" + resultCode);
        switch(requestCode){
            case ACTIVITY_BLUETOOTH_ENABLE_CODE:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "You will need Bluetooth to use this application!", Toast.LENGTH_SHORT).show();
                        //finish();
                }
                break;
            case ACTIVITY_BLUETOOTH_VISIBLE_CODE:
                if (resultCode == VISIBILITY_TIME){
                    Toast.makeText(this, "Bluetooth visible!", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth invisible!", Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        //stop discovering

        if (parent.getId() == R.id.listDevices) {
            listDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listDevices.setItemChecked(position, true);         //set 'checked'

            String info = parent.getAdapter().getItem(position).toString();
            String address = info.substring(info.length() - 17);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            Log.e("Item selected", address);

            if (visible_or_paired){//if looking for stuff
                //if pressed on visible device, pair with that device
                bluetoothAdapter.cancelDiscovery();
                pairDevice(device);

            } else {
                //if pressed on paired device, connect to that device

                Intent intent = new Intent(BluetoothActivity.this, SendActivity.class);
                intent.putExtra(DEVICE_ADDRESS, address);
                startActivity(intent);

            }
        }
    }

    public void turnBluetoothOff (){
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "Turned OFF", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(getApplicationContext(), "Already OFF", Toast.LENGTH_LONG).show();
    }
    public void turnBluetoothOn (){
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, ACTIVITY_BLUETOOTH_ENABLE_CODE);
        } else Toast.makeText(this, "Already ON", Toast.LENGTH_SHORT).show();
    }
    public void turnBluetoothPaired (){
        pairedDevices = bluetoothAdapter.getBondedDevices();
        visible_or_paired = false;  //later in onItemClick finds this

        if (btArrayAdapter != null)
            btArrayAdapter.clear();

        if (pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices) {
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    public void turnBluetoothVisible(){
        visible_or_paired = true;
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        if (btArrayAdapter != null)
            btArrayAdapter.clear();

        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        bluetoothAdapter.startDiscovery();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("Status", "Pairing invoked");
        } catch (Exception e) {
            Log.d("Status", "Unable to pair.\n" + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mReceiver);
        bluetoothAdapter.cancelDiscovery();
        //bluetoothAdapter.disable();
    }
}


