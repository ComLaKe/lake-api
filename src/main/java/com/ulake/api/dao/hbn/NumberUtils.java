package com.ulake.api.dao.hbn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberUtils {
	private static final Logger log = LoggerFactory.getLogger(NumberUtils.class);
	
	public static int asInt(long l) {
		if (l <= Integer.MAX_VALUE) {
			return (int) l;
		} else {
			log.warn("Passed long {} > Integer.MAX_INTEGER; returning Integer.MAX_INTEGER", l);
			return Integer.MAX_VALUE;
		}
	}
}