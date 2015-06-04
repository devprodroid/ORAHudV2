package devprodroid.bluetooth;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTMessage {
    private static final byte SYNC_BYTE = 71;
    private static final int HEADER_SIZE = 6;
    private byte msgType;
    private byte[] payload;

    public BTMessage() {
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void read(InputStream input) throws IOException {
        byte[] header = new byte[6];
        if(6 != input.read(header)) {
            throw new IOException("Incomplete header.");
        } else {
            byte i = 0;
            int var7 = i + 1;
            if(header[i] != 71) {
                throw new IOException("Message does not have syncronization byte.");
            } else {
                byte msgType = header[var7++];
                int expectedPayloadLength = (header[var7++] & 255) << 24 | (header[var7++] & 255) << 16 | (header[var7++] & 255) << 8 | (header[var7++] & 255) << 0;
                if(expectedPayloadLength < 0) {
                    throw new IOException("Invalid header.");
                } else {
                    byte[] payload = new byte[expectedPayloadLength];
                    if(expectedPayloadLength != input.read(payload)) {
                        throw new IOException("Incomplete message.");
                    } else {
                        this.msgType = msgType;
                        this.payload = payload;
                    }
                }
            }
        }
    }

    public void write(OutputStream output) throws IOException {
        this.validate();
        int msgLength = 6 + this.payload.length;
        byte[] msgBuffer = new byte[msgLength];
        byte i = 0;
        int var6 = i + 1;
        msgBuffer[i] = 71;
        msgBuffer[var6++] = this.msgType;
        msgBuffer[var6++] = (byte)(this.payload.length >> 24);
        msgBuffer[var6++] = (byte)(this.payload.length >> 16);
        msgBuffer[var6++] = (byte)(this.payload.length >> 8);
        msgBuffer[var6++] = (byte)this.payload.length;
        System.arraycopy(this.payload, 0, msgBuffer, var6, this.payload.length);
        int var10000 = var6 + this.payload.length;
        output.write(msgBuffer);
    }

    public byte getMsgType() {
        return this.msgType;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    protected void validate() throws IOException {
        if(this.payload == null) {
            throw new IOException("No payload");
        }
    }
}
