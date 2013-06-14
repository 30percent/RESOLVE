package edu.clemson.cs.r2jt.archiving;

import java.awt.Rectangle;
import java.util.Date;

/**
 * 
 * <p>This class is used to setup the GUI for the jar file created 
 * by the compiler.</p>
 *
 */
public class GuiSetup {

    private final String title = "RESOLVE Command Line Simulator";
    private final int width = 800;
    private final int height = 600;

    private final Rectangle facilityLabelRec = new Rectangle(20, 10, 200, 20);
    private final Rectangle inputLabelRec = new Rectangle(20, 40, 100, 25);
    private final Rectangle outputLabelRec = new Rectangle(20, 75, 100, 25);
    private final Rectangle inputFieldRec = new Rectangle(125, 40, 600, 25);
    private final Rectangle outputTextAreaRec = new Rectangle(125, 75, 600, 400);
    private final Rectangle runButtonRec = new Rectangle(625, 5, 100, 25);
    //private final Rectangle enterButtonRec = new Rectangle(625, 40, 100, 25);
    private final Rectangle genMessageRec = new Rectangle(20, 500, 500, 20);
    private final Rectangle genDateRec = new Rectangle(300, 525, 200, 20);

    //private final String facilityName = "User Facility";
    private final String consoleInMsg = "Console Input:";
    private final String consoleOutMsg = "Console Output:";
    private final String facilityLabelMsg = "Facility: \" + programFacilityName";
    private final String runButtonMsg = "Run Facility";
    private final String enterButtonMsg = "Enter";
    private final String launchMsg = "Launching \" + programFacilityName + \"\\n\"";
    private final String completeMsg = "\\n\" + programFacilityName + \" Completed\\n\"";
    private final String genMsg =
            "Automatically generated by the Clemson RESOLVE " + "Verifying Compiler on:";
    private final Date date = new Date();
    private final String dateFormatString = "EEE, MMM d, yyyy 'at' HH:mm:ss";

    public GuiSetup() {

    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rectangle getFacilityLabelRec() {
        return facilityLabelRec;
    }

    public Rectangle getInputLabelRec() {
        return inputLabelRec;
    }

    public Rectangle getOutputLabelRec() {
        return outputLabelRec;
    }

    public Rectangle getInputFieldRec() {
        return inputFieldRec;
    }

    public Rectangle getOutputTextAreaRec() {
        return outputTextAreaRec;
    }

    public Rectangle getRunButtonRec() {
        return runButtonRec;
    }

    /*public Rectangle getEnterButtonRec() {
    	return enterButtonRec;
    }*/
    public Rectangle getGenMessageRec() {
        return genMessageRec;
    }

    public Rectangle getGenDateRec() {
        return genDateRec;
    }

    /*public String getFacilityName() {
    	return facilityName;
    }*/
    public String getConsoleInMsg() {
        return consoleInMsg;
    }

    public String getConsoleOutMsg() {
        return consoleOutMsg;
    }

    public String getFacilityLabelMsg() {
        return facilityLabelMsg;
    }

    public String getRunButtonMsg() {
        return runButtonMsg;
    }

    public String getEnterButtonMsg() {
        return enterButtonMsg;
    }

    public String getLaunchMsg() {
        return launchMsg;
    }

    public String getCompleteMsg() {
        return completeMsg;
    }

    public String getGenMsg() {
        return genMsg;
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormatString() {
        return dateFormatString;
    }

}
