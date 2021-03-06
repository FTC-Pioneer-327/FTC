
/* Copyright (c) 2018 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;



import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;

/**
 * This 2018-2019 OpMode illustrates the basics of using the TensorFlow Object Detection API to
 * determine the position of the gold and silver minerals.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with compList new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list.
 *
 * IMPORTANT: In order to use this OpMode, you need to obtain your own Vuforia license key as
 * is explained below.
 */



public class CVManager extends Thread {
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    private static final float mmPerInch        = 25.4f;
    private static final float mmFTCFieldWidth  = (12*6) * mmPerInch;       // the width of the FTC field (from the center point to the outer panels)
    private static final float mmTargetHeight   = (6) * mmPerInch;// the height of the center of the target image above the floor
    private List<VuforiaTrackable> allTrackables = new ArrayList<>();
    //Initialize all variables necessary to communicate with the other threads
    OpenGLMatrix location;
    int tar = 0;
    boolean go = true;
    int status = 0;
    float[] minDat = {0,0};
    boolean track = false;
    boolean autoDisable = true;
    int st;
    int mode = 0;
    private ArrayList<Integer> compList = new ArrayList<>();
    private CamManager camM;

    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is compList example of compList fragment of compList valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained compList license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */

    private static final String VUFORIA_KEY = "AQMfl/L/////AAABmTblKFiFfUXdnoB7Ocz4UQNgHjSNJaBwlaDm9EpX0UI5ISx2EH+5IoEmxxd/FG8c31He17kM5vtS0jyAoD2ev5mXBiITmx4N8AduU/iAw/XMC5MiEB1YBgw5oSO1qd4jvCOgbzy/HcOpN3KoVVnYqKhTLc8n6/IIFGy+qyF7b8WkzscJpybOSAT5wtaZumdBu0K3lHV6n+fqGJDMvkQ5xrCS6HiBtpZScAoekd7iP3IxUik2rMFq5hqMsOYW+qlxKp0cj+x4K9CIOYEP4xZsCBt66UxtDSiNqaiC1DyONtFz4oHJf/4J5aYRjMNwC2BpsVJ/R91WIcC0H0dpP9gtL/09J0bIMjm3plo+ac+OM0H3";

