package com.lpr.track.utility;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.lpr.track.MainApp;

public class checkSumUtility {
	
	private static final Logger logger = Logger.getLogger(MainApp.class);
	public static String getCheckSum(String Data){
		//int uint8 = 0;
		int sum = 0 & 0xFF;
		//String[] datachunks = new ArrayList<String>();
		String[] datachunks = Data.trim().split("\\*");
		//System.out.println("1 : "+datachunks[0]);
		//System.out.println("2 : "+datachunks[1]);
		String[] chars = datachunks[0].split("");
		//System.out.println("--> "+chars.length);
		for(int i=0;i<chars.length;i++){
			//char c = s.charAt(0);
			char temp =chars[i].charAt(0);
			sum=sum+temp;
			sum=sum & 0xFF;
		}
		String hex = Integer.toHexString(sum);
		//System.out.println("Check Sum generated : "+sum+", in Hex Format : "+hex);
		logger.info("--> Check Sum generated : "+sum+", in Hex Format : "+hex);
		return hex;
	}

}
