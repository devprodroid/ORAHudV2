package devprodroid.orahudclient;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.UUID;

import devprodroid.bluetooth.BTServerService;
import devprodroid.bluetooth.DataModel;
import devprodroid.orahudclient.glSurface.GLSurf;
import devprodroid.orahudclient.util.SystemUiHider;

/**
 * Connection Activity displaying a HUD on the ORA
 */
public class HUDActivity extends Activity {


    private static final int TOAST_DURATION = Toast.LENGTH_SHORT;
    private static final String serv_UUID = "07419c1a-090c-11e5-a6c0-1697f925ec7b";
    private static final String serv_name = "ORA Server";


    public static final Byte bDontCare =0;
    public static final Byte bConnected =1;
    public static final Byte bDisconnected =2;

    private static final int REQUEST_ENABLE_BT = 1;

    private GLSurfaceView glSurfaceView;
    private ViewFlipper viewFlipper;

    private float lastX;

    ValueAnimator colorAnim;

    private TextView tvBattery;
    private TextView tvWifi;

    private TextView tvAltitude;
    private TextView tvYaw;

    private boolean mConnected =false;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    /**
     * The Instance of DataModel containing the displayed Data
     */
    public DataModel dataModel;

    Intent serviceIntent;


    private boolean mBatteryAnimationRunning=false;

    public HUDActivity() {
        dataModel = new DataModel();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_hud);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glSurfaceView = new GLSurf(this,dataModel);


        RelativeLayout layout = (RelativeLayout) findViewById(R.id.hud_1);

        RelativeLayout.LayoutParams glParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        layout.addView(glSurfaceView, glParams);

        serviceIntent = new Intent(this, BT_Service.class);

        isBluetoothAvailable();

