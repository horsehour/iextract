package com.horsehour.util;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Execute Scripts
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20150701 P.M.2:31:19
 **/
public class ScriptExecutor {

	/**
	 * Execute command line with arguments
	 * 
	 * @param args
	 */
	public static void execute(String[] args) {
		try {
			Runtime.getRuntime().exec(args);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Execute command line
	 * 
	 * @param cmd
	 * @return result
	 */
	public static String execute(String cmd) {
		Process p = null;
		StringBuffer sb = null;
		try {
			p = Runtime.getRuntime().exec("cmd /c " + cmd);// 执行完命令后关闭命令窗口
			sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = br.readLine()) != null)
				sb.append(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	/**
	 * 宽带连接
	 * 
	 * @param nameADSL
	 *            宽带名称（e.g. 宽带连接）
	 * @param userADSL
	 *            宽带账号（e.g. 11401023@cer）
	 * @param passADSL
	 *            宽带密码（e.g. chunheng）
	 * @return 连接成功则为true，否则为false
	 */
	public static boolean connADSL(String nameADSL, String userADSL, String passADSL) {
		String cmd = "rasdial " + nameADSL + " " + userADSL + " " + passADSL;
		String code = execute(cmd);

		if (code.indexOf("已连接") > 0) {
			System.out.println("connected.");
			return true;
		}

		System.err.println(code);
		System.err.println("fail to connect.");
		return false;
	}

	/**
	 * 断开宽带
	 * 
	 * @param nameADSL
	 * @return 断开则为true，否则为false
	 */
	public static boolean disconADSL(String nameADSL) {
		String cmd = "rasdial " + nameADSL + " /disconnect";
		String code = execute(cmd);

		if (code.indexOf("没有连接") > 0) {
			System.err.println(nameADSL + "not connected.");
			return false;
		}

		System.out.println("disconnect.");
		return true;
	}

	/**
	 * 定时待机
	 * 
	 * @param time
	 */
	public static void suspend(String time) {
		String cmd = "powercfg -h off";// 关闭休眠功能
		execute(cmd);
		cmd = "at " + time + " rundll32 powrprof.dll, SetSuspendState";
		execute(cmd);
	}

	/**
	 * wait for just a minute and suspend it
	 */
	public static void suspend() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		String time = df.format(new Date());
		String[] sub = time.split(":");

		int hour = new Integer(sub[0]);
		int minute = new Integer(sub[1]);

		if (minute < 9)
			time = sub[0] + ":0" + (minute + 1);
		else if (minute < 59)
			time = sub[0] + ":" + (minute + 1);
		else if (hour < 9)
			time = "0" + (hour + 1) + ":00";
		else if (hour == 23)
			time = "00:00";
		else if (hour == 24)
			time = "01:00";
		else
			time = (hour + 1) + ":00";

		suspend(time);
	}

	/**
	 * @return current time: hh:mm:ss
	 */
	public static String currentTime() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		return df.format(new Date());
	}

	/**
	 * @return current date: yyyy-MM-dd
	 */
	public static String currentDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(new Date());
	}

	/**
	 * @return all fonts supported in the current os
	 */
	public static String[] getAllOSFonts() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}

	// -----------------------------------------------------------------------------------
	/**
	 * 使用LETOR提供的度量工具(perl)检测预测结果
	 * 
	 * @param version
	 * @param testFile
	 * @param predictFile
	 * @param evalFile
	 * @param output
	 */
	public static void evalLETOR(int version, String testFile, String predictFile, String evalFile, String output) {
		String[] args = new String[6];
		args[0] = "D:/ProgramFiles/Perl/perl/bin/perl.exe";
		String code;
		if (version == 3)
			code = "Eval-Score-3.0.pl";
		else if (version == 4)
			code = "Eval-Score-4.0.pl";
		else if (version == 5)
			code = "Eval-Score-MSLR.pl";
		else
			return;

		args[1] = "F:/Research/Experiments/Baselines/Metric/" + code;
		args[2] = testFile;
		args[3] = predictFile;
		args[4] = evalFile;
		args[5] = "0";

		execute(args);
	}
}
