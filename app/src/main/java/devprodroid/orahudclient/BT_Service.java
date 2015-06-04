package devprodroid.orahudclient; /**
 * Created by robert on 02.06.15.
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import devprodroid.bluetooth.BTMessage;
import devprodroid.bluetooth.BTServerService;
import devprodroid.bluetooth.Event;


public class BT_Service extends BTServerService {
    private static final String TAG = BT_Service.class.getPackage().getName();
    public BT_Service() {
    }

    public void onCreate() {
        super.onCreate();
    }

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



    public BTMessage handle(BTMessage msg) {
        byte eventType = msg.getMsgType();
        byte[] payload = msg.getPayload();
        switch(eventType) {

            case 4:

                break;
            default:
                throw new IllegalStateException();
        }

        return null;
    }
}