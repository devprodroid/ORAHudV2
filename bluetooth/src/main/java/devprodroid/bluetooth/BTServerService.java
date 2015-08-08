package devprodroid.bluetooth;

/**
 * Created by robert on 04.06.15.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import devprodroid.bluetooth.BTServer.Callback;


public abstract class BTServerService extends Service implements BTSocketListener.Callback, Callback {
    private static final String TAG = BTServerService.class.getName();
    private String serverName;
    private UUID serverUUID;
    public static final String MSG_SERVER_NAME = "MSG_SERVER_NAME";
    public static final String MSG_BT_UUID = "MSG_BT_UUID";
    private BluetoothAdapter btAdapter;
    private BTServer server;

    public BTServerService() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.serverName = intent.getStringExtra("MSG_SERVER_NAME");
        if(this.serverName == null) {
            throw new IllegalArgumentException("Invalid server name.");
        } else {
            this.serverUUID = UUID.fromString(intent.getStringExtra("MSG_BT_UUID"));
            this.btAdapter = BluetoothAdapter.getDefaultAdapter();
            Runnable onBTEnabled = new Runnable() {
                public void run() {
                    BTServerService.this.restartServer();
                }
            };
            if(this.btAdapter.isEnabled()) {
                (new Thread(onBTEnabled, "start server")).start();
                return 2;
            } else {
                Log.e(TAG, "BlueTooth not enabled");
                throw new IllegalStateException("Bluetooth not enabled.");
            }
        }
    }

    public void onDestroy() {
        if(this.server != null) {
            this.server.cancel();
        }

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void restartServer() {
        Log.i(TAG, "Starting server thread...");

        try {
            if(this.server != null) {
                this.server.cancel();
            }

            this.server = this.createPServer();
            this.server.start();
        } catch (IOException var2) {
            Log.e(TAG, var2.getMessage(), var2);
        }

    }

    protected BTServer createPServer() throws IOException {
        return new BTServer(this, this.serverName, this.serverUUID);
    }
}
