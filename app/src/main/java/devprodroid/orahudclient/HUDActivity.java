package devprodroid.orahudclient;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private static final int REQUEST_ENABLE_BT = 1;

    private GLSurfaceView glSurfaceView;
    private ViewFlipper viewFlipper;
    private float lastX;


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


        // final View controlsView = findViewById(R.id.btn_bt_connect);
        // final View contentView = findViewById(R.id.main_layout);
        serviceIntent = new Intent(this, BT_Service.class);

        isBluetoothAvailable();


        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
//        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
//        mSystemUiHider.setup();
//        mSystemUiHider
//                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
//                    // Cached values.
//                    int mControlsHeight;
//                    int mShortAnimTime;
//
//                    @Override
//                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//                    public void onVisibilityChange(boolean visible) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//                            // If the ViewPropertyAnimator API is available
//                            // (Honeycomb MR2 and later), use it to animate the
//                            // in-layout UI controls at the bottom of the
//                            // screen.
//
//                            if (mControlsHeight == 0) {
//                                mControlsHeight = controlsView.getHeight();
//                            }
//                            if (mShortAnimTime == 0) {
//                                mShortAnimTime = getResources().getInteger(
//                                        android.R.integer.config_shortAnimTime);
//                            }
//                            controlsView.animate()
//                                    .translationY(visible ? 0 : mControlsHeight)
//                                    .setDuration(mShortAnimTime);
//                        } else {
//                            // If the ViewPropertyAnimator APIs aren't
//                            // available, simply show or hide the in-layout UI
//                            // controls.
//                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
//                        }
//
//                        if (visible && AUTO_HIDE) {
//                            // Schedule a hide().
//                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
//                        }
//                    }
//                });
//
//        // Set up the user interaction to manually show or hide the system UI.
//        contentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (TOGGLE_ON_CLICK) {
//                    mSystemUiHider.toggle();
//                } else {
//                    mSystemUiHider.show();
//                }
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.


        Button bt_connectBtn = (Button) findViewById(R.id.btn_bt_connect);
        //bt_connectBtn.setOnTouchListener(mDelayHideTouchListener);
        bt_connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartBtnClicked(v);
            }
        });


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
                    showError("Incorrect Bluetooth UUID");
                    return;
                }

                // Start the service
                try {
                    startBTService();
                } catch (Exception e) {

                    showError("Bluetooth not running");

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


    // Using the following method, we will handle all screen swaps.
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

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
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
     * Modify ui according to recieved bluetoth messages
     *
     * @param intent Intent with Payload
     */
    private void updateUI(Intent intent) {


        parsePayload(intent);

        // Update UI Elements

        final TextView tvText = (TextView) findViewById(R.id.tv2);
        tvText.setText("Accelero: " + dataModel.getAccZ().toString());



    }

    /**
     * Read Intent Payload and update DataModel
     *
     * @param intent Intent with payload
     */
    private void parsePayload(Intent intent) {

        byte[] message = intent.getByteArrayExtra("payload");
        dataModel.setFlightData(message);
        //glSurfaceView.requestRender();
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };

//    Handler mHideHandler = new Handler();
//    Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mSystemUiHider.hide();
//        }
//    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }
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


    public boolean isBtServiceRunning() {
        return isMyServiceRunning(BT_Service.class);
    }

    public boolean isBluetoothAvailable() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.e("BTDeviceDiscover", "No bluetooth adapter detected/available.");
            return false;
        }
        //Check if bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            Log.e("BTDeviceDiscover", "Bluetooth is DISABLED. Asking the user");
            return false;
        }
        return true;
    }


}
