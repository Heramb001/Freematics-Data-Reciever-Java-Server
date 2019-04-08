package com.lpr.track.utility;

import org.apache.log4j.Logger;

import com.lpr.track.MainApp;

public class generalUtilities {
	private static final Logger logger = Logger.getLogger(MainApp.class);
	public static void displayColumnNames(){
		logger.info("Data Trasfer Started !!!!!!!");
		System.out.println("Data Trasfer Started !!!!!!!");
		logger.info("Vehicle_id Vehicle_Speed Engine_RPM Throttle_Position EngineLoad TimingAdvance Accelerometer BatteryVoltage CPUTemperature");
		System.out.println("Vehicle_id Vehicle_Speed Engine_RPM Throttle_Position EngineLoad TimingAdvance Accelerometer BatteryVoltage CPUTemperature");
		loggers.DataLogger.info("Vehicle_id Vehicle_Speed Engine_RPM Throttle_Position EngineLoad TimingAdvance Accelerometer BatteryVoltage CPUTemperature");
	}
}