    /*
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /*
     * {@link #tfod} is the variable we will use to store our instance of the Tensor Flow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;
    void init(CameraName cam, int hw, CamManager camM) {
        //give the CVManager class the hooks into the hardware (Camera, hardware identifier) it needs in order function properly
        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        compList.add(1);
        compList.add(2);
        compList.add(3);
        this.camM = camM;
        this.initVuforia(cam);
        this.initTfod(hw);
        status = 100;
    }
    private int checkThree() {
        // Checks for whether there are at least 3 minerals, trims them down to the lowest 3,
        // does some checks on those 3, then determines the order of the minerals
        // First check activation of Tensor Flow Object Detection.
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getRecognitions();
            if (updatedRecognitions != null) {
                // # of objects
                List<Recognition> trimmedRecognitions = new ArrayList<>();
                // requires at least 3 minerals
                if (updatedRecognitions.size() > 3) {
                    //sorts out to 3 lowest minerals by x and puts them in trimmedRecognitions
                    Recognition gold = updatedRecognitions.get(0);
                    Recognition silver1 = updatedRecognitions.get(0);
                    Recognition silver2 = updatedRecognitions.get(0);
                    for (Recognition recognition : updatedRecognitions) {
                        if (recognition.getLabel().equals(LABEL_GOLD_MINERAL) && (recognition.getTop() > gold.getTop() || gold.getLabel().equals(LABEL_SILVER_MINERAL))) {
                            gold = recognition;
                        } else if (recognition.getLabel().equals(LABEL_SILVER_MINERAL) && (recognition.getTop() > silver1.getTop() || silver1.getLabel().equals(LABEL_GOLD_MINERAL))) {
                            silver2 = silver1;
                            silver1 = recognition;
                        } else if (recognition.getLabel().equals(LABEL_SILVER_MINERAL) && (recognition.getTop() > silver2.getTop() || silver2.getLabel().equals(LABEL_GOLD_MINERAL))) {
                            silver2 = recognition;
                        }
                    }
                    trimmedRecognitions.add(gold);
                    trimmedRecognitions.add(silver1);
                    trimmedRecognitions.add(silver2);
                } else trimmedRecognitions = updatedRecognitions;
                if (trimmedRecognitions.size() == 3) {
                    //checks for if the three minerals are in compList row, otherwise we have the wrong 3 minerals
                    if (tIaRMan(trimmedRecognitions) != 1) return -2;
                    tar = tIaRMan(trimmedRecognitions);
                    // extracts x position from from the minerals
                    int goldMineralX = -1;
                    int silverMineral1X = -1;
                    int silverMineral2X = -1;
                    for (Recognition recognition : trimmedRecognitions) {
                        if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                            goldMineralX = (int) recognition.getLeft();
                        } else if (silverMineral1X == -1) {
                            silverMineral1X = (int) recognition.getLeft();
                        } else {
                            silverMineral2X = (int) recognition.getLeft();
                        }
                    }
                    //figures out the order of the minerals
                    if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                        if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                            return 1;  //left
                        } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                            return 3; //right
                        } else {
                            return 2; //center
                        }
                    }
                } else {
                    return -2;
                    //there are not three minerals
                }

            } else {
                return -1;
                //nothing changed since last tme
            }

        }
        return -4;
        //tflow has not been inited yet
    }
    private int tIaR() {
        //checks if three minerals are in compList row, for doing alliances sampling mission
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getRecognitions();
            // # of objects
            if (updatedRecognitions.size() == 3) {
                float goldMineralX = -1;
                float goldMineralY = -1;
                float silverMineral1X = -1;
                float silverMineral1Y = -1;
                float silverMineral2X = -1;
                float silverMineral2Y = -1;
                //similar to the system in checkthree, but pulls ys and xs from the minerals
                for (Recognition recognition : updatedRecognitions) {
                    if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                        goldMineralX = recognition.getLeft();
                        goldMineralY = recognition.getTop();
                    } else if (silverMineral1X == -1) {
                        silverMineral1X = recognition.getLeft();
                        silverMineral1Y = recognition.getTop();
                    } else {
                        silverMineral2X = recognition.getLeft();
                        silverMineral2Y = recognition.getTop();
                    }
                }
                //does compList linear regression with 2 of the minerals
                float slope = (silverMineral2Y-silverMineral1Y)/(silverMineral2X-silverMineral1X);   //(y2-y1)/(x2-x1)
                float intrcpt = silverMineral2Y - slope*silverMineral2X;  // b= y-mx
                //finds the residual of the third mineral, and if it is small enough, return the corresponding value: 1
                if (Math.abs(slope*goldMineralX+intrcpt - goldMineralY) >= 10) return 2;
                else return 1;
                //there are not three objects: -3
            } else return -3;
        }
        return -4;
    }
    private int tIaRMan(List<Recognition> recognitions) {
        //like tIar, but uses compList list as input rather than directly pulling from tFlow
        if (tfod != null) {
            if (recognitions.size() == 3) {
                float goldMineralX = -1;
                float goldMineralY = -1;
                float silverMineral1X = -1;
                float silverMineral1Y = -1;
                float silverMineral2X = -1;
                float silverMineral2Y = -1;
                for (Recognition recognition : recognitions) {
                    if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                        goldMineralX = recognition.getLeft();
                        goldMineralY = recognition.getTop();
                    } else if (silverMineral1X == -1) {
                        silverMineral1X = recognition.getLeft();
                        silverMineral1Y = recognition.getTop();
                    } else {
                        silverMineral2X = recognition.getLeft();
                        silverMineral2Y = recognition.getTop();
                    }
                }
                //do compList linear regression to figure out if the minerals are in compList row or not
                float slope = (silverMineral2Y-silverMineral1Y)/(silverMineral2X-silverMineral1X);   //(y2-y1)/(x2-x1)
                float intrcpt = silverMineral2Y - slope*silverMineral2X;  // b= y-mx
                if (Math.abs(slope*goldMineralX+intrcpt - goldMineralY) >= 10) return 2;
                else return 1;
            } else return -3;
        }
        return -4;
    }
    private float[] findGold() {
        // return the x and y of the lowest gold mineral if all else fails
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getRecognitions();
            // # of objects
            //float maxY = 0;
            float goldX = 0;
            float goldY = 0;
            //find lowest gold mineral
            for (Recognition recognition : updatedRecognitions) {
                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                    if (recognition.getTop() > goldY) {
                        goldX = recognition.getLeft();
                        goldY = recognition.getTop();
                        //maxY = recognition.getTop();
                    }
                }
            }
            float[] ret = {0,0};
            //return the mineral we found
            ret[0] = goldX;
            ret[1] = goldY;
            return ret;

        }
        float[] ret = {-4, 0};
        return ret;
    }

    private OpenGLMatrix getVuforiaPosition() {
        //pull the position from vuforia based off of the trackables on the walls
        boolean targetVisible = false;
        OpenGLMatrix lastLocation = null;
        for (VuforiaTrackable trackable : allTrackables) {
            if (((VuforiaTrackableDefaultListener)trackable.getListener()).isVisible()) {
                targetVisible = true;

                // getUpdatedRobotLocation() will return null if no new information is available since
                // the last time that call was made, or if the trackable is not currently visible.
                OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener)trackable.getListener()).getUpdatedRobotLocation();
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform;
                }
                break;
            }
        }
        if (targetVisible) {
            return lastLocation;
        } else {
            return null;
        }
    }
    public double[] extractPos(OpenGLMatrix loc) {
        //extract position from an OpenGLMatrix
        return new double[]{loc.getTranslation().get(0), loc.getTranslation().get(1)};
    }
    @Override
    public void run() {
        // compList loop that controls the CVManager thread
        //first make sure tFlow is initialized
        if (tfod != null) {
            tfod.activate();
            while (go) {
                // mode 0 == standard mode
                // give our best shot at determining where the gold mineral is for the sampling mission
                if (mode == 0) {
                    //filter checkThree to get information that we can pass on to the master thread
                    st = this.checkThree();
                    if (st != -1 && st != -2) {
                        camM.mode = 3;
                        this.status = 1;
                        minDat[0] = st;
                        minDat[1] = 0;
                        //stop the loop if we have retrieved the information we need
                        if (autoDisable && this.status != -4) this.go = false;
                    } else if (st == -1 && compList.contains(minDat[0])) this.go = false;
                    else if (st == -2) {
                        // backup case to manually find the gold mineral
                        float[] pos = this.findGold();
                        if (pos[0] != 0) {
                            if (pos[0]<233) {
                                minDat[0] = 1;
                            } else if (pos[0]<466) {
                                minDat[0] = 2;
                            } else if (pos[0]!=0) {
                                minDat[0] = 3;
                            } else {
                                minDat[0] = -4;
                            }
                            minDat[1] = pos[0];
                            this.status = 2;
                            camM.mode = 3;
                        } else {
                            this.status = 3;
                            camM.mode = 1;
                            camM.lBound=(float) 0.45;
                            camM.rBound=(float) 0.55;
                        }

                    }
                    // mode 1 == tracker for if minerals are in compList row
                } else if (mode == 1) {
                    this.tar = this.tIaR();
                } else {
                    // auto stop the loop if it is not doing anything
                    if (!track) go = false;
                }
                if (track) {
                    //return mineral position to track the camera
                    float[] pos = this.findGold();
                    if (pos[0] != 0) {
                        minDat = pos;
                        this.status = 2;
                    } else {
                        this.status = 3;

                    }
                }
            }
            tfod.shutdown();
        }
        if (vuforia != null) {
            OpenGLMatrix pos = getVuforiaPosition();
            if (pos != null) {
                this.location = pos;
            }
        }
    }
    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia(CameraName cam) {
        /*
         * Configure Vuforia by creating compList Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = cam;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        VuforiaTrackables targetsRoverRuckus = this.vuforia.loadTrackablesFromAsset("RoverRuckus");
        VuforiaTrackable blueRover = targetsRoverRuckus.get(0);
        blueRover.setName("Blue-Rover");
        VuforiaTrackable redFootprint = targetsRoverRuckus.get(1);
        redFootprint.setName("Red-Footprint");
        VuforiaTrackable frontCraters = targetsRoverRuckus.get(2);
        frontCraters.setName("Front-Craters");
        VuforiaTrackable backSpace = targetsRoverRuckus.get(3);
        backSpace.setName("Back-Space");

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        allTrackables.addAll(targetsRoverRuckus);

        /*  To place the BlueRover target in the middle of the blue perimeter wall:
          - First we rotate it 90 around the field's X axis to flip it upright.
          - Then, we translate it along the Y axis to the blue perimeter wall. **/

