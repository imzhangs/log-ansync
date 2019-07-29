package com.log.ansync.enums;

public enum LogFilterEnum {

	log,
	txt,
	md,
	
	;
	
	public static boolean contains(String key) {
		for(LogFilterEnum e:LogFilterEnum.values()) {
			if(key.endsWith(e.name())) {
				return true;
			}
		}
		return false;
	}
}
