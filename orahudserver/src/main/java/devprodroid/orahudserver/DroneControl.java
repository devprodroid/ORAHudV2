package devprodroid.orahudserver;

import android.util.Log;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;

/**
 * Created by robert on 15.07.15.
 */
public class DroneControl implements Runnable {
    private boolean running = false;

    private IARDrone drone;
    private CommandManager cmd;

    private float pitch_angle;
    private float roll_angle;

    private boolean isConnected = true;

    private boolean isFlying = false;


    private final String TAG = getClass().getPackage().getName();
    private final int controlMultiplier = 2;

    private Thread myThread;

    public DroneControl(IARDrone aDrone) {
        drone = aDrone;
        cmd = drone.getCommandManager();
        startThread();
    }

    public void startThread() {
        running = true;
        myThread = new Thread(this);
        myThread.start();

    }

    public void stopThread() {
        running = false;
        land();
        boolean retry = true;
        while (retry) {
            try {
                myThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {

// (cmd.isConnected())
        while ((running)) {
            if (true) {


                if (getPitch_angleNormed() > 20) {
                    //back
                    cmd.backward(getPitch_angleControl());
                    Log.e("Command", "back: "+ getPitch_angleControl());
                } else if (getPitch_angleNormed()<-20){
                    //forward
                    cmd.forward(getPitch_angleControl());
                    Log.e("Command", "forward: "+ getPitch_angleControl());
                }

                if (getRoll_angleNormed() > 20) {
                    //right
                    cmd.goRight(getRoll_angleControl());
                    Log.e("Command", "right: "+ getRoll_angleControl());
                } else if (getRoll_angleNormed() < -20) {
                    //left
                    cmd.goLeft(getRoll_angleControl());
                    Log.e("Command", "left: "+ getRoll_angleControl());

                }

                //We sleep for 20ms for performance reasons
                sleep(20);

                //spin left
                //spin right

                //up
                //down


                //if the Controller is not connected, the Drone needs to land, so that nothing
                //unexpected happens
            } else {
                if (isFlying()) {
                    cmd.landing();
                    setIsFlying(false);
                    sleep(200);
                }
            }
        }
    }


    //let sleep
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

    public float getPitch_angle() {
        return pitch_angle;
    }

    public int getPitch_angleNormed() {
        return Math.round(pitch_angle*10);
    }
    public int getPitch_angleControl() {
        return Math.abs(Math.round(pitch_angle * controlMultiplier));
    }



    public void setPitch_angle(float pitch_angle) {
        this.pitch_angle = pitch_angle;
    }
    /**
     * @return Pitch angle as int with controlMultiplier applied
     */
    public float getRoll_angle() {
        return roll_angle;
    }

    public int getRoll_angleNormed() {
        return Math.round(roll_angle*10);
    }


    /**
     * @return Roll angle as int with controlMultiplier applied
     */
    public int getRoll_angleControl() {
        return Math.abs(Math.round(roll_angle * controlMultiplier));
    }

    public void setRoll_angle(float roll_angle) {
        this.roll_angle = roll_angle;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setIsFlying(boolean isFlying) {
        this.isFlying = isFlying;
    }


    public void takeoff() {
        if (!isFlying()) {
            drone.takeOff();
            setIsFlying(true);
        }

    }

    public void land() {
        if (isFlying()) {
            drone.landing();
            setIsFlying(false);
        }
    }

    public void hover() {
        drone.hover();
        Log.e(TAG, "Hover: ");
    }
}
