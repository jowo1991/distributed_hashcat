package de.jowo.pspac.util;

import java.time.LocalTime;

public class Utils {
	public static String convertMilliSecondToHHMMSSString(long milliseconds) {
		return LocalTime.MIN.plusSeconds(milliseconds / 1000).toString();
	}
}
