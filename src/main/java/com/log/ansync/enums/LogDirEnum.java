package com.log.ansync.enums;

public enum LogDirEnum {

	IIS_INPUT_DIR("iis.input.dir",System.getenv("SystemDrive") +"/inetpub/logs/LogFiles/W3SVC1") ,
	IIS_OUTPUT_DIR("iis.output.dir","D:/log-ansync/output/LogFiles") ,
	
	NGINX_INPUT_DIR("nginx.input.dir","/usr/local/nginx/logs") ,
	NGINX_OUTPUT_DIR("nginx.output.dir","/log-ansync/nginxlogs/") ,
	
	OTHER_INPUT_DIR("other.input.dir","/home/logs") ,
	OTHER_OUTPUT_DIR("other.output.dir","/log-ansync/otherlogs/") ,
	;
	
	private String defaultDir;
	
	private String key;
	
	LogDirEnum(String key, String defaultDir){
		this.key=key;
		this.defaultDir=defaultDir;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getDefaultDir() {
		return this.defaultDir;
	}
}
