package com.horsehour.search.ip;

/**
 * /**
 * 
 * <pre>
 * QQwry.dat格式
 * 一. 文件头，共8字节 
 * 	   1. 第一个起始IP的绝对偏移， 4字节
 *     2. 最后一个起始IP的绝对偏移， 4字节
 * 二. "结束地址/国家/区域"记录区
 *     四字节IP地址后跟的每一条记录分成两个部分
 *     1. 国家记录
 *     2. 地区记录
 *     但是地区记录是不一定有的。而且国家记录和地区记录都有两种形式
 *     1. 以0结束的字符串
 *     2. 4个字节，一个字节可能为0x1或0x2
 * 		  a. 为0x1时，表示在绝对偏移后还跟着一个区域的记录，注意是绝对偏移之后，而不是这四个字节之后
 *        b. 为0x2时，表示在绝对偏移后没有区域记录
 *        不管为0x1还是0x2，后三个字节都是实际国家名的文件内绝对偏移
 * 		  如果是地区记录，0x1和0x2的含义不明，但是如果出现这两个字节，也肯定是跟着3个字节偏移，如果不是
 *        则为0结尾字符串
 * 三. "起始地址/结束地址偏移"记录区
 *     1. 每条记录7字节，按照起始地址从小到大排列
 *        a. 起始IP地址，4字节
 *        b. 结束IP地址的绝对偏移，3字节
 * 
 * 注意- 文件内IP地址和所有偏移量均采用little-endian格式，而Java采用big-endian格式,需转换
 * </pre>
 * 
 * @see http://lumaqq.linuxsir.org/article/qqwry_format_detail.html
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130430
 */
public class IPMain {

	public static void main(String[] args){
		String url = "http://www.horse.org/";
		String ip = IPUtil.decodeIP(url);

		IPLocation loc = new IPEngine("./data/research/qqwry.dat").getLocation(ip);
		System.out.println(ip + " : " + loc.toString());
	}
}