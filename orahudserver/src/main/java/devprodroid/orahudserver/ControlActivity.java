package devprodroid.orahudserver;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;
import devprodroid.bluetooth.BTClient;
import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTSocketListener;
import devprodroid.bluetooth.DataModel;


/**
 * This Activity displays Control interface and informations for the droneii
 */
public class ControlActivity extends AppCompatActivity implements BTSocketListener.Callback, AttitudeListener,AltitudeListener, BatteryListener {


    public final static String MSG_BT_UUID = "MSG_BT_UUID";
    public final static String MSG_MAC_BT_DEVICE_ADDRESS = "MSG_MAC_BT_DEVICE_ADDRESS";
    private final String TAG = getClass().getPackage().getName();
    private boolean connected = false;

    private BluetoothDevice device;

    private BTClient client;
    private Boolean btSending = false;
    private ProgressDialog mProgressDialog;

    private TextView tvText;

    private DataModel dataModel;
    private final static int INTERVAL = 500;
    Handler mHandler;
    public static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Handler handler = new Handler();

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        //Param reading
        Intent act_param = this.getIntent();
        final String macBTDeviceAddress = act_param.getStringExtra(MSG_MAC_BT_DEVICE_ADDRESS);
        final UUID serv_UUID = UUID.fromString(act_param.getStringExtra(MSG_BT_UUID));


        tvText = (TextView) findViewById(R.id.text_navdata);

        initButtons();
        dataModel = new DataModel();


        if (true) {
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

                        //connection successful
                        connected = true;
                        mProgressDialog.dismiss();
                        showMsg("Connection successful!");

                    } catch (IOException e) {
                        showMsg(e.getMessage());
                        Log.e(TAG, e.getMessage(), e);
                        finish();
                    }
                }

                ;
            }.start();
        }

    }

    private void initButtons() {
        YADroneApplication app = (YADroneApplication) getApplication();
        final IARDrone drone = app.getARDrone();

        Button forward = (Button) findViewById(R.id.cmd_forward);
        forward.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().forward(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        Button backward = (Button) findViewById(R.id.cmd_backward);
        backward.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().backward(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button left = (Button) findViewById(R.id.cmd_left);
        left.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().goLeft(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button right = (Button) findViewById(R.id.cmd_right);
        right.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().goRight(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        Button up = (Button) findViewById(R.id.cmd_up);
        up.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().up(40);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        Button down = (Button) findViewById(R.id.cmd_down);
        down.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().down(40);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button spinLeft = (Button) findViewById(R.id.cmd_spin_left);
        spinLeft.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().spinLeft(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button spinRight = (Button) findViewById(R.id.cmd_spin_right);
        spinRight.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().spinRight(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        final Button landing = (Button) findViewById(R.id.cmd_landing);
        landing.setOnClickListener(new OnClickListener() {
            boolean isFlying = false;

            public void onClick(View v) {
                if (!isFlying) {
                    drone.takeOff();
                    landing.setText("Landing");
                } else {
                    drone.landing();
                    landing.setText("Take Off");
                }
                isFlying = !isFlying;
            }
        });

        Button emergency = (Button) findViewById(R.id.cmd_emergency);
        emergency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drone.reset();
            }
        });
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
        final TextView text = (TextView)findViewById(R.id.text_navdata);

        dataModel.setPitch(Math.round(pitch / 1000));
        dataModel.setRoll(Math.round(roll / 1000));
        dataModel.setYaw(Math.round(yaw / 1000));
        new SendtoBT().execute( );
    }

    public void attitudeUpdated(float arg0, float arg1) {
    }

    public void windCompensation(float pitch, float roll) {
        dataModel.setPitchCompensation(Math.round(pitch / 1000));
        dataModel.setRollCompensation(Math.round(roll / 1000));
    }




    /**
     * Callback for the refresh button.
     *
     * @param v
     */
    public void onBtnClicked(View v) {

        new SendtoBT().execute(3500000f,3500000f,-53500f);

    }

    private void sendMessage() {
        if (connected) {
            try {
                BTMessage msg = new BTMessage();

                final EditText input = (EditText) findViewById(R.id.inputMessage);
                String s = String.valueOf(input.getText());
                byte[] b = s.getBytes("UTF-8");
                msg.setPayload(b);

                client.sendMessage(msg);
            } catch (IOException E) {
                E.printStackTrace();

            }
        }
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
    public void onDestroy() {
        if (client != null) {
            client.close();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void receivedAltitude(int altitude) {
        dataModel.setAltitude(Math.round(altitude));
    }

    @Override
    public void receivedExtendedAltitude(Altitude altitude) {

    }

    @Override
    public void batteryLevelChanged(int batteryLevel) {
        dataModel.setBatteryLevel(batteryLevel);
    }

    @Override
    public void voltageChanged(int voltage) {
        dataModel.setVoltage(voltage);
    }









    private class SendtoBT extends AsyncTask<Float, Void ,Boolean> {

        @Override
        protected Boolean doInBackground(Float... params) {

            if (!btSending) {
                try {
                    btSending = true;
                    if (connected) {
                        BTMessage msg = new BTMessage();

                        msg.setPayload(dataModel.getFlightDataByteArray());

                        client.sendMessage(msg);

                    }


                } catch (IOException E) {
                    Log.e(TAG, E.getMessage(), E);

                } finally {
                    btSending = false;

                }
            }
            return true;
        }
    }



}

