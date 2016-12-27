package com.horsehour.search.encoding;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110707
 */
public class EncodeTable {
	// Supported 24 Encoding Types
	public static int TOTALTYPES = 24;

	public static int GB2312 = 0;
	public static int GBK = 1;
	public static int GB18030 = 2;
	public static int HZ = 3;
	public static int BIG5 = 4;
	public static int CNS11643 = 5;
	public static int UTF8 = 6;
	public static int UTF8T = 7;
	public static int UTF8S = 8;
	public static int UNICODE = 9;
	public static int UNICODET = 10;
	public static int UNICODES = 11;
	public static int ISO2022CN = 12;
	public static int ISO2022CN_CNS = 13;
	public static int ISO2022CN_GB = 14;
	public static int EUC_KR = 15;
	public static int CP949 = 16;
	public static int ISO2022KR = 17;
	public static int JOHAB = 18;
	public static int SJIS = 19;
	public static int EUC_JP = 20;
	public static int ISO2022JP = 21;
	public static int ASCII = 22;
	public static int UNDEFINED = 23;

	public static String[] nameList;

	public EncodeTable() {
		nameList = new String[TOTALTYPES];

		nameList[GB2312] = "GB2312";
		nameList[GBK] = "GBK";
		nameList[GB18030] = "GB18030";
		nameList[HZ] = "HZ-GB-2312";
		nameList[ISO2022CN_GB] = "ISO-2022-CN-EXT";
		nameList[BIG5] = "BIG5";
		nameList[CNS11643] = "EUC-TW";
		nameList[ISO2022CN_CNS] = "ISO-2022-CN-EXT";
		nameList[ISO2022CN] = "ISO-2022-CN";
		nameList[UTF8] = "UTF-8";
		nameList[UTF8T] = "UTF-8";
		nameList[UTF8S] = "UTF-8";
		nameList[UNICODE] = "UTF-16";
		nameList[UNICODET] = "UTF-16";
		nameList[UNICODES] = "UTF-16";
		nameList[EUC_KR] = "EUC-KR";
		nameList[CP949] = "x-windows-949";
		nameList[ISO2022KR] = "ISO-2022-KR";
		nameList[JOHAB] = "x-Johab";
		nameList[SJIS] = "Shift_JIS";
		nameList[EUC_JP] = "EUC-JP";
		nameList[ISO2022JP] = "ISO-2022-JP";
		nameList[ASCII] = "ASCII";
		nameList[UNDEFINED] = "ISO8859-1";
	}
}