        Button bt_connectBtn = (Button) findViewById(R.id.btn_bt_connect);
        //bt_connectBtn.setOnTouchListener(mDelayHideTouchListener);
        bt_connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartBtnClicked(v);
            }
        });

        tvBattery = (TextView) findViewById(R.id.tvBattery);
        colorAnim = ObjectAnimator.ofInt(tvBattery, "backgroundColor", Color.RED, Color.BLACK);

        tvWifi = (TextView) findViewById(R.id.tvWifi);

        tvAltitude = (TextView) findViewById(R.id.tvAltitude);
        tvYaw = (TextView) findViewById(R.id.tvYaw);
    }


    private void onStartBtnClicked(View v) {
        if (v.getId() == R.id.btn_bt_connect) {

            //viewFlipper.showNext();
            Button btn_start = ((Button) findViewById(R.id.btn_bt_connect));

            if (btn_start.getText().toString().compareTo(getResources().getString(R.string.btn_bt_stop)) != 0) {
                //Service is stopped.
                // Check the UUID
                try {

                    UUID.fromString(serv_UUID);
                } catch (IllegalArgumentException e) {
                    showError(getString(R.string.lblIncorrectUUID));
                    return;
                }

                // Start the service
                try {
                    startBTService();
                } catch (Exception e) {

                    showError(getString(R.string.lblBTNotRunning));

                }
            } else {
                try {
                    stopService(serviceIntent);
                    serviceIntent = null;
                    btn_start.setText(R.string.btn_bt_start);
                } catch (NullPointerException E) {
                    showError("Service not running");
                }
            }
        }

    }


    // Using the folloing method, we will handle all screen swaps.
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float currentX = touchevent.getX();

                // Handling left to right screen swap.
                if (lastX < currentX) {

                    // If there aren't any other children, just break.
                    if (viewFlipper.getDisplayedChild() == 0)
                        break;

                    // Next screen comes in from left.
                    viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);
                    // Current screen goes out from right.
                    viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);

                    // Display next screen.
                    viewFlipper.showNext();

                }

                // Handling right to left screen swap.
                if (lastX > currentX) {

                    // If there is a child (to the left), kust break.
                    if (viewFlipper.getDisplayedChild() == 3)
                        break;

                    // Next screen comes in from right.
                    viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
                    // Current screen goes out from left.
                    viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                    // Display previous screen.
                    viewFlipper.showPrevious();
                }
                break;
        }
      //  glSurfaceView.requestRender();
        return false;
    }


    private void startBTService() {
        serviceIntent = new Intent(this, BT_Service.class);
        serviceIntent.putExtra(BTServerService.MSG_SERVER_NAME,
                serv_name);
        serviceIntent.putExtra(BTServerService.MSG_BT_UUID, serv_UUID);

        if (isBluetoothAvailable()) {
            startService(serviceIntent);
        }


        if (isBtServiceRunning()) {
            // Change button form.
            Button btn_start = ((Button) findViewById(R.id.btn_bt_connect));
            btn_start.setText(getString(R.string.btn_bt_stop));
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    /**
     * Receive messages from the BTService containing the Data from the Drone
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
           updateUI(intent);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(BT_Service.BROADCAST_ACTION));

        startBTService();

        glSurfaceView.onResume();
        glSurfaceView.requestRender();

    }

    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        unregisterReceiver(broadcastReceiver);

        if (isBtServiceRunning()) {
            stopService(serviceIntent);
        }
    }


    /**
     * Modify ui according to recieved bluetooth messages
     *
     * @param intent Intent with Payload
     */
    private void updateUI(Intent intent) {

        parsePayload(intent);

        // Update UI Elements

        //fixed HUD Values

        animateBatteryIndicator();

        if (mConnected) {
            tvWifi.setText("Wifi: " + dataModel.getLinkQuality() + "/10");
        }
        else
            tvWifi.setText("DISCONECT");

        tvAltitude.setText(getString(R.string.lblAlt) + String.format("%.1f", dataModel.getAltitudeM()) + "m");

        tvYaw.setText("HDG: " + dataModel.getYaw() + "°");
    }

    /**
     * Animates the Battery Indicating Textview with color and text changes
     */
    private void animateBatteryIndicator() {

        if ((dataModel.getBatteryTooLow()) || (dataModel.getBatteryLevel() < 20)) {
            tvBattery.setText(getString(R.string.lblBatt) + dataModel.getBatteryLevel() + "%");
            colorAnim.setDuration(500);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);

            if(!colorAnim.isRunning()) {
                colorAnim.start();
            }
            tvBattery.setTextColor(Color.WHITE);
        } else {
            colorAnim.end();
            tvBattery.setTextColor(Color.GREEN);
            tvBattery.setBackgroundResource(R.drawable.lblbackborder);
            tvBattery.setText(getString(R.string.lblBatt) + dataModel.getBatteryLevel() + "%");
        }
    }

    /**
     * Read Intent Payload and update DataModel with current values
     *
     * @param intent Intent with payload
     */
    private void parsePayload(Intent intent) {
        byte connection= intent.getByteExtra("connection", bDontCare);

        if (connection==bDontCare) {

            byte[] message = intent.getByteArrayExtra("payload");
            dataModel.setFlightData(message);
        } else {
            if (connection == bConnected) {
                Toast.makeText(HUDActivity.this, "Connected", TOAST_DURATION).show();
                // Next screen comes in from right.
                viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
                // Current screen goes out from left.
                viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);


                viewFlipper.setDisplayedChild(2);
                mConnected=true;
            }
            if (connection == bDisconnected) {
                Toast.makeText(HUDActivity.this, "Disconnected", TOAST_DURATION).show();
                mConnected=false;
                dataModel.resetData();

            }
        }
    }


    public void showError(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HUDActivity.this, msg, TOAST_DURATION).show();
            }
        });
    }

    /**
     * @param serviceClass
     * @return Background service running
     * http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
     * Peter Mortensen
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if Bluetooth Service is running
     * @return
     */
    public boolean isBtServiceRunning() {
        return isMyServiceRunning(BT_Service.class);
    }

    /**
     * Check if Bluetooth is available and ask user to enable
     * @return
     */
    public boolean isBluetoothAvailable() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.e("BTDeviceDiscover", getString(R.string.lblNoBTAdapter));
            return false;
        }
        //Check if bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            Log.e("BTDeviceDiscover", getString(R.string.lblBTDisabled));
            return false;
        }
        return true;
    }


}
