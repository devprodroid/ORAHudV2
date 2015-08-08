package devprodroid.bluetooth;

/**
 * Created by robert on 15.06.15.
 */
public class DataModel {


    /**
     * Array containing the flightdata
     * Spec:
     * [1] pitch
     * [2] roll
     * [3] yaw
     */
    private int[] FlightData;


    //from AttitudeListener
    private Integer pitch=0;
    private Integer roll=0;
    private Integer yaw =0;

    private Integer pitchCompensation=0;
    private Integer rollCompensation=0;

    //from AltitudeListener
    private Integer altitude =0;

    //from BatteryListener
    private Integer batteryLevel=0;
    private Integer voltage=0;
    private Integer accZ=0;




    public DataModel() {
        FlightData = new int[64];
        linkValuesToArrayIndices();

    }
    /**
     * Generates an Integer array from a byte array array
     * @param src The Byte Array
     * @return
     */
    public static int[] byte2int(byte[] src) {
        int dstLength = src.length >>> 2;
        int[] dst = new int[dstLength];

            for (int i = 0; i < dstLength; i++) {
                int j = i << 2;
                int x = 0;
                x += (src[j++] & 0xff) << 0;
                x += (src[j++] & 0xff) << 8;//three weeks lifetime wasted
                x += (src[j++] & 0xff) << 16;
                x += (src[j++] & 0xff) << 24;
                dst[i] = x;
            }
            return dst;
    }

    /**
     * Generates a byte array from an integer array
     * @param src The Integer Array
     * @return
     */
    private static byte[] int2byte(int[] src) {

        int srcLength = src.length;
        byte[] dst = new byte[srcLength << 2];

        for (int i = 0; i < srcLength; i++) {
            int x = src[i];
            int j = i << 2;
            dst[j++] = (byte) ((x >> 0) & 0xff);
            dst[j++] = (byte) ((x >> 8) & 0xff);
            dst[j++] = (byte) ((x >> 16) & 0xff);
            dst[j++] = (byte) ((x >> 24) & 0xff);
        }
        //   if (dst[0]==0) {Log.d("int2byte", Integer.toString(src[0]));}
        return dst;
    }

    public byte[] getFlightDataByteArray() {
        return int2byte(FlightData);
    }

    public int[] getFlightDataIntArray() {
        return FlightData;
    }

    public void setFlightData(byte[] input) {
        FlightData = byte2int(input);
        updateFields();

    }

    private void linkValuesToArrayIndices() {

        FlightData[0] = pitch;
        FlightData[1] = roll;
        FlightData[2] = yaw;

        FlightData[3] = pitchCompensation;
        FlightData[4] = rollCompensation;

        FlightData[5] = altitude;

        FlightData[6] = batteryLevel;
        FlightData[7] = accZ;
    }


    public void updateFields() {

        pitch = FlightData[0];
        roll = FlightData[1];
        yaw = FlightData[2];

        pitchCompensation = FlightData[3];
        rollCompensation = FlightData[4];

        altitude = FlightData[5];

        batteryLevel = FlightData[6];
        accZ =FlightData[7];

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

    public Integer getPitchCompensation() {
        return pitchCompensation;
    }

    public void setPitchCompensation(int pitchCompensation) {
        this.pitchCompensation = pitchCompensation;
        linkValuesToArrayIndices();
    }

    public Integer getRollCompensation() {
        return rollCompensation;
    }

    public void setRollCompensation(int rollCompensation) {
        this.rollCompensation = rollCompensation;
        linkValuesToArrayIndices();
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
        linkValuesToArrayIndices();
    }

    public Integer getAltitude() {
        return altitude;
    }


    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
        linkValuesToArrayIndices();
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setVoltage(Integer voltage) {
        this.voltage = voltage;
        linkValuesToArrayIndices();
    }

    public Integer getAccZ() {
        return accZ;
    }


    public int getVoltage() {
        return voltage;
    }

    public void setAccZ(Integer accZ) {
        this.accZ = accZ;
        linkValuesToArrayIndices();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("{Pitch:").append(pitch)
                .append(" Roll=").append(roll)
                .append(", Yaw=").append(yaw)
                .append("}").toString();
    }

}
