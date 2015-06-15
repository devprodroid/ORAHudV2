package devprodroid.orahudclient; /**
 * Created by robert on 02.06.15.
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.Date;

import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTServerService;
import devprodroid.bluetooth.Event;


public class BT_Service extends BTServerService {

    public static final String BROADCAST_ACTION = "devprodroid.orahudclient.displayevent";
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;


    private static final String TAG = BT_Service.class.getPackage().getName();


    public BT_Service() {
    }

    public void onCreate() {

        super.onCreate();

        //Intent for connection with HUDActivity
        intent = new Intent(BROADCAST_ACTION);
        handler.removeCallbacks(sendUpdatesToUI);

    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            //DisplayLoggingInfo();
            handler.postDelayed(this, 5000); // 5 seconds
        }
    };


    @Override
    public void onClientConnected(BluetoothSocket bluetoothSocket) {

    }

    @Override
    public void onServerStopped() {
        Log.i(TAG, "server stopped");
    }

    @Override
    public BTMessage onMsgReceived(BluetoothDevice bluetoothDevice, BTMessage btMessage) {

        switch (btMessage.getMsgType()) {

            case Event.MSG_TYPE_KEYBOARD:
            case Event.MSG_TYPE_MOUSE_DOWN:
            case Event.MSG_TYPE_MOUSE_UP:
            case Event.MSG_TYPE_MOUSE_MOVE:
            case Event.MSG_TYPE_BATT_LEVEL:

                return handle(btMessage);
            default:

                Log.w(TAG, "Not implemented");
                return null;
        }
    }

    @Override
    public boolean isAuthorized(BluetoothDevice bluetoothDevice) {
        //TODO : add auth system
        return true;
    }

    @Override
    public void onDisconnected(BluetoothDevice bluetoothDevice) {
        Log.i(TAG, bluetoothDevice.getName() + " disconnected");
    }


    //TODO: Implement message Handling
    public BTMessage handle(BTMessage msg) {
        byte eventType = msg.getMsgType();
        byte[] payload = msg.getPayload();
        switch (eventType) {

            case 4: //notify activity ! wohooo
                intent.putExtra("time", new Date().toLocaleString());
                intent.putExtra("counter", String.valueOf(++counter));
                sendBroadcast(intent);

                break;
            default:
                throw new IllegalStateException();
        }

        return null;
    }

}