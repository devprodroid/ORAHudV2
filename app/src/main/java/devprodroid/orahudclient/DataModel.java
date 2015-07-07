package devprodroid.orahudclient;

/**
 * Created by robert on 15.06.15.
 */
public class DataModel  {

    boolean attitude =false;

    public void setAttitude(boolean attitude) {
        this.attitude = attitude;
    }


    /**
     * Array containing the flightdata
     * Spec:
     * [0] pitch
     * [1] roll
     * [2] yaw
     *
     */
    private int[] FlightData;

    public DataModel(){
        FlightData = new int[64];
    }



}
