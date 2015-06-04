package devprodroid.orahudserver;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.AttitudeListener;
import devprodroid.bluetooth.BTClient;
import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTSocketListener;


/**
 * This Activity displays Control interface and informations for the droneii
 */
public class ControlActivity extends AppCompatActivity implements  BTSocketListener.Callback, AttitudeListener {


    private final String TAG = getClass().getPackage().getName();
    public final static String MSG_BT_UUID = "MSG_BT_UUID";
    public final static String MSG_MAC_BT_DEVICE_ADDRESS = "MSG_MAC_BT_DEVICE_ADDRESS";

    private boolean connected = false;

    private BluetoothDevice device;

    private BTClient client;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);


        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        //Param reading
        Intent act_param = this.getIntent();
        final String macBTDeviceAddress = act_param.getStringExtra(MSG_MAC_BT_DEVICE_ADDRESS);
        final UUID serv_UUID = UUID.fromString(act_param.getStringExtra(MSG_BT_UUID));
        //retrieve the bt device
        for (BluetoothDevice d : ba.getBondedDevices()) {
            if (d.getAddress().equals(macBTDeviceAddress)) {
                device = d;
                break;
            }
        }
        if (device == null) {
            throw new IllegalArgumentException();
        }

        //Progress dialog
        mProgressDialog = ProgressDialog.show(this, "Please wait",
                "Trying to connect to \"" + device.getName() + "\"", true);

        //Connection thread
        new Thread("Client creation") {
            public void run() {
                try {
                    client = new BTClient(ControlActivity.this, ControlActivity.this, device, serv_UUID);

                    //connection successfull
                    connected = true;
                    mProgressDialog.dismiss();
                    showMsg("Connection successful!");

                } catch (IOException e) {
                    showMsg(e.getMessage());
                    Log.e(TAG, e.getMessage(), e);
                    finish();
                }
            };
        }.start();

    }
















    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
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



    public void onResume() {
        super.onResume();

        YADroneApplication app = (YADroneApplication) getApplication();
        IARDrone drone = app.getARDrone();
        drone.getNavDataManager().addAttitudeListener(this);
    }

    public void onPause() {
        super.onPause();

        YADroneApplication app = (YADroneApplication) getApplication();
        IARDrone drone = app.getARDrone();
        drone.getNavDataManager().removeAttitudeListener(this);
    }

    public void attitudeUpdated(final float pitch, final float roll, final float yaw) {
        final TextView text = (TextView) findViewById(R.id.text_navdata);

        runOnUiThread(new Runnable() {
            public void run() {
                text.setText("Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw);


//                if (connected) {
//                    try {
//
//                        //client.sendMessage("huhu");
//                } catch (IOException E) {
//                        E.printStackTrace();
//
//                }
//                }

            }
        });
    }

    public void attitudeUpdated(float arg0, float arg1) {
    }

    public void windCompensation(float arg0, float arg1) {
    }


    /**
     * Callback for the refresh button.
     *
     * @param v
     */
    public void onBtnClicked(View v) {
        YADroneApplication app = (YADroneApplication)getApplication();
        final IARDrone drone = app.getARDrone();
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 10);

    }


    @Override
    public BTMessage onMsgReceived(BluetoothDevice bluetoothDevice, BTMessage btMessage) {
        return null;
    }

    @Override
    public boolean isAuthorized(BluetoothDevice bluetoothDevice) {
        return true;
    }

    @Override
    public void onDisconnected(BluetoothDevice bluetoothDevice) {
        showMsg("Disconnected from server.");
        finish();
    }
    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ControlActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy()
    {


        if (client != null) {
            client.close();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        super.onDestroy();
    }
}
