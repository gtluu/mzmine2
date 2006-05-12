/*
 * Copyright 2006 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.methods.filtering.mean;

import java.text.NumberFormat;

import java.util.Vector;

import java.awt.Frame;

import net.sf.mzmine.interfaces.Scan;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.io.MZmineProject;
import net.sf.mzmine.methods.Method;
import net.sf.mzmine.methods.MethodParameters;
import net.sf.mzmine.userinterface.dialogs.ParameterSetupDialog;
import net.sf.mzmine.userinterface.mainwindow.MainWindow;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskController;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.util.Logger;


/**
 * This class represent a method for filtering scans in raw data file with moving average filter.
 */
public class MeanFilter implements Method, TaskListener {

    /**
     * @return Textual description of method
     */
	public String getMethodDescription() {
		return new String("Moving average filter");
	}


    /**
     * This function displays a modal dialog to define method parameters
     * @return parameters set by user
     */
	public MeanFilterParameters askParameters(MethodParameters parameters) {

		MeanFilterParameters currentParameters = (MeanFilterParameters)parameters;

		if (currentParameters==null) currentParameters = new MeanFilterParameters();

		// Show parameter setup dialog
		double[] paramValues = new double[1];
		paramValues[0] = currentParameters.oneSidedWindowLength;

		String[] paramNames = new String[1];
		paramNames[0] = "Give M/Z window length (one-sided)";

		NumberFormat[] numberFormats = new NumberFormat[1];
		numberFormats[0] = NumberFormat.getNumberInstance(); numberFormats[0].setMinimumFractionDigits(3);

		MainWindow mainWin = MainWindow.getInstance();

		ParameterSetupDialog psd = new ParameterSetupDialog((Frame)mainWin, "Please check the parameter values", paramNames, paramValues, numberFormats);
		psd.show();


		// Check if user clicked Cancel-button
		if (psd.getExitCode()==-1) {
			return null;
		}


		// Read parameter values
		double d;

		d = psd.getFieldValue(0);
		if (d<=0) {
			mainWin.displayErrorMessage("Incorrect M/Z window length value!");
			return null;
		}
		currentParameters.oneSidedWindowLength = d;

		return currentParameters;

	}


    /**
     * Runs this method on a given project
     * @param project
     * @param parameters
     */
    public void runMethod(RawDataFile[] rawDataFiles, MethodParameters parameters) {

		Task filterTask;

		for (RawDataFile rawDataFile: rawDataFiles) {
			filterTask = new MeanFilterTask(rawDataFile, (MeanFilterParameters)parameters);
			TaskController.getInstance().addTask(filterTask, this);
		}

	}

    public void taskStarted(Task task) {
		// do nothing
	}

    public void taskFinished(Task task) {

        if (task.getStatus() == Task.TaskStatus.FINISHED) {

			RawDataFile oldFile = (RawDataFile)((Object[])task.getResult())[0];
			RawDataFile newFile = (RawDataFile)((Object[])task.getResult())[1];
			MeanFilterParameters mfParam = (MeanFilterParameters)((Object[])task.getResult())[2];
			MZmineProject.getCurrentProject().updateFile(oldFile, newFile, this, mfParam);

        } else if (task.getStatus() == Task.TaskStatus.ERROR) {
            /* Task encountered an error */
            Logger.putFatal("Error opening a file: " + task.getErrorMessage());
            MainWindow.getInstance().displayErrorMessage(
                    "Error opening a file: " + task.getErrorMessage());

        }

	}

}

