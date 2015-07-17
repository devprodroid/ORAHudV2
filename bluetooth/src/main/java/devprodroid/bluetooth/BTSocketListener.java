package devprodroid.bluetooth;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTSocketListener extends Thread {
    private final BluetoothSocket socket;
    private final BTSocketListener.Callback callback;
    private boolean running = false;

    public BTSocketListener(BluetoothSocket socket, BTSocketListener.Callback callback) {
        super("socket listener");
        this.socket = socket;
        this.callback = callback;
    }

    public void run() {
        this.running = true;
        BluetoothDevice device = this.socket.getRemoteDevice();

        try {
            if(this.callback.isAuthorized(device)) {
                InputStream e = this.socket.getInputStream();
                OutputStream clientOutput = this.socket.getOutputStream();

                while(this.running) {
                    BTMessage msg = new BTMessage();
                    msg.read(e);
                    BTMessage response = this.callback.onMsgReceived(device, msg);
                    if(response != null) {
                        response.write(clientOutput);
                    }
                }
            }
        } catch (IOException var6) {
            this.callback.onDisconnected(device);
        }

    }

    public void cancel() {
        this.running = false;

        try {
            this.socket.close();
        } catch (IOException var2) {
        }

    }

    public interface Callback {
        BTMessage onMsgReceived(BluetoothDevice var1, BTMessage var2);

        boolean isAuthorized(BluetoothDevice var1);

        void onDisconnected(BluetoothDevice var1);
    }
}
