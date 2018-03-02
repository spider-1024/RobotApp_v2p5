/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp_v2p5;

//import com.pi4j.io.gpio.GpioController;
//import com.pi4j.io.gpio.GpioFactory;
//import com.pi4j.io.gpio.GpioPinDigitalOutput;
//import com.pi4j.io.gpio.PinState;
//import com.pi4j.io.gpio.RaspiPin;
//import com.pi4j.io.serial.*;

import java.io.IOException;
import com.RobotMsgv2p0.*;
import static java.lang.Math.abs;

/**
 *
 * @author owner
 */
public class RobotControl {
//    static Serial serial;
//    static GpioController gpio;
//    static GpioPinDigitalOutput rxTxReferencePin;
//    static GpioPinDigitalOutput arduinoLED;
//    static GpioPinDigitalOutput driverStationLED;
//    static GpioPinDigitalOutput operationalStateLED;
//    static GpioPinDigitalOutput extraLED;
    
    static final int MAX_STEERING_ANGLE = 523;
    static final int MIN_STEERING_ANGLE = 245;
    static final int ZERO_STEERING_ANGLE = 389;
    static final int BUFF_STEERING_ANGLE = 20;   // hysteresis amount
    
    static final int P_GAIN = 10;  // proportional gain
    static final int I_GAIN = 10;  // integral gain
    
    // reflects the state of the serial comms to Arduino
    private boolean robotControlStatus;
    
    private int [] analogValues;
    
    // Scan control variables
    private int scanAngle;
    private int robotSteeringValue;
    private int steeringIntegrate;
    private int previousSteeringError;
    private int previousSteeringSpeed;
    private double steeringPGain;  // conversion factor to steering units
    private double steeringNGain;  // conversion factor to steering units 
    
    int testVal;
    private int initialTimeStamp = 0;
    
    /**
     * class : robotMotorInfo
     * 
     * This class defines the specific control elements of the robot that are
     * needed to support the drive requests.
     * This class reflects the motors, sensors that are available on the 
     * actual robot.
     * 
     * motor1 - Forward Right and Rear Left
     * motor2 - Forward Left and Rear Right
     */
    public class robotMotorInfo {
        int motor1Speed;
        char motor1Direction;
        int motor2Speed;
        char motor2Direction;
        int distSensorMotorAngle;
        int steeringMotorSpeed;
        char steeringMotorDirection;
        int targetSteeringValue;    
    }
    
    
    // global to update Motor control values.
    robotMotorInfo robotMotors;
    
    
    /**
     * Constructor for RobotControl Class
     */
    public RobotControl() {
        
        //create an instance of the serial communication class
//        serial = SerialFactory.createInstance();
//        gpio = GpioFactory.getInstance();
        
        // setup LED pins
//        arduinoLED          = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW);
//        driverStationLED    = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, PinState.LOW);
//        operationalStateLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, PinState.LOW);
 
        // setup analog value storage
        analogValues = new int[7];
        analogValues[3] = ZERO_STEERING_ANGLE;  // good idea to initialize this one
        
        
        // misc
        robotMotors = new robotMotorInfo();
        robotControlStatus = false;        
        scanAngle = 0;        
        testVal = 0;
        
        // steering control
        previousSteeringError = 0;
        previousSteeringSpeed = 0;
        steeringIntegrate = 0;
        steeringPGain = (MAX_STEERING_ANGLE - ZERO_STEERING_ANGLE)/(99.0-50.0);
        steeringNGain = (ZERO_STEERING_ANGLE - MIN_STEERING_ANGLE)/(50.0-0.0);
    }
    
    /**
     * This method is called to execute any initialization required
     * for this method
     */
    public void robotControlInitialize() throws IOException {
 
        System.out.println("Initializing Serial");
 
/*
        // create and regiser serial data listner
        serial.addListener(new SerialDataEventListener () {
        @Override
        public void dataReceived(SerialDataEvent event) {
               
               try {
                   processArduinoUpdates(event.getAsciiString());
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
         });
        
  
        // setup Serial object
        SerialConfig config = new SerialConfig();
        
        // Need to set Pin 4 High for voltage converter
        rxTxReferencePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.HIGH);
        
        // set default serial settings (device, baud rate, flow control, etc)
        //
        // by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
        // NOTE: this utility method will determine the default serial port for the
        //       detected platform and board/model.  For all Raspberry Pi models
        //       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
        //       model 3B is will return "/dev/ttyS0".
        // config.device("/dev/ttyUSB0")
        config.device("/dev/ttyS0")
            .baud(Baud._38400)
            .dataBits(DataBits._8)
            .parity(Parity.NONE)
            .stopBits(StopBits._1)
            .flowControl(FlowControl.NONE);

                       
        // open the default serial device/port with the configuration settings
        serial.open(config);
            
        // pause to allow arduino to get started
        try {
            Thread.sleep(2000);
        }  catch (InterruptedException e){}
       
  */      
        // if we made it this far we must be good to go
        robotControlStatus = true;
    }
    
