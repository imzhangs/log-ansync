package com.log.ansync;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.log.ansync.enums.LogDirEnum;
import com.log.ansync.enums.LogFilterEnum;
import com.log.ansync.enums.LogTypeEnum;
import com.log.ansync.position.LogPositionService;

public class LogAnsync {

	static final String LOG_TYPE_KEY = "logType";

	static final Logger logger = LoggerFactory.getLogger(LogAnsync.class);

	static final Executor threadPool = new ThreadPoolExecutor(200, 1000, 5, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory());

	static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(20);
	static String saveLastPositionPath = "/log-ansync/position/";

	static Map<String, Long> logPositionMap = new HashMap<String, Long>();

	static long timestamp = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		Map<String, String> argMap = new HashMap<String, String>();
		for (String arg : args) {
			String params[] = arg.split("\\s+");
			for (String param : params) {
				argMap.put(param.split("=")[0].replaceFirst("\\-", "").trim(), param.split("=")[1].trim());
			}
		}
		LogTypeEnum logType = LogTypeEnum.OTHER;
		if (argMap.containsKey(LOG_TYPE_KEY)) {
			logType = LogTypeEnum.valueOf(argMap.get(LOG_TYPE_KEY).toUpperCase());
		} else {
			logger.warn("input param {} is null , use default value={}", LOG_TYPE_KEY, logType.getType());
		}
		String logInputPath = "";
		String logOutputPath = "";
		switch (logType) {
		case IIS:
			logInputPath = LogDirEnum.IIS_INPUT_DIR.getDefaultDir();
			if (argMap.containsKey(LogDirEnum.IIS_INPUT_DIR.getKey())) {
				logInputPath = argMap.get(LogDirEnum.IIS_INPUT_DIR.getKey());
				logOutputPath = argMap.get(LogDirEnum.IIS_OUTPUT_DIR.getKey());
			} else {
				logger.warn("input param {} is null , use default value={}", LogDirEnum.IIS_INPUT_DIR.getKey(),
						logInputPath);
			}
			logOutputPath = LogDirEnum.IIS_OUTPUT_DIR.getDefaultDir();
			if (argMap.containsKey(LogDirEnum.IIS_OUTPUT_DIR.getKey())) {
				logOutputPath = argMap.get(LogDirEnum.IIS_OUTPUT_DIR.getKey());
			} else {
				logger.warn("input param {} is null , use default value={}", LogDirEnum.IIS_OUTPUT_DIR.getKey(),
						logOutputPath);
			}
			break;
		case NGINX:
			logInputPath = LogDirEnum.NGINX_INPUT_DIR.getDefaultDir();
			if (argMap.containsKey(LogDirEnum.NGINX_INPUT_DIR.getKey())) {
				logInputPath = argMap.get(LogDirEnum.NGINX_INPUT_DIR.getKey());
			} else {
				logger.warn("input param {} is null , use default value={}", LogDirEnum.NGINX_INPUT_DIR.getKey(),
						logInputPath);
			}
			logOutputPath = LogDirEnum.NGINX_OUTPUT_DIR.getDefaultDir();
			if (argMap.containsKey(LogDirEnum.NGINX_OUTPUT_DIR.getKey())) {
				logOutputPath = argMap.get(LogDirEnum.NGINX_OUTPUT_DIR.getKey());
			} else {
				logger.warn("input param {} is null , use default value={}", LogDirEnum.NGINX_OUTPUT_DIR.getKey(),
						logOutputPath);
			}
			break;
		default:
			logInputPath = LogDirEnum.OTHER_INPUT_DIR.getDefaultDir();
			if (argMap.containsKey(LogDirEnum.OTHER_INPUT_DIR.getKey())) {
				logInputPath = argMap.get(LogDirEnum.OTHER_INPUT_DIR.getKey());
			} else {
				logger.warn("input param {} is null , use default value={}", LogDirEnum.OTHER_INPUT_DIR.getKey(),
						logInputPath);
			}
			logOutputPath = LogDirEnum.OTHER_OUTPUT_DIR.getDefaultDir();
			if (argMap.containsKey(LogDirEnum.OTHER_OUTPUT_DIR.getKey())) {
				logOutputPath = argMap.get(LogDirEnum.OTHER_OUTPUT_DIR.getKey());
			} else {
				logger.warn("input param {} is null , use default value={}", LogDirEnum.OTHER_OUTPUT_DIR.getKey(),
						logOutputPath);
			}
			break;
		}

