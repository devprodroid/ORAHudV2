package devprodroid.orahudserver.control;

import android.util.Log;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;


/**
 * This class controlls the movement of the drone with a thread
 * computing the movement from the roll and pitch values set externally for the calling class
 */
public class DroneControl implements Runnable {
    private static Boolean stop = false;

    private IARDrone drone;
    private CommandManager cmd;

    private float pitch_angle;
    private float roll_angle;


    private static boolean  isFlying = false;


    private final String TAG = getClass().getPackage().getName();
    private final int controlMultiplier = 2;

    /**
     * Time to sleep between commands
     */
    private final int sleepDuration = 100;

    private Thread myThread;
    private static boolean translateMode;
    private static boolean rotateMode;
    private static boolean controlActive;


    private static boolean goUpDemand;
    private static boolean goDownDemand;



    //Settings
    private boolean mIndoorMode;


    public DroneControl(IARDrone aDrone, boolean outdoorMode) {
        drone = aDrone;
        drone.start();
        cmd = drone.getCommandManager();
      //  drone.getCommandManager().setNavDataDemo(false);


        //cmd.setOutdoor(outdoorMode, outdoorMode);
        drone.start();


        startThread();
        cmd.setNavDataDemo(false);
    }

    /**
     * Start the Command Runner Thread
     */
    public void startThread() { setStop(false);
        myThread = new Thread(this);
        myThread.start();


    }

    /**
     * Stop the Command Runner thread
     */
    public void stopThread() {

        land();
        Log.d("Drone", "Landing");
        setIsFlying(false);

        setStop(true);
    }





    public Boolean getStop() {
        return stop;
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {

        //if no action is performed, the drone shall hover
        boolean performAction;

        while (!this.getStop()) {
            if (cmd.isConnected()) {
                performAction = false;
                if (isFlying()) {

                   // move(int speedX, int speedY, int speedZ, int speedSpin)
                    if (isTiltControlActive()) {
                        if (isTranslateMode()){
                            cmd.move(-getPitch_angleNormed(), -getRoll_angleNormed(), 0, 0);
                            performAction=true;}
                        else if (isRotateMode()){
                            cmd.move(-getPitch_angleNormed(), 0, 0, -getRoll_angleNormed());
                            performAction=true;}
                    }

                    performAction = performAction || goUpDown();
                    //We sleep for 20ms for performance reasons

                    if (!performAction) hover();
                }
                sleep(sleepDuration);
            } else {
                //if the Controller is not connected, the Drone needs to land, so that nothing
                //unexpected happens
                if (isFlying()) {
                    cmd.landing();
                    Log.d("DebugLandelse", "DebugLand");
                    setIsFlying(false);
                    sleep(sleepDuration);
                }
            }
        }
    }


    private boolean pitchUpDown() {

        if (getPitch_angleNormed() > 20) {
            //back
            cmd.backward(getPitch_angleControl());
            Log.e("Command", "backward: " + getPitch_angleControl());
            return true;
        } else if (getPitch_angleNormed() < -20) {
            //forward
            cmd.forward(getPitch_angleControl());
            Log.e("Command", "forward: " + getPitch_angleControl());
            return true;
        }
        return false;
    }

    private boolean goUpDown() {

        if (isGoUpDemand()) {
            cmd.up(40);
            return true;
        } else if (isGoDownDemand()) {

            cmd.down(40);
            return true;
        }

        return false;
    }


    private boolean rollLeftRight() {

        if (getRoll_angleNormed() > 20) {
            //right
            cmd.goRight(getRoll_angleControl());
            Log.e("Command", "goRight: " + getRoll_angleControl());
            return true;
        } else if (getRoll_angleNormed() < -20) {
            //left
            cmd.goLeft(getRoll_angleControl());
            Log.e("Command", "goLeft: " + getRoll_angleControl());
            return true;
        }
        return false;
    }

    private boolean spinLeftRight() {

        if (getRoll_angleNormed() > 20) {
            //right
            cmd.spinRight(getRoll_angleControl() * 5);
            Log.e("Command", "spin right: " + getRoll_angleControl());
            return true;
        } else if (getRoll_angleNormed() < -20) {
            //left
            cmd.spinLeft(getRoll_angleControl() * 5);
            Log.e("Command", "spin left: " + getRoll_angleControl());
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
        if (Math.abs(pitch_angle)>=0.05)
            return Math.round(pitch_angle * 10);
        else return 0;
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
        if (Math.abs(roll_angle)>=0.05)
            return Math.round(roll_angle * 10);
        else return 0;
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
            cmd.hover();
            setIsFlying(true);


        }

    }

    /**
     * land and set isFlying false
     */
    public void land() {
        //if (isFlying()) {
            drone.landing();
            setIsFlying(false);
            Log.d("DebugLand", "DebugLand");
        //}
    }

    /**
     * let the drone hover
     */
    public void hover() {
        drone.hover();
        Log.d(TAG, "Hover");


    }

    //translate
    public boolean isTranslateMode() {
        return this.translateMode;
    }

    public void setTranslateMode() {
        this.translateMode = true;
        this.rotateMode = false;

    }

    public boolean isRotateMode() {
        return rotateMode;
    }


    //Rotation and up down movement
    public void setRotateMode() {
        this.rotateMode = true;
        this.translateMode = false;
    }

    public boolean isTiltControlActive() {
       // Log.d(TAG, "isTiltControlActive " + Boolean.toString(this.controlActive));
        return this.controlActive;

    }

    //Rotation and up down movement
    public void setControlActive(boolean active) {
        this.controlActive = active;
    }


    public boolean isGoUpDemand() {
        return goUpDemand;
    }

    public void setGoUpDemand(boolean goUpDemand) {
        this.goUpDemand = goUpDemand;
    }

    public boolean isGoDownDemand() {
        return goDownDemand;
    }

    public void setGoDownDemand(boolean goDownDemand) {
        this.goDownDemand = goDownDemand;
    }

    public boolean ismIndoorMode() {


        return mIndoorMode;
    }

    public void setmIndoorMode(boolean mIndoorMode) {
        this.mIndoorMode = mIndoorMode;
    }
}