    /** 
     * This method processes the serial information from the Arduino
     */
    
    private void processArduinoUpdates(String inputString) {
      
    String ardGreet = "";
              
//    System.out.println("string = " + inputString.length() + "  " + inputString);

//    RobotSupport.fileWriteTypeA(inputString);
  

     // if we dont have at least 21 chars, something is wrong.
     if (!((inputString.length() == 32) || (inputString.length() == 38)))
         return;

      for (int i=0; i<inputString.length(); i++) {
          
          // extract analog data
          if (inputString.charAt(i) == 'S'){
              
              char cIndex = inputString.charAt(i+1);
              int index = cIndex - '0';
              
              
              // if index value did not match, something is wrong
              if (index <0 || index > 5)
                  continue;
                 
              try {
                 int val = Integer.parseInt((inputString.substring(i+2,i+5)));        
              
                 setAnalogValue(index, val);
                 i = i+4;
                 
              
              } catch (IndexOutOfBoundsException ex) { 
                  System.out.println("Parse error at " + i + " on " + inputString);
              }
              
              
          }
          
          if (inputString.charAt(i) == 'H') {
              ardGreet = inputString.substring(i,i+5);
          }
      }
   
       // check on Arduino Status
       if (ardGreet.equals("HiPi?"))
           setArduinoLED(true);

    }
    
    // This method will set the white LED to indicate the operational State
    public void setOperationalStatusLED(boolean state) {
//       operationalStateLED.setState(state);
    }
    
    // This method will set the second green LED to indicated state of 
    // communication with the Driver Station
    public void setDriverStationLED(boolean state) {
//        driverStationLED.setState(state);
    }
    
    // This method will set the state of communication with the Arduino
    public void setArduinoLED(boolean state){
//        arduinoLED.setState(state);
    }
    
    // This method is used to store the analog values after reading from 
    // arduino
    private synchronized void setAnalogValue(int index, int val) {
       analogValues[index] = val;
    }
    
    // This returns the current analog value
    public synchronized int getAnalogValue(int index) {
            return analogValues[index];
    }
    
    // This returns the current distance value
    public synchronized int getDistTraveledValue() {
        return analogValues[4];
    }
    
    /**
     * This method returns the state of the serial comms to Arduino
     * @return 
     */
    public boolean getRobotControlArduinoStatus() {
        return robotControlStatus;
    }
    
    /**
     * This method sets the scanAngle
     */
    public void setScanAngle(int val) {
        scanAngle = val;
    }
   
     
    /**
     * 
     * Method : sendControlDataToArduino
     * This method takes the information in motor control, creates the string
     * for the arduino and then sends that to the arduino
     * 
     * 
     * @param : none 
     */ 
     
    private void sendControlDataToArduino() {
        

       String controlString = new String();
         
       // update Motors for left side
       controlString = String.format("M1%03dD1%c", robotMotors.motor1Speed, robotMotors.motor1Direction);
       
       // update Motors for right side
       controlString = controlString + String.format("M2%03dD2%c", robotMotors.motor2Speed, robotMotors.motor2Direction);
        
       // update Distance rotation motor
       controlString = controlString + String.format("M3%03dD3%c", robotMotors.distSensorMotorAngle, 'F');
        
       // update Steering motor
       controlString = controlString + String.format("M4%03dD4%cS1%03d?", 
               robotMotors.steeringMotorSpeed, robotMotors.steeringMotorDirection,robotMotors.targetSteeringValue);
        
//        if ((testVal % 50) == 0)
//            System.out.println("cs = " + controlString);
        
    
/*
        try {
            serial.write(controlString);
        } catch (IOException ex) {
            System.out.println("ard write error : " + ex);
             
        }
 */

    }
    