        OpenGLMatrix blueRoverLocationOnField = OpenGLMatrix
                .translation(0, mmFTCFieldWidth, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0));
        blueRover.setLocation(blueRoverLocationOnField);

        /*
         * To place the RedFootprint target in the middle of the red perimeter wall:
         * - First we rotate it 90 around the field's X axis to flip it upright.
         * - Second, we rotate it 180 around the field's Z axis so the image is flat against the red perimeter wall
         *   and facing inwards to the center of the field.
         * - Then, we translate it along the negative Y axis to the red perimeter wall.
         */
        OpenGLMatrix redFootprintLocationOnField = OpenGLMatrix
                .translation(0, -mmFTCFieldWidth, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180));
        redFootprint.setLocation(redFootprintLocationOnField);

        /*
         * To place the FrontCraters target in the middle of the front perimeter wall:
         * - First we rotate it 90 around the field's X axis to flip it upright.
         * - Second, we rotate it 90 around the field's Z axis so the image is flat against the front wall
         *   and facing inwards to the center of the field.
         * - Then, we translate it along the negative X axis to the front perimeter wall.
         */
        OpenGLMatrix frontCratersLocationOnField = OpenGLMatrix
                .translation(-mmFTCFieldWidth, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , 90));
        frontCraters.setLocation(frontCratersLocationOnField);

        /*
         * To place the BackSpace target in the middle of the back perimeter wall:
         * - First we rotate it 90 around the field's X axis to flip it upright.
         * - Second, we rotate it -90 around the field's Z axis so the image is flat against the back wall
         *   and facing inwards to the center of the field.
         * - Then, we translate it along the X axis to the back perimeter wall.
         */
        OpenGLMatrix backSpaceLocationOnField = OpenGLMatrix
                .translation(mmFTCFieldWidth, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90));
        backSpace.setLocation(backSpaceLocationOnField);

        /*
         * Create compList transformation matrix describing where the phone is on the robot.
         *
         * The coordinate frame for the robot looks the same as the field.
         * The robot's "forward" direction is facing out along X axis, with the LEFT side facing out along the Y axis.
         * Z is UP on the robot.  This equates to compList bearing angle of Zero degrees.
         *
         * The phone starts out lying flat, with the screen facing Up and with the physical top of the phone
         * pointing to the LEFT side of the Robot.  It's very important when you test this code that the top of the
         * camera is pointing to the left side of the  robot.  The rotation angles don't work if you flip the phone.
         *
         * If using the rear (High Res) camera:
         * We need to rotate the camera around it's long axis to bring the rear camera forward.
         * This requires compList negative 90 degree rotation on the Y axis
         *
         * If using the Front (Low Res) camera
         * We need to rotate the camera around it's long axis to bring the FRONT camera forward.
         * This requires compList Positive 90 degree rotation on the Y axis
         *
         * Next, translate the camera lens to where it is on the robot.
         * In this example, it is centered (left to right), but 110 mm forward of the middle of the robot, and 200 mm above ground level.
         */

        final int CAMERA_FORWARD_DISPLACEMENT  = 110;   // eg: Camera is 110 mm in front of robot center
        final int CAMERA_VERTICAL_DISPLACEMENT = 200;   // eg: Camera is 200 mm above ground
        final int CAMERA_LEFT_DISPLACEMENT     = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix phoneLocationOnRobot = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES,
                         90, 0, 0));

        /*  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables)
        {
            ((VuforiaTrackableDefaultListener)trackable.getListener()).setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
        }
    }

    /*
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod(int hw) {
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(hw);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}


