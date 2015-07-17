package devprodroid.bluetooth;

/**
 * Created by robert on 04.06.15.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;


public class BTServer extends Thread {
    private final BluetoothServerSocket serverSocket;
    private BTServer.Callback callback;
    private boolean running;

    public BTServer(BTServer.Callback callback, String btServiceName, UUID uuid) throws IOException {
        super("Server " + btServiceName);
        this.callback = callback;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(btServiceName, uuid);
    }

    public void run() {
        ArrayList socketListeners = new ArrayList();
        this.running = true;

        while(this.running) {
            BluetoothSocket socketListener = null;

            try {
                socketListener = this.serverSocket.accept();
            } catch (IOException var4) {
                continue;
            }

            if(socketListener != null) {
                this.callback.onClientConnected(socketListener);
                BTSocketListener socketListener1 = new BTSocketListener(socketListener, this.callback);
                socketListener1.start();
                socketListeners.add(socketListener1);
            }
        }

        Iterator socketListener3 = socketListeners.iterator();

        while(socketListener3.hasNext()) {
            BTSocketListener socketListener2 = (BTSocketListener)socketListener3.next();
            socketListener2.cancel();
        }

        this.callback.onServerStopped();
    }

    public void cancel() {
        this.running = false;

        try {
            this.serverSocket.close();
        } catch (IOException var2) {
        }

    }

    public interface Callback extends BTSocketListener.Callback {
        void onClientConnected(BluetoothSocket var1);

        void onServerStopped();
    }
}