    /** 
     * 
     * Method : robotControlApplyTurnAssist
     * 
     * The steering motor does not have enough power to turn the robot on its own.
     * This method adjusts the drive motors to help turn the robot.
     * Because the robot motors are setup in a cross diagonal pattern, by applying
     * forward motion on one relative to the other, it forces the turn.
     */
    
    private void robotControlApplyTurnAssist() {
      
        // For now, only apply if we are not moving.
        if ((robotMotors.steeringMotorSpeed != 0) && (robotMotors.motor1Speed < 100)) {
            
            // for now we will apply have speed to each X-side
            if (robotMotors.steeringMotorDirection == 'F') {
                robotMotors.motor1Speed = 200;
                robotMotors.motor1Direction = 'R';
                robotMotors.motor2Speed = 200;
                robotMotors.motor2Direction = 'F';
            }
            
            if (robotMotors.steeringMotorDirection == 'R') {
                robotMotors.motor1Speed = 200;
                robotMotors.motor1Direction = 'F';
                robotMotors.motor2Speed = 200;
                robotMotors.motor2Direction = 'R';
            }
            
        }
        
    }  // end robotControlApplyTurnAssist
    
        private void robotControlApplyTurnAssist2() {
      
        // For now, only apply if we are not moving.
        if ((robotMotors.steeringMotorSpeed != 0) && (robotMotors.motor1Speed < 100)) {
            
            // for now we will apply have speed to each X-side
            if (robotMotors.steeringMotorDirection == 'F') {
                robotMotors.motor1Speed = robotMotors.steeringMotorSpeed;
                robotMotors.motor1Direction = 'R';
                robotMotors.motor2Speed = robotMotors.steeringMotorSpeed;
                robotMotors.motor2Direction = 'F';
            }
            
            if (robotMotors.steeringMotorDirection == 'R') {
                robotMotors.motor1Speed = robotMotors.steeringMotorSpeed;
                robotMotors.motor1Direction = 'F';
                robotMotors.motor2Speed = robotMotors.steeringMotorSpeed;
                robotMotors.motor2Direction = 'R';
            }
            
        }
        
    }  // end robotControlApplyTurnAssist
    
    /**
     * 
     * Method : processMotorControl
     * 
     * This method is responsible for determining the motor control values to apply
     * to the motors
     * 
     * 
     * This method is called every time from the main processing loop
     */
    public void processMotorControl(RobotControlMsgGen cntrlMsg) {
        int targetSteeringAngle;
        int currentSteeringAngle, currentSteeringSpeed;
        int error;
        String str = new String();
                
            
        // check for forward direction.  Motors will not turn unless enough initial speed
        if (cntrlMsg.driveSpeed > 100) {
             robotMotors.motor1Speed = cntrlMsg.driveSpeed;
             robotMotors.motor2Speed = cntrlMsg.driveSpeed;
        }
        else {
            robotMotors.motor1Speed = 0;
            robotMotors.motor2Speed = 0;
        }
        
        if (cntrlMsg.driveDirection == 'R') {
            robotMotors.motor1Direction = 'R';
            robotMotors.motor2Direction = 'R';
        }
        else {
            robotMotors.motor1Direction = 'F';
            robotMotors.motor2Direction = 'F';
        }
        
        
        // check for turning
        // Turning will come on with values between 0 and 100 (50 = center)
        // Need to map to the following angle values from steering sensor
        // minSteerinAngle --- zeroSteeringAngle --- maxSteeringAngle      
        
        if (cntrlMsg.steeringValue > 51) 
            targetSteeringAngle = (int) ((steeringPGain) * (cntrlMsg.steeringValue - 50)) + ZERO_STEERING_ANGLE;
        else if (cntrlMsg.steeringValue < 50) 
            targetSteeringAngle = (int) ((steeringNGain) * (cntrlMsg.steeringValue)) + MIN_STEERING_ANGLE;
        else 
            targetSteeringAngle = ZERO_STEERING_ANGLE;

        
        //  Tell Arduino the target steering value for it to rotate to
        robotMotors.targetSteeringValue = targetSteeringAngle;
        

        currentSteeringAngle = getAnalogValue(3);  // steering value is in 3 
        currentSteeringSpeed = getAnalogValue(2);  
        error = targetSteeringAngle - currentSteeringAngle;
        
        // Setup for coarse initial adjustment
        if (abs(error) > 20) {
            robotMotors.steeringMotorSpeed = 200;
        }
        if (abs(error) > 10)
            robotMotors.steeringMotorSpeed = 150;
                   
        if (error > 0)
           robotMotors.steeringMotorDirection = 'F';
        else
            robotMotors.steeringMotorDirection = 'R';
        
        
        //  The steering motor does not have enough power on its own to turn the robot.  To 
        //  help turn we use the drive motors to apply a power assist.
//        robotControlApplyTurnAssist2();

       str = targetSteeringAngle + "," + currentSteeringAngle + "," + currentSteeringSpeed 
                       + "," + robotMotors.steeringMotorDirection + '\n';
       
       RobotSupport.fileWriteTypeA(str);    
        
        // finally send the data to Arduino
        sendControlDataToArduino();
         
   }  // end processMotorControl
    
