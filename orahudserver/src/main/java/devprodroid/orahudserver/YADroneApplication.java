package devprodroid.orahudserver;

import android.annotation.SuppressLint;
import android.app.Application;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;

/**
 * Application context for keeping the drone instance
 */
public class YADroneApplication extends Application
{
    /**
     * The drone is kept in the application context so that all activities use the same drone instance
     */
    private IARDrone drone;

    @SuppressLint("MissingSuperCall")
    public void onCreate()
    {
       drone = new ARDrone("192.168.1.1", null); // null because of missing video support on Android
    }

    public IARDrone getARDrone()
    {
        return drone;
    }

}