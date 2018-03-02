/*
 * RobotSupport
 */
package robotapp_v2p5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class : RobotSupport
 * 
 * Description: This class supports utility methods to aid in troubleshooting
 * and analyzing the various aspects of the robot
 * @author owner
 */
public class RobotSupport {
    
    static BufferedWriter typeA;
    static boolean typeAOpen;
    
    // constructor
    // open file for writing
    static public void RobotSupport() {
        typeAOpen = false;
    }
    
    
    // method for setting up the file
    public static void enableFiles () {
      
        // check if alredy open
        if (typeAOpen == true)
            return;
        
        // open the files for writing
        try {
            typeA = new BufferedWriter(new FileWriter("/mnt/ramdisk/fileTypeA"));
            typeAOpen = true;
        }
        catch (IOException e) {
            System.out.println("Error: Could not open outfile typeA");
            typeAOpen = false;
        }
    }
    
    // method for closing the files
    public static void disableFiles() {
        
        if (typeAOpen == false)
            return;
        
        typeAOpen = false;
        try {
            typeA.close();
        }
        catch (IOException e) {
        }
    }
    // global method to allow writing data to the file
    public static void fileWriteTypeA(String str) {
        
        if (typeAOpen == false)
            return;
        
        try {
            typeA.write(str);
        }
        catch (IOException e) {
            System.out.println("Error : writing to file");
            typeAOpen = false;
        }
    }  // end robotSupportWriteTypeA
    
}
