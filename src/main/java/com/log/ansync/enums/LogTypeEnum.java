package com.log.ansync.enums;

public enum LogTypeEnum {

	IIS("iis"),
	NGINX("nginx"),
	APACHE("apache"),
	F5("f5"),
	OTHER("other")
	;
	
	private String type;
	
	LogTypeEnum(String type){
		this.type=type;
	}
	
	public String getType() {
		return type;
	}
}
