package devprodroid.orahudserver.control;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import de.yadrone.base.exception.ARDroneException;
import de.yadrone.base.exception.CommandException;
import de.yadrone.base.exception.ConfigurationException;
import de.yadrone.base.exception.IExceptionListener;
import de.yadrone.base.exception.NavDataException;
import de.yadrone.base.navdata.AcceleroListener;
import de.yadrone.base.navdata.AcceleroPhysData;
import de.yadrone.base.navdata.AcceleroRawData;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;
import de.yadrone.base.navdata.ControlState;
import de.yadrone.base.navdata.DroneState;
import de.yadrone.base.navdata.MagnetoData;
import de.yadrone.base.navdata.MagnetoListener;
import de.yadrone.base.navdata.NavDataManager;
import de.yadrone.base.navdata.StateListener;
import devprodroid.bluetooth.BTClient;
import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTSocketListener;
import devprodroid.bluetooth.DataModel;
import devprodroid.orahudserver.R;
import devprodroid.orahudserver.SettingsActivity;
import devprodroid.orahudserver.YADroneApplication;






/**
 * This Activity displays Control interface and informations for the drone
 */
public class ControlActivity extends Activity implements SensorEventListener,
        BTSocketListener.Callback, AttitudeListener, AltitudeListener, BatteryListener,
        AcceleroListener, MagnetoListener,IExceptionListener,StateListener {


    public final static String MSG_BT_UUID = "MSG_BT_UUID";
    public final static String MSG_MAC_BT_DEVICE_ADDRESS = "MSG_MAC_BT_DEVICE_ADDRESS";
    public final static String MSG_DEBUG_ENABLED = "MSG_DEBUG_ENABLED";
    private final static int INTERVAL = 500;
    private final String TAG = getClass().getPackage().getName();
    private Handler mHandler;
    private boolean connected = false;
    private BluetoothDevice device;
    private BTClient mBTClient;
    private Boolean btSending = false;
    private ProgressDialog mProgressDialog;
    private TextView tvText;
    private DataModel mDataModel;
    private DroneControl mDroneControl;
    private YADroneApplication mApp;
    private IARDrone mDrone;
    private NavDataManager nav;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private static TextView tv;
    private boolean mDroneSendRunning = false;
    private boolean mDebugMode;





    public static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        tv = (TextView) findViewById(R.id.tv);

        registerSensors();

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //startHandler();
        initButtons();

        initDataModel();




        initDrone();
        // addDroneListeners();
        Log.d(TAG, "OnStart");

        initBluetoothConnection();

    }

    /**
     * get drone reference and create DroneControl instance
     */
    private void initDrone() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean outdoorMode = sharedPref.getBoolean(SettingsActivity.KEY_PREF_OUTDOOR_MODE, false);

        mApp = (YADroneApplication) getApplication();

        mDrone = mApp.getARDrone();


        mDroneControl = new DroneControl(mDrone,outdoorMode);
        nav = mDrone.getNavDataManager();
       // mDrone.getCommandManager().setNavDataDemo(false);
        mDroneControl.startThread();
        mDroneSendRunning = true;

        addDroneListeners();




    }

    /**
     * Create DataModel Instance
     */
    private void initDataModel() {
        mDataModel = new DataModel();
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
        mDebugMode = act_param.getBooleanExtra(MSG_DEBUG_ENABLED,false);


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
                    SensorManager.SENSOR_DELAY_NORMAL);
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

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
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
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
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


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mDroneControl.setGoUpDemand(false);


                }

                return true;
            }
        });

        btn_goDown.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDroneControl.setGoDownDemand(true);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mDroneControl.setGoDownDemand(false);

                }

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
                                   }

        );

        Button emergency = (Button) findViewById(R.id.cmd_emergency);
        emergency.setOnClickListener(new View.OnClickListener()

                                     {
                                         public void onClick(View v) {
                                             mDrone.reset();
                                             Log.d("DebugLand", "reset");
                                         }
                                     }

        );
    }

    @Override
    public void onResume() {
        super.onResume();

        initDrone();
     //   addDroneListeners();

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mDroneControl.startThread();
        mDroneSendRunning = true;
        startHandler();
        Log.d(TAG, "OnResume");

    }

    /**
     * Register for drone listeners
     */
    private void addDroneListeners() {

        if (mDebugMode) mDrone.addExceptionListener(this);

        nav.addAttitudeListener(this);
        nav.addBatteryListener(this);
        nav.addAltitudeListener(this);
        nav.addAcceleroListener(this);
        nav.addMagnetoListener(this);
        nav.addStateListener(this);
        Log.d(TAG, "addDroneListeners finished");


    }
    @Override
    public void onPause() {
        super.onPause();


        mHandler.removeCallbacksAndMessages(mHandler);

        mDroneSendRunning = false;
        mDroneControl.setStop(true);

        mDroneControl.stopThread();


        removeDroneListeners();

        mDrone.stop();
        //mDrone.reset();

        Log.d("DebugLand", "onPause");


        mSensorManager.unregisterListener(this);


         // this.finish();


    }

    /**
     * unregister drone listeners
     */
    private void removeDroneListeners() {
        mDrone.removeExceptionListener(this);
        nav.removeAttitudeListener(this);
        nav.removeBatteryListener(this);
        nav.removeAltitudeListener(this);
        nav.removeAcceleroListener(this);
        nav.removeMagnetoListener(this);
        nav.removeStateListener(this);

    }


    public void attitudeUpdated(final float pitch, final float roll, final float yaw) {
        mDataModel.setPitch(Math.round(pitch / 1000));
        mDataModel.setRoll(Math.round(roll / 1000));
        mDataModel.setYaw(Math.round(yaw / 1000));
       // Log.d(TAG, mDataModel.toString());
        //    new SendtoBT().execute();
    }

    public void attitudeUpdated(float arg0, float arg1) {
    }

    public void windCompensation(float pitch, float roll) {
        mDataModel.setPitchCompensation(Math.round(pitch / 1000));
        mDataModel.setRollCompensation(Math.round(roll / 1000));


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

    }

    /**
     * Writes Altitude and climbrate
     *
     * @param altitude
     */
    @Override
    public void receivedExtendedAltitude(Altitude altitude) {

        int zVel = Math.round(altitude.getZVelocity());

        mDataModel.setAccZ(zVel);

        mDataModel.setAltitude(altitude.getRef());
       // Log.d("ALTITUDE", Integer.toString(altitude.getRef()));

    }

    @Override
    public void batteryLevelChanged(int batteryLevel) {
        mDataModel.setBatteryLevel(batteryLevel);
        //  Log.d(TAG, "AR.Drone Battery: " + batteryLevel);


    }

    @Override
    public void voltageChanged(int voltage) {
        // //mDataModel.setVoltage(voltage);
    }

    @Override
    public void receivedRawData(AcceleroRawData acceleroRawData) {

    }

    @Override
    public void receivedPhysData(AcceleroPhysData acceleroPhysData) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //landscape config

        mDroneControl.setPitch_angle(event.values[0]);
        mDroneControl.setRoll_angle(event.values[1]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void received(MagnetoData magnetoData) {


    }


    public void startHandler() {


        mHandler = new Handler();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {


                    BTMessage msg = new BTMessage();
                    if (!btSending) {
                        try {
                            btSending = true;
                            if (connected) {
                                msg.setPayload(mDataModel.getFlightDataByteArray());
                                mBTClient.sendMessage(msg);
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            btSending = false;

                        }
                    }
               if (mDroneSendRunning)
                   mHandler.postDelayed(this, 20);
            }
        }, 20);

    }

    @Override
    public void exeptionOccurred(ARDroneException exc) {



        if (exc instanceof ConfigurationException)
        {
            Log.e(TAG,exc.getMessage());
        }
        else if (exc instanceof CommandException)
        {
            Log.e(TAG,exc.getMessage());
        }
        else if (exc instanceof NavDataException) {
            Log.e(TAG,exc.getMessage());
        }




    }

    @Override
    public void stateChanged(DroneState droneState) {

           mDataModel.setIsFlying(droneState.isFlying());
           mDroneControl.setIsFlying(droneState.isFlying());




        if (droneState.isBatteryTooLow())
            Log.d("DroneState", "isBatteryTooLow True");
        if (droneState.isTooMuchWind())
            Log.d("DroneState", "isTooMuchWind True");


    }

    @Override
    public void controlStateChanged(ControlState controlState) {

    }


    private class SendtoBT extends AsyncTask<Float, Void, Boolean> {
        BTMessage msg = new BTMessage();

        @Override
        protected Boolean doInBackground(Float... params) {

            if (!btSending) {
                try {
                    btSending = true;
                    if (connected) {


                        msg.setPayload(mDataModel.getFlightDataByteArray());

                        mBTClient.sendMessage(msg);


                        //mDataModel.setFlightData(mDataModel.getFlightDataByteArray());

                        // Log.d(TAG, "Send Payload" + mDataModel.toString());


                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    btSending = false;

                }
            }
            return true;
        }
    }





}

