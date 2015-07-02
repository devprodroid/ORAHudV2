package devprodroid.orahudserver;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.yadrone.base.IARDrone;


public class ConnectionActivity extends AppCompatActivity {


    private static final String TAG = ConnectionActivity.class.getName();
    private static final int REQUEST_ENABLE_BT = 1;

    private static final UUID serv_UUID = UUID.fromString("07419c1a-090c-11e5-a6c0-1697f925ec7b");
    List<BluetoothDevice> validDevices = new ArrayList<>();

    ListView devListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        //Add WIfi for Drone Connection

        initializeWifi();

        //initializeDrone();
    }

    private void initializeWifi() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final TextView text = (TextView) findViewById(R.id.text_init);
        text.append(wifi.getConnectionInfo().getSSID());


    }

    private void initializeDrone() {
        YADroneApplication app = (YADroneApplication) getApplication();
        final IARDrone drone = app.getARDrone();

        try {
            Log.e(TAG, "Initialize the drone ..");
            drone.start();


        } catch (Exception exc) {

            if (drone != null)
                drone.stop();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        devListView = (ListView) findViewById(R.id.bt_listview);


        //Create a broadcastreceiver that is trigerred asynchronously after a call to fetchUuidsWithSdp() call.
        BroadcastReceiver handler = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Check that we just received an UUID discover message
                if (intent.getAction().equals(BluetoothDevice.ACTION_UUID)) {

                    Bundle bdl = intent.getExtras();
                    BluetoothDevice btDevice = (BluetoothDevice) bdl
                            .getParcelable(BluetoothDevice.EXTRA_DEVICE);

                    Log.e(TAG, "ACTION_UUID event received. Searching through available UUID on "
                            + btDevice.getAddress());

                    ParcelUuid[] puList = btDevice.getUuids();

                    // Search our UUID server through the list of available UUID
                    // on the remote device
                    for (ParcelUuid pu : puList) {
                        if (pu.getUuid().compareTo(serv_UUID) == 0) {
                            Log.e(TAG, "Compatible server found.");

                            //Check if the device is not already present in the list.
                            if (validDevices.indexOf(btDevice) == -1)
                                validDevices.add(btDevice);

                            break;
                        }
                    }

                    //Refresh the GUI
                    if (validDevices.size() == 0) {
                        Log.e(TAG, "No compatible devices found.");
                        devListView.setClickable(false);
                        String[] noDeviceFound = {"Please pair your device with ORA Glasses before using ORA connect"};
                        devListView.setAdapter(new ArrayAdapter<String>(ConnectionActivity.this,
                                android.R.layout.simple_list_item_1, noDeviceFound));

                        devListView.setOnItemClickListener(null);
                    } else {
                        //Refresh the list of devices on the GUI.
                        BTListAdapter adapter = new BTListAdapter(ConnectionActivity.this, validDevices);
                        devListView.setAdapter(adapter);
                        devListView.setClickable(true);

                        AdapterView.OnItemClickListener clickHandler = new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

                                BluetoothDevice btDevice = validDevices.get(position);


                                Intent intent = new Intent(ConnectionActivity.this, ControlActivity.class);

                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                //Start the main activity

                                intent.putExtra(ControlActivity.MSG_BT_UUID, ConnectionActivity.serv_UUID.toString());
                                intent.putExtra(ControlActivity.MSG_MAC_BT_DEVICE_ADDRESS, btDevice.getAddress());

                                startActivity(intent);
                            }

                        };

                        //Register the event on click.
                        devListView.setOnItemClickListener(clickHandler);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_UUID);

        registerReceiver(handler, filter);

        //Refresh the listview of MAC addresses and names.
        RefreshDeviceList();
    }

    /**
     * Refresh the list of paired devices. Note that a device should be paired to be listed
     * in the listview on this GUI.
     */
    private void RefreshDeviceList() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Flush the list of valid devices
        validDevices.clear();

        if (mBluetoothAdapter == null)
            Log.e("BTDeviceDiscover", "No bluetooth adapter detected/available.");

        //Get the list of paired devices.
        Set<BluetoothDevice> devList = mBluetoothAdapter.getBondedDevices();

        //Check if bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            Log.e("BTDeviceDiscover", "Bluetooth is DISABLED. Asking the user");
            return;
        }

        for (BluetoothDevice btDevice : devList) {
            //Refresh the list of UUID on the remote device. !!!!ASYNCHRONE
            Log.e(TAG, "READING FROM " + btDevice.getAddress());
            btDevice.fetchUuidsWithSdp();
        }
    }

    /**
     * Callback for the refresh button.
     *
     * @param v
     */
    public void onBtnClicked(View v) {
        if (v.getId() == R.id.btn_refresh) {
            RefreshDeviceList();
        }
        if (v.getId() == R.id.btnDrone) {
                initializeDrone();
        }
    }

    private void startControlActivity() {
        //Start the main activity
        Intent intent = new Intent(this, ControlActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_connection,
                    container, false);
            return rootView;
        }
    }

    /**
     * Upon pressing the BACK-button, the user has to confirm the connection to the drone is taken down.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            new AlertDialog.Builder(this).setMessage("Upon exiting, drone will be disconnected !").setTitle("Exit YADrone ?").setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    YADroneApplication app = (YADroneApplication) getApplication();
                    IARDrone drone = app.getARDrone();
                    drone.stop();

                    finish();
                }
            }).setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // User selected Cancel, nothing to do here.
                }
            }).show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


}
