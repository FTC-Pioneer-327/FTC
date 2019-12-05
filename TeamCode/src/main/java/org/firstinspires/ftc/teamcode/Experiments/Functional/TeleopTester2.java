package org.firstinspires.ftc.teamcode.Experiments.Functional;


import android.renderscript.Double2;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Helpers.bMath;
import org.firstinspires.ftc.teamcode.Robot.Robot;
import org.firstinspires.ftc.teamcode.Robot.RobotArm;
import org.firstinspires.ftc.teamcode.Robot.RobotWallTrack;

@TeleOp(name = "Teleop2", group = "Sensor")
public class TeleopTester2 extends LinearOpMode {

    RobotWallTrack.SensorGroup targetWallTrackGroup = null;

    Robot robot = new Robot();

    ElapsedTime deltaTime = new ElapsedTime();

    public boolean lockRotation;
    public boolean lastLockRotation;

    double targetRotation;

    double moveSpeed;
    double rotateSpeed;
    double targetRotationOffset;

    boolean grab = false;
    double extension = 0;
    double armAngle = 0;
    double wrist = 0.5;


    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap, this);
//        robot.armWintch.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        robot.armWintch.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();

        targetRotation = robot.GetRotation();

        while (opModeIsActive()) {

            //I need to "change something"

            ///DRIVER CONTROLS
            moveSpeed = bMath.Clamp(gamepad1.right_trigger + 0.35, 0, 1);
            rotateSpeed = bMath.Clamp(gamepad1.left_trigger + 0.35, 0, 1);

            targetRotation += gamepad1.left_stick_x;
//            targetRotation = bMath.Loop(targetRotation, 360);

            if (lockRotation) {
                robot.MoveComplex(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, robot.GetRotation() - (targetRotation));
            } else {
                robot.MoveSimple(new Double2(gamepad1.right_stick_x, gamepad1.right_stick_y), moveSpeed, gamepad1.left_stick_x * rotateSpeed);
            }


            if (gamepad1.a) {
                lockRotation = !lockRotation;
                sleep(350);
            }

            if (lastLockRotation != lockRotation) {
                lastLockRotation = lockRotation;
                if (lockRotation) {
                    targetRotation = robot.GetRotation();
                }
            }

            //ARM CONTROLS

            if(gamepad2.dpad_up){
                wrist+=0.1;
                sleep (100);
            }

            if(gamepad2.dpad_down){
                wrist -=0.1;
                sleep (100);
            }

            //press the B button to change if the grabber is open or closed
           if (gamepad2.b){
               if (!grab){
                   grab = true;
                   sleep (200);
               }
               else {
                   grab = false;
                   sleep (200);
               }
           }

            if (grab){
                robot.arm.SetGripState(RobotArm.GripState.CLOSED, wrist);
            }

            if (!grab){
                robot.arm.SetGripState(RobotArm.GripState.OPEN, wrist);
            }
            //press the B button to change if the grabber is open or closed


            //extend arm by tapping right trigger
            extension += gamepad2.right_trigger/10;
            //retract arm by tapping left trigger
            extension -= gamepad2.left_trigger/10;
            //rotate arm up and down with the left joystick
            armAngle -= gamepad2.left_stick_y/3;

            extension = bMath.Clamp(extension, 0, 1);
            armAngle = bMath.Clamp (armAngle, 0, 1);
            robot.arm.SetArmState(armAngle, extension, 1, 1);


            telemetry.addData("Rotation Locked ", lockRotation);
            telemetry.addData("", "");
            telemetry.addData("Current Rotation ", robot.GetRotation());
            telemetry.addData("Current Target Rotation", targetRotation);
            telemetry.update();

            deltaTime.reset();

        }
        robot.Stop();
    }
}