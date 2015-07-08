package devprodroid.bluetooth;

/**
 * Created by robert on 15.06.15.
 */
public class DataModel {




    private Integer pitch = 0;
    private Integer roll = 0;
    private Integer yaw = 0;

    public byte[] getFlightDataByteArray() {
        return int2byte(FlightData);
    }


    public int[] getFlightDataIntArray() {
        return FlightData;
    }


    public void setFlightData(byte[] input){
        FlightData=byte2int(input);
        updateFields();

    }

    public static int[] byte2int(byte[]src) {
        int dstLength = src.length >>> 2;
        int[]dst = new int[dstLength];

        for (int i=0; i<dstLength; i++) {
            int j = i << 2;
            int x = 0;
            x += (src[j++] & 0xff) << 0;
            x += (src[j++] & 0xff) << 8;
            x += (src[j++] & 0xff) << 16;
            x += (src[j++] & 0xff) << 24;
            dst[i] = x;
        }
        return dst;
    }

    private static byte[] int2byte(int[]src) {
        int srcLength = src.length;
        byte[]dst = new byte[srcLength << 2];

        for (int i=0; i<srcLength; i++) {
            int x = src[i];
            int j = i << 2;
            dst[j++] = (byte) ((x >>> 0) & 0xff);
            dst[j++] = (byte) ((x >>> 8) & 0xff);
            dst[j++] = (byte) ((x >>> 16) & 0xff);
            dst[j++] = (byte) ((x >>> 24) & 0xff);
        }
        return dst;
    }

    /**
     * Array containing the flightdata
     * Spec:
     * [0] pitch
     * [1] roll
     * [2] yaw
     */
    private int[] FlightData;

    public DataModel() {
        FlightData = new int[64];
        linkValuesToArrayIndices();
    }


    private void linkValuesToArrayIndices() {
        FlightData[0] = pitch;
        FlightData[1] = roll;
        FlightData[2] = yaw;

    }

    public void updateFields() {
        pitch = FlightData[0]  ;
        roll = FlightData[1] ;
        yaw =  FlightData[2] ;

    }


    /**
     * Getter and setter Section
     */
    public Integer getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
        linkValuesToArrayIndices();
    }

    public Integer getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
        linkValuesToArrayIndices();
    }

    public Integer getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
        linkValuesToArrayIndices();
    }
}
