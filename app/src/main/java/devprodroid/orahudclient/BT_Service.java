package devprodroid.orahudclient; /**
 * Created by robert on 02.06.15.
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTServerService;


public class BT_Service extends BTServerService {

    public static final String BROADCAST_ACTION = "devprodroid.orahudclient.displayevent";

    public static final Byte bDontCare =0;
    public static final Byte bConnected =1;
    public static final Byte bDisconnected =2;

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
        BTMessage msgTmp = new BTMessage();
        msgTmp.setConnectionState(bConnected);
        handle(msgTmp);
    }

    @Override
    public void onServerStopped() {
        Log.i(TAG, "server stopped");
    }

    @Override
    public BTMessage onMsgReceived(BluetoothDevice bluetoothDevice, BTMessage btMessage) {
                return handle(btMessage);
    }

    @Override
    public boolean isAuthorized(BluetoothDevice bluetoothDevice) {
        //TODO : add auth system
        return true;
    }

    @Override
    public void onDisconnected(BluetoothDevice bluetoothDevice) {
        Log.i(TAG, bluetoothDevice.getName() + " disconnected");
        BTMessage msgTmp = new BTMessage();
        msgTmp.setConnectionState(bDisconnected);
        handle(msgTmp);
    }


    //TODO: Implement message Handling
    public BTMessage handle(BTMessage msg) {
        byte connectionState = msg.getConnectionState();

        if (connectionState == bDontCare) {
            byte[] payload = msg.getPayload();
            intent.putExtra("connection", connectionState);
            intent.putExtra("payload", payload);
            sendBroadcast(intent);
        } else {
            intent.putExtra("connection", connectionState);
            sendBroadcast(intent);
        }

        return null;
    }

}