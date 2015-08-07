package devprodroid.orahudserver.control;

import android.app.Activity;
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
import android.util.Log;
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
import de.yadrone.base.navdata.MagnetoData;
import de.yadrone.base.navdata.MagnetoListener;
import devprodroid.bluetooth.BTClient;
import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTSocketListener;
import devprodroid.bluetooth.DataModel;
import devprodroid.orahudserver.R;
import devprodroid.orahudserver.YADroneApplication;


/**
 * This Activity displays Control interface and informations for the drone
 */
public class ControlActivity extends Activity implements SensorEventListener, BTSocketListener.Callback, AttitudeListener, AltitudeListener, BatteryListener, AcceleroListener,MagnetoListener {


    public final static String MSG_BT_UUID = "MSG_BT_UUID";
    public final static String MSG_MAC_BT_DEVICE_ADDRESS = "MSG_MAC_BT_DEVICE_ADDRESS";
    private final static int INTERVAL = 500;
    private final String TAG = getClass().getPackage().getName();
    Handler mHandler;
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
    private SensorManager mSensorManager;
    private Sensor mSensor;


    public static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);


        registerSensors();

        initDrone();

        initButtons();

        initDataModel();

        initBluetoothConnection();

    }


    /**
     * get drone reference and create DroneControl instance
     */
    private void initDrone() {
        mApp = (YADroneApplication) getApplication();

        mDrone = mApp.getARDrone();
        mDroneControl = new DroneControl(mDrone);
    }

    /**
     * Create DataModel Instance
     */
    private void initDataModel() {
        mdataModel = new DataModel();
    }

    /**
     * Connect to Bluetooth device
     */
    private void initBluetoothConnection() {
        //TODO: Runtime configuration changes have to be acknowledged
        //TODO: Implement reconnection mechanism
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        //Param reading
        Intent act_param = this.getIntent();
        final String macBTDeviceAddress = act_param.getStringExtra(MSG_MAC_BT_DEVICE_ADDRESS);
        final UUID serv_UUID = UUID.fromString(act_param.getStringExtra(MSG_BT_UUID));


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
                        // showMsg(e.getMessage());
                        Log.e(TAG, e.getMessage(), e);
                        showMsg("Connection not successful! Make sure Hud Client is running");

                        // finish();
                    } finally {
                        mProgressDialog.dismiss();
                    }
                }

            }.start();
        }
    }

    /**
     * register SensorManager and Listener for Accelerometer
     */
    private void registerSensors() {
        //register Orientation sensor
        mSensorManager = (SensorManager) getSystemService(this.getBaseContext().SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        if (mSensor != null) {
            // Success! There's an accelerometer
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(this, "This device doesnt support STRING_TYPE_ACCELEROMETER",
                    Toast.LENGTH_SHORT).show();
            //stopSelf();
        }
    }

    /**
     * Assign buttons to drone actions.
     * TODO: Put methods in droneControl
     */
    private void initButtons() {


        Button btn_tanslate = (Button) findViewById(R.id.btn_tanslate);
        Button btn_rotate = (Button) findViewById(R.id.btn_rotate);

        Button btn_goUp = (Button) findViewById(R.id.btn_up);
        Button btn_goDown = (Button) findViewById(R.id.btn_down);


        btn_tanslate.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDroneControl.setTranslateMode();
                    mDroneControl.setControlActive(true);

                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    mDroneControl.setControlActive(false);
                    mDroneControl.hover();
                }

                return true;
            }
        });

        btn_rotate.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDroneControl.setRotateMode();

                    mDroneControl.setControlActive(true);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    mDroneControl.setControlActive(false);
                    mDroneControl.hover();
                }

                return true;
            }
        });

        btn_goUp.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDroneControl.setGoUpDemand(true);


                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    mDroneControl.setGoUpDemand(false);

                }

                return true;
            }
        });

        btn_goDown.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDroneControl.setGoDownDemand(true);

                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    mDroneControl.setGoDownDemand(false);

                }

                return true;
            }
        });

