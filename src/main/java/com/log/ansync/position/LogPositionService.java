package com.log.ansync.position;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class LogPositionService {
	
	static final Map<String,Long> lastPositionMap=new ConcurrentHashMap<String,Long>();
	
	static final ScheduledThreadPoolExecutor cacheTask = new ScheduledThreadPoolExecutor(20);

	public static void loadAllPosition(File positionDir) {
		try {
			if(positionDir.exists()) {
				for(File indexFile:positionDir.listFiles()) {
					String positionStr = FileUtils.readFileToString(indexFile,"utf-8");
					lastPositionMap.put(indexFile.getName(), Long.valueOf(StringUtils.isBlank(positionStr)?"0":positionStr));
				}
			}else {
				FileUtils.forceMkdir(positionDir);
			}
			cacheTask.scheduleWithFixedDelay(new Runnable() {

				public void run() {
					lastPositionMap.clear();
				}
				
			},0,86400,TimeUnit.SECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writePosition(File lastPositionFile,long pos) {
		try {
			FileUtils.writeStringToFile(lastPositionFile, pos+"", "utf-8", false);
			lastPositionMap.put(lastPositionFile.getName(), pos);
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	public static long readPosition(String logPositionFilePath,String fileName){
		Long pos= lastPositionMap.get(fileName+".index");
		try {
			if(pos==null||pos==0) {
				File logPositionFile=new File(logPositionFilePath);
				String positionStr = FileUtils.readFileToString(logPositionFile,"utf-8");
				lastPositionMap.put(logPositionFile.getName()+".index", Long.valueOf(StringUtils.isBlank(positionStr)?"0":positionStr));
			}
			pos=lastPositionMap.get(fileName+".index");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pos==null?0:pos;
	}
	
	
}