		LogPositionService.loadAllPosition(new File(saveLastPositionPath + logType.name().toLowerCase() + "/"));

		if (StringUtils.isNotBlank(logInputPath)) {
			seekLogFile(logType, logInputPath, logOutputPath, 5);
		} else {
			logger.error("logPath ={} !!!!", logInputPath);
		}
	}

	static void seekLogFile(final LogTypeEnum logType, final String logInputDir, final String logOutputDir, long delay)
			throws Exception {
		File logDirFile = new File(logInputDir);
		if (!logDirFile.exists()) {
			logger.error("log input dir {} does't exists !!!", logInputDir);
			return;
		}
		for (final File logFile : logDirFile.listFiles()) {
			if (!LogFilterEnum.contains(logFile.getName())) {
				logger.error("{} does no matched *.(log|txt) !!!", logFile.getName());
				continue;
			}
			
			scheduled.scheduleWithFixedDelay(new Runnable() {
				public void run() {
					RandomAccessFile readRandomFile = null;
					RandomAccessFile writeRandomFile = null;
					try {
						readRandomFile = new RandomAccessFile(logFile, "r");
						String saveLogPath = logOutputDir.endsWith("/") ? logOutputDir
								: logOutputDir + "/" + logType.name().toLowerCase() + "/";
						switch (logType) {
						case IIS:
							FileUtils.forceMkdir(new File(saveLogPath));
							break;
						case NGINX:
						case OTHER:
						default:
							saveLogPath = saveLogPath + DateFormatUtils.format(new Date(), "yyyyMMdd");
							FileUtils.forceMkdir(new File(saveLogPath));
							break;
						}

						String lastPositionFilePath = saveLastPositionPath + logType.name().toLowerCase() + "/" + logFile.getName() + ".index";
						long logPosition = LogPositionService.readPosition(lastPositionFilePath,logFile.getName());

						writeRandomFile = new RandomAccessFile(new File(saveLogPath + logFile.getName()), "rw");
						readRandomFile.seek(logPosition);
						String line = "";
						while (StringUtils.isNotBlank(line = readRandomFile.readLine())) {
							timestamp = System.currentTimeMillis() - timestamp;
							writeRandomFile.seek(writeRandomFile.length());
							writeRandomFile.write((line + "\r\n").getBytes());
							logger.debug("{}ms seek upate ,line={},conten={}", timestamp, logPosition, line);
						}
						logPosition = readRandomFile.getFilePointer();

						LogPositionService.writePosition(new File(lastPositionFilePath), logPosition);

						logger.debug("{} logPosition={}", logFile.getName(), logPosition);
						timestamp = System.currentTimeMillis();

					} catch (IOException e) {
						logger.error("seekLogFile({}) seek ERROR!", logInputDir, e);
					} finally {
						if (readRandomFile != null) {
							try {
								readRandomFile.close();
							} catch (IOException e) {
								logger.error("{} readRandomFile.close() ERROR!", logInputDir, e);
							}
						}
						if (writeRandomFile != null) {
							try {
								writeRandomFile.close();
							} catch (IOException e) {
								logger.error("{} writeRandomFile.close() ERROR!", logInputDir, e);
							}
						}
					}
				}

			}, 0, delay, TimeUnit.SECONDS);
		}
	}

	static void readAllLogs(final File file) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles()) {
				readAllLogs(sub);
			}
		} else if (file.getName().endsWith(".log")) {
			threadPool.execute(new Runnable() {
				public void run() {
					try {
						for (String line : FileUtils.readLines(file, "utf-8")) {
							logger.info(line);
						}
					} catch (IOException e) {
						logger.error("readAllLogs({}) ERROR!", file, e);
					}
				}

			});
		}
	}

}