//      btn_goUp
//      btn_goDown


//
//        Button up = (Button) findViewById(R.id.cmd_up);
//        up.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().up(40);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });
//
//        Button down = (Button) findViewById(R.id.cmd_down);
//        down.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().down(40);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });
//
//
//        Button spinLeft = (Button) findViewById(R.id.cmd_spin_left);
//        spinLeft.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().spinLeft(20);
//
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });
//
//
//        Button spinRight = (Button) findViewById(R.id.cmd_spin_right);
//        spinRight.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    mDrone.getCommandManager().spinRight(20);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    mDroneControl.hover();
//
//                return true;
//            }
//        });

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
                                   }

        );

        Button emergency = (Button) findViewById(R.id.cmd_emergency);
        emergency.setOnClickListener(new View.OnClickListener()

                                     {
                                         public void onClick(View v) {
                                             mDrone.reset();
                                         }
                                     }

        );
    }


    public void onResume() {
        super.onResume();

        addDroneListeners();

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        mDroneControl.startThread();

    }

    /**
     * Register for drone listeners
     */
    private void addDroneListeners() {


        mDrone.getNavDataManager().addAttitudeListener(this);
        mDrone.getNavDataManager().addBatteryListener(this);
        mDrone.getNavDataManager().addAltitudeListener(this);
        mDrone.getNavDataManager().addAcceleroListener(this);
        mDrone.getNavDataManager().addMagnetoListener(this);



    }

    public void onPause() {
        super.onPause();
        mDroneControl.stopThread();

        removeDroneListeners();

        mSensorManager.unregisterListener(this);
        //  finish();


    }

    /**
     * unregister drone listeners
     */
    private void removeDroneListeners() {

        mDrone.getNavDataManager().removeAttitudeListener(this);
        mDrone.getNavDataManager().removeBatteryListener(this);
        mDrone.getNavDataManager().removeAltitudeListener(this);
        mDrone.getNavDataManager().removeAcceleroListener(this);
        mDrone.getNavDataManager().removeMagnetoListener(this);
    }


    public void attitudeUpdated(final float pitch, final float roll, final float yaw) {

        mdataModel.setPitch(Math.round(pitch / 1000));
        mdataModel.setRoll(Math.round(roll / 1000));
        mdataModel.setYaw(Math.round(yaw / 1000));
        new SendtoBT().execute();
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
      //  Log.d(TAG, "AR.Drone Battery: " + batteryLevel);
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
        float tmp = acceleroPhysData.getPhysAccs()[2];
        mdataModel.setAccZ(Math.round(tmp));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //   float azimuth_angle = event.values[0];
        // float pitch_angle = event.values[1];-3 +3
        //  float roll_angle = event.values[2];


       // TextView tv = (TextView) findViewById(R.id.tv);
      //  tv.setText("Orientation X (Roll) :" + Float.toString(event.values[2]) + "\n" +
      //          "Orientation Y (Pitch) :" + Float.toString(event.values[1]) + "\n" +
      //          "Orientation Z (Yaw) :" + Float.toString(event.values[0]));


        //landscape config

        mDroneControl.setPitch_angle(event.values[0]);
        mDroneControl.setRoll_angle(event.values[1]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void received(MagnetoData magnetoData) {
        TextView tv = (TextView) findViewById(R.id.tv);

        Float magn=magnetoData.getHeadingFusionUnwrapped();
        tv.setText(magn.toString());
    }


    private class SendtoBT extends AsyncTask<Float, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Float... params) {

            if (!btSending) {
                try {
                    btSending = true;
                    if (connected) {
                        BTMessage msg = new BTMessage();

                        msg.setPayload(mdataModel.getFlightDataByteArray());

                        mBTClient.sendMessage(msg);
                        //Log.d(TAG, "Send Payload");

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

