package devprodroid.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by robert on 04.06.15.
 */

public class BTClient implements Closeable {
    private BluetoothSocket socket = null;

    public BTClient(Context context, BTSocketListener.Callback callback, BluetoothDevice device, UUID serverUuid) throws IOException {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket clientSocket = null;
        if(ba.isEnabled()) {
            clientSocket = device.createRfcommSocketToServiceRecord(serverUuid);
            if(clientSocket != null) {
                clientSocket.connect();
            }
        }

        if(clientSocket != null) {
            this.socket = clientSocket;
            (new BTSocketListener(clientSocket, callback)).start();
        } else {
            throw new IOException("Unable to connect to the server.");
        }
    }

    public void close() {
        if(this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException var2) {
            }
        }

    }

    public void sendMessage(BTMessage msg) throws IOException {
        msg.write(this.socket.getOutputStream());
    }
}