    public void processMotorControl2(RobotControlMsgGen cntrlMsg) {
        int targetSteeringAngle;
        int currentSteeringAngle, currentSteeringSpeed;
        int error, deltaError,speedAdjust;
        String str;

                
            
        // check for forward direction.  Motors will not turn unless enough initial speed
        if (cntrlMsg.driveSpeed > 100) {
             robotMotors.motor1Speed = cntrlMsg.driveSpeed;
             robotMotors.motor2Speed = cntrlMsg.driveSpeed;
        }
        else {
            robotMotors.motor1Speed = 0;
            robotMotors.motor2Speed = 0;
        }
        
        if (cntrlMsg.driveDirection == 'R') {
            robotMotors.motor1Direction = 'R';
            robotMotors.motor2Direction = 'R';
        }
        else {
            robotMotors.motor1Direction = 'F';
            robotMotors.motor2Direction = 'F';
        }
        
        
        // check for turning
        // Turning will come on with values between 0 and 100 (50 = center)
        // Need to map to the following angle values from steering sensor
        // minSteerinAngle --- zeroSteeringAngle --- maxSteeringAngle      
        
        if (cntrlMsg.steeringValue > 51) 
            targetSteeringAngle = (int) ((steeringPGain) * (cntrlMsg.steeringValue - 50)) + ZERO_STEERING_ANGLE;
        else if (cntrlMsg.steeringValue < 50) 
            targetSteeringAngle = (int) ((steeringNGain) * (cntrlMsg.steeringValue)) + MIN_STEERING_ANGLE;
        else 
            targetSteeringAngle = ZERO_STEERING_ANGLE;


        //  Tell Arduino the target steering value for it to rotate to
        robotMotors.targetSteeringValue = targetSteeringAngle;
        
        
        currentSteeringAngle = getAnalogValue(3);  // steering value is in 3 
        currentSteeringSpeed = getAnalogValue(2);  
        error = targetSteeringAngle - currentSteeringAngle;
        deltaError = error - previousSteeringError;
        previousSteeringError = error;
        
        // set direction
        if (error > 0)
            robotMotors.steeringMotorDirection = 'F';
        else
            robotMotors.steeringMotorDirection = 'R';
        
        // the remaining code determines the speed at which to set the motors.
        // The goal is to slowly increase the speed value until the motors start
        // to turn, and then leave it at that speed.
        
        if ((abs(error) > 3) && (deltaError >= 0)) {
            speedAdjust = previousSteeringSpeed + 20;
            robotMotors.steeringMotorSpeed = 100 + speedAdjust;
        }
        else {
            previousSteeringSpeed = 0;
            previousSteeringError = 0;
            robotMotors.steeringMotorSpeed = 0;
        }
        
        
        // Check if we are within our target goal.
        if (abs(error) <= 3) {
            robotMotors.steeringMotorSpeed = 0;
        }
        
        // do final boundry check
        if (robotMotors.steeringMotorSpeed > 254)
            robotMotors.steeringMotorSpeed = 254;
    
        
        // stop motors if trying to move out of bounds
        if ((currentSteeringAngle > MAX_STEERING_ANGLE) && (robotMotors.steeringMotorDirection == 'F'))
            robotMotors.steeringMotorSpeed = 0;
        
        if ((currentSteeringAngle < MIN_STEERING_ANGLE) && (robotMotors.steeringMotorDirection == 'R'))
            robotMotors.steeringMotorSpeed = 0;
        
       str = targetSteeringAngle + "," + currentSteeringAngle + "," + robotMotors.steeringMotorSpeed 
                       + "," + abs(error) + '\n';
       RobotSupport.fileWriteTypeA(str);
        
        
        
        // finally send the data to Arduino
        sendControlDataToArduino();
         
   }  // end processMotorControl
    
}