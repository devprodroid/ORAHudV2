package devprodroid.orahudserver.control;

import android.util.Log;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;


/**
 * This class controlls the movement of the drone with a thread
 * computing the movement from the roll and pitch values set externally for the calling class
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

    /**
     * Time to sleep between commands
     */
    private final int sleepDuration = 100;

    private Thread myThread;

    public DroneControl(IARDrone aDrone) {
        drone = aDrone;
        cmd = drone.getCommandManager();
        startThread();
    }

    /**
     * Start the Command Runner Thread
     */
    public void startThread() {
        running = true;
        myThread = new Thread(this);
        myThread.start();

    }

    /**
     * Stop the Command Runner thread
     */
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
    boolean performAction=false;
// (cmd.isConnected())
        while ((running)) {
            if (true) {


                //Pitch movement
                performAction=pitchUpDown();

                //Roll Movement
                performAction= performAction||rollLeftRight();

                //We sleep for 20ms for performance reasons


                //TODO: this is experimental and may lead to wired behavior of the drone, however we want to make sure that the drone hovers

                if (!performAction){
                    hover();
                }

                //spin left
                //spin right

                //up
                //down
                sleep(sleepDuration);



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


    private boolean pitchUpDown() {

        if (getPitch_angleNormed() > 20) {
            //back
            cmd.backward(getPitch_angleControl());
            Log.e("Command", "back: " + getPitch_angleControl());
            return true;
        } else if (getPitch_angleNormed() < -20) {
            //forward
            cmd.forward(getPitch_angleControl());
            Log.e("Command", "forward: " + getPitch_angleControl());
            return false;
        }
        return false;
    }

    private boolean rollLeftRight(){

        if (getRoll_angleNormed() > 20) {
            //right
            cmd.goRight(getRoll_angleControl());
            Log.e("Command", "right: " + getRoll_angleControl());
            return true;
        } else if (getRoll_angleNormed() < -20) {
            //left
            cmd.goLeft(getRoll_angleControl());
            Log.e("Command", "left: " + getRoll_angleControl());
            return true;

        }
        return false;
    }

    //let the thread sleep
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

    public float getPitch_angle() {
        return pitch_angle;
    }


    /**
     * @return The pitch angle multiplied by 10 and rounded to int
     */
    public int getPitch_angleNormed() {
        return Math.round(pitch_angle * 10);
    }

    /**
     * @return Pitch angle as int with controlMultiplier applied
     */
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

    /**
     * @return The roll angle multiplied by 10 and rounded to int
     */
    public int getRoll_angleNormed() {
        return Math.round(roll_angle * 10);
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


    /**
     * @return isFlying state of the drone
     */
    public boolean isFlying() {
        return isFlying;
    }

    public void setIsFlying(boolean isFlying) {
        this.isFlying = isFlying;
    }


    /**
     * takeoff and set isFlying true
     */
    public void takeoff() {
        if (!isFlying()) {
            drone.takeOff();
            setIsFlying(true);
        }

    }

    /**
     * land and set isFlying false
     */
    public void land() {
        if (isFlying()) {
            drone.landing();
            setIsFlying(false);
        }
    }

    /**
     * let the drone hover
     */
    public void hover() {
        drone.hover();
        Log.e(TAG, "Hover: ");
    }
}
