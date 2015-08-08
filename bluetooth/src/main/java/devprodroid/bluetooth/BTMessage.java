package devprodroid.bluetooth;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTMessage {

    private byte[] payload;

    public BTMessage() {
    }



    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void read(InputStream input) throws IOException {
        byte[] buffer = new byte[256];
        int bytes;
        bytes = input.read(buffer);
        this.payload = buffer;
       // Log.d("BytesRead", Integer.toString(bytes));

    }

    public void write(OutputStream output) throws IOException {

        output.write(this.payload);
    }


    public byte[] getPayload() {
        return this.payload;
    }


}
