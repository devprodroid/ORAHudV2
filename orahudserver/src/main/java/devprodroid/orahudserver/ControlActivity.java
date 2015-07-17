package devprodroid.orahudserver;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.AcceleroListener;
import de.yadrone.base.navdata.AcceleroPhysData;
import de.yadrone.base.navdata.AcceleroRawData;
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
public class ControlActivity extends AppCompatActivity implements SensorEventListener, BTSocketListener.Callback, AttitudeListener,AltitudeListener, BatteryListener, AcceleroListener {


    public final static String MSG_BT_UUID = "MSG_BT_UUID";
    public final static String MSG_MAC_BT_DEVICE_ADDRESS = "MSG_MAC_BT_DEVICE_ADDRESS";
    private final String TAG = getClass().getPackage().getName();
    private boolean connected = false;

    private BluetoothDevice device;

    private BTClient mBTClient;
    private Boolean btSending = false;
    private ProgressDialog mProgressDialog;

    private TextView tvText;

    private DataModel mdataModel;

    private DroneControl mDroneControl;

    private YADroneApplication mApp;
    private IARDrone mDrone;

    private final static int INTERVAL = 500;
    Handler mHandler;




    private SensorManager mSensorManager;
    private Sensor mSensor;




    public static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //register Orientation sensor
        mSensorManager = (SensorManager) getSystemService(this.getBaseContext().SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mApp = (YADroneApplication) getApplication();
        mDrone = mApp.getARDrone();
        mDroneControl = new DroneControl(mDrone);

        if (mSensor != null) {
            // Success! There's an accelerometer
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(this, "This device doesnt support STRING_TYPE_ACCELEROMETER",
                    Toast.LENGTH_SHORT).show();
            //stopSelf();
        }


        //TODO: Runtime configuration changes have to be acknowledged
       //TODO: Implement reconnection mechanism
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        //Param reading
        Intent act_param = this.getIntent();
        final String macBTDeviceAddress = act_param.getStringExtra(MSG_MAC_BT_DEVICE_ADDRESS);
        final UUID serv_UUID = UUID.fromString(act_param.getStringExtra(MSG_BT_UUID));


        
        //tvText = (TextView) findViewById(R.id.text_navdata);

        initButtons();
        mdataModel = new DataModel();


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
                        mProgressDialog.show();
                        mBTClient = new BTClient(ControlActivity.this, ControlActivity.this, device, serv_UUID);

                        //connection successful
                        connected = true;

                        showMsg("Connection successful!");

                    } catch (IOException e) {
                        showMsg(e.getMessage());
                        Log.e(TAG, e.getMessage(), e);

                        finish();
                    }
                    finally {
                        mProgressDialog.dismiss();
                    }
                }

                ;
            }.start();
        }


       //// WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
       // Display mDisplay = mWindowManager.getDefaultDisplay();

       // Log.d("ORIENTATION_TEST", "getOrientation(): " + mDisplay.getRotation());

    }

    private void initButtons() {


//        Button forward = (Button) findViewById(R.id.cmd_forward);
//        forward.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().forward(20);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });
//
//        Button backward = (Button) findViewById(R.id.cmd_backward);
//        backward.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().backward(20);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });
//
//
//        Button left = (Button) findViewById(R.id.cmd_left);
//        left.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().goLeft(20);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });
//
//
//        Button right = (Button) findViewById(R.id.cmd_right);
//        right.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().goRight(20);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });

        Button up = (Button) findViewById(R.id.cmd_up);
        up.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mDrone.getCommandManager().up(40);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    mDroneControl.hover();

                return true;
            }
        });

        Button down = (Button) findViewById(R.id.cmd_down);
        down.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mDrone.getCommandManager().down(40);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    mDroneControl.hover();

                return true;
            }
        });


        Button spinLeft = (Button) findViewById(R.id.cmd_spin_left);
        spinLeft.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mDrone.getCommandManager().spinLeft(20);

                else if (event.getAction() == MotionEvent.ACTION_UP)
                    mDroneControl.hover();

                return true;
            }
        });


        Button spinRight = (Button) findViewById(R.id.cmd_spin_right);
        spinRight.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mDrone.getCommandManager().spinRight(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    mDroneControl.hover();

                return true;
            }
        });

        final Button landing = (Button) findViewById(R.id.cmd_landing);
        landing.setOnClickListener(new OnClickListener() {


            public void onClick(View v) {
                if (!mDroneControl.isFlying()) {
                    mDroneControl.takeoff();
                    landing.setText("Landing");
                } else {
                    mDroneControl.land();
                    landing.setText("Take Off");
                }

            }
        });

        Button emergency = (Button) findViewById(R.id.cmd_emergency);
        emergency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDrone.reset();
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

        addDroneListeners();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        mDroneControl.startThread();

    }

    private void addDroneListeners() {
        YADroneApplication app = (YADroneApplication) getApplication();
        IARDrone drone = app.getARDrone();

        drone.getNavDataManager().addAttitudeListener(this);
        drone.getNavDataManager().addBatteryListener(this);
        drone.getNavDataManager().addAltitudeListener(this);
        drone.getNavDataManager().addAcceleroListener(this);
    }

    public void onPause() {
        super.onPause();
        mDroneControl.stopThread();

        removeDroneListeners();
        mSensorManager.unregisterListener(this);


    }

    private void removeDroneListeners() {
        YADroneApplication app = (YADroneApplication) getApplication();
        IARDrone drone = app.getARDrone();
        drone.getNavDataManager().removeAttitudeListener(this);
        drone.getNavDataManager().removeBatteryListener(this);
        drone.getNavDataManager().removeAltitudeListener(this);
        drone.getNavDataManager().removeAcceleroListener(this);
    }

    public void attitudeUpdated(final float pitch, final float roll, final float yaw) {

        mdataModel.setPitch(Math.round(pitch / 1000));
        mdataModel.setRoll(Math.round(roll / 1000));
        mdataModel.setYaw(Math.round(yaw / 1000));
        new SendtoBT().execute( );
    }

    public void attitudeUpdated(float arg0, float arg1) {
    }

    public void windCompensation(float pitch, float roll) {
        mdataModel.setPitchCompensation(Math.round(pitch / 1000));
        mdataModel.setRollCompensation(Math.round(roll / 1000));
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

        //TODO: Ask User to try to restore connection
        //finish();
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
        if (mBTClient != null) {
            mBTClient.close();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void receivedAltitude(int altitude) {
       mdataModel.setAltitude(0);
    }

    @Override
    public void receivedExtendedAltitude(Altitude altitude) {

    }

    @Override
    public void batteryLevelChanged(int batteryLevel) {
        mdataModel.setBatteryLevel(batteryLevel);
       // Log.e(TAG, "Batterylevel: " +( (Integer) batteryLevel).toString());
    }

    @Override
    public void voltageChanged(int voltage) {
        //mdataModel.setVoltage(voltage);
    }

    @Override
    public void receivedRawData(AcceleroRawData acceleroRawData) {

    }

    @Override
    public void receivedPhysData(AcceleroPhysData acceleroPhysData) {
        float tmp=acceleroPhysData.getPhysAccs()[2];
        mdataModel.setAccZ(Math.round(tmp));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

     //   float azimuth_angle = event.values[0];
       // float pitch_angle = event.values[1];-3 +3
      //  float roll_angle = event.values[2];


        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText("Orientation X (Roll) :" + Float.toString(event.values[2]) + "\n" +
                "Orientation Y (Pitch) :" + Float.toString(event.values[1]) + "\n" +
                "Orientation Z (Yaw) :" + Float.toString(event.values[0]));



    //landscape config

        mDroneControl.setPitch_angle(event.values[0]);
        mDroneControl.setRoll_angle(event.values[1]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private class SendtoBT extends AsyncTask<Float, Void ,Boolean> {

        @Override
        protected Boolean doInBackground(Float... params) {

            if (!btSending) {
                try {
                    btSending = true;
                    if (connected) {
                        BTMessage msg = new BTMessage();

                        msg.setPayload(mdataModel.getFlightDataByteArray());

                        mBTClient.sendMessage(msg);

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

