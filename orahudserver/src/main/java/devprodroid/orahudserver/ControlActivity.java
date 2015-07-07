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
import de.yadrone.base.navdata.AttitudeListener;
import devprodroid.bluetooth.BTClient;
import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTSocketListener;


/**
 * This Activity displays Control interface and informations for the droneii
 */
public class ControlActivity extends AppCompatActivity implements BTSocketListener.Callback, AttitudeListener {


    private final String TAG = getClass().getPackage().getName();
    public final static String MSG_BT_UUID = "MSG_BT_UUID";
    public final static String MSG_MAC_BT_DEVICE_ADDRESS = "MSG_MAC_BT_DEVICE_ADDRESS";

    private boolean connected = false;

    private BluetoothDevice device;

    private BTClient client;
    private boolean btSending =false;
    private ProgressDialog mProgressDialog;

    private TextView tvText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);


        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        //Param reading
        Intent act_param = this.getIntent();
        final String macBTDeviceAddress = act_param.getStringExtra(MSG_MAC_BT_DEVICE_ADDRESS);
        final UUID serv_UUID = UUID.fromString(act_param.getStringExtra(MSG_BT_UUID));


        tvText = (TextView) findViewById(R.id.text_navdata);

       initButtons();



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

                        //connection successfull
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

    private void initButtons()
    {
        YADroneApplication app = (YADroneApplication)getApplication();
        final IARDrone drone = app.getARDrone();

        Button forward = (Button)findViewById(R.id.cmd_forward);
        forward.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().forward(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        Button backward = (Button)findViewById(R.id.cmd_backward);
        backward.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().backward(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button left = (Button)findViewById(R.id.cmd_left);
        left.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().goLeft(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button right = (Button)findViewById(R.id.cmd_right);
        right.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().goRight(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        Button up = (Button)findViewById(R.id.cmd_up);
        up.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().up(40);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        Button down = (Button)findViewById(R.id.cmd_down);
        down.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().down(40);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button spinLeft = (Button)findViewById(R.id.cmd_spin_left);
        spinLeft.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().spinLeft(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });


        Button spinRight = (Button)findViewById(R.id.cmd_spin_right);
        spinRight.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    drone.getCommandManager().spinRight(20);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    drone.hover();

                return true;
            }
        });

        final Button landing = (Button)findViewById(R.id.cmd_landing);
        landing.setOnClickListener(new OnClickListener() {
            boolean isFlying = false;
            public void onClick(View v)
            {
                if (!isFlying)
                {
                    drone.takeOff();
                    landing.setText("Landing");
                }
                else
                {
                    drone.landing();
                    landing.setText("Take Off");
                }
                isFlying = !isFlying;
            }
        });

        Button emergency = (Button)findViewById(R.id.cmd_emergency);
        emergency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
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


    public static byte [] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public void attitudeUpdated(final float pitch, final float roll, final float yaw) {


        if (!btSending) {

                    btSending=true;
                    try {
                          if (connected) {

                              byte[] bytes =  intToByteArray(Math.round(roll/1000));

                                //Log.e(TAG, String.valueOf(roll));


                            BTMessage msg = new BTMessage();
                            msg.setMsgType((byte) 4);
                            msg.setPayload(bytes);

                            client.sendMessage(msg);
                        }
                    } catch (IOException E) {
                        Log.e(TAG, E.getMessage(), E);

                    }
                    finally {
                        btSending=false;
                    }


        }
    }

    public void attitudeUpdated(float arg0, float arg1) {
    }

    public void windCompensation(float arg0, float arg1) {
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }


    /**
     * Callback for the refresh button.
     *
     * @param v
     */
    public void onBtnClicked(View v) {
        //  YADroneApplication app = (YADroneApplication) getApplication();
         // final IARDrone drone = app.getARDrone();
        //  drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 10);
        try {
        //sendMessage();
        //
          byte[] bytes =  intToByteArray(215);
        BTMessage msg = new BTMessage();
        msg.setMsgType((byte) 4);
        msg.setPayload(bytes);

        client.sendMessage(msg);
        } catch (IOException E) {
            Log.e(TAG, E.getMessage(), E);

        }


    }







    private void sendMessage() {
        if (connected) {
            try {
                BTMessage msg = new BTMessage();
                msg.setMsgType((byte) 4);

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


}
