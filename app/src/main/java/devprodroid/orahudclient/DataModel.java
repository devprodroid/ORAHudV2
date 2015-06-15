package devprodroid.orahudclient;

import java.util.Observable;

/**
 * Created by robert on 15.06.15.
 */
public class DataModel extends Observable {

    boolean attitude =false;

    public void setAttitude(boolean attitude) {
        this.attitude = attitude;
    }

    public boolean isAttitude() {
        return attitude;
    }

    public void changeSomething() {
        // Notify observers of change
        setChanged();
        notifyObservers();
    }
}
