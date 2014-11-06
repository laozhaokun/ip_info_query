package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/** 
 * @ClassName: IpQuery 
 * @Description: TODO 
 * @author zhaohf@asiainfo.com 
 * @date 2014年9月9日 上午9:17:35 
 * 
 */
public class IpQuery {
	public static void main(String[] args) throws NumberFormatException, IOException {
//		Map<String,String> map = new IpQuery().getIpOfAProvince("ip_yidong.xml", "浙江");
//		for(Map.Entry<String, String> m : map.entrySet())
//			System.out.println(m.getKey() + "\t"+m.getValue());
//		getIps(args);
		getIpsSH(args);//ipchaxun.jar
//		getIpCount(args);
	}
	//查看某省市某运营商有多少ip
	public static void getIpCount(String[] args) throws IOException{
		Map<String,String> map = new IpQuery().getIpOfAProvince("ip_dianxin.xml","上海");
		long result = 0;
		for(Map.Entry<String, String>  m : map.entrySet()){
			result += getIpNumber(m.getKey(), m.getValue());
		}
		System.out.println(result);
	}
	//打印出某地区的记录
	public static void  getIpsSH(String[] args) throws IOException{
		Map<String,String> map = new IpQuery().getIpOfAProvince("ip_china.xml","上海");
		int column =  Integer.valueOf(args[1]);
		final LineIterator it = FileUtils.lineIterator(new File(args[0]),"UTF-8");
		try {
			while (it.hasNext()) {
				final String line = it.nextLine();
				if (line.trim() == "")
					continue;
				String[] params = line.split("\t");
				String ip = params[column];
				boolean flag = contains(ip, map);
				if (flag) {
					System.out.println(line);
				}
			}
		} finally {
			it.close();
		}
	}
	public static void getIps(String[] args) throws NumberFormatException, IOException{
		Options options = new Options();
		options.addOption("i",true,"指定查询某运营商(ISP)");
		options.addOption("p",true,"指定查询某省市(province)");
		options.addOption("f",true,"指定输入文件");
		options.addOption("c",true,"指定ip所在的列(从0开始)");
		options.addOption("ip",true,"查找一个ip的归属");
		options.addOption("b",false,"指定是否是大文件(大文件边度边处理,小文件一次读完)");
		options.addOption("print",false,"指定是否打印符合条件的ip");
		String help = "参数说明：\n-f\t指定输入文件\n-c\t指定ip所在的列(从0开始)"
				+ "\n-i\t指定查询某运营商(ISP)\n-p\t指定查询某省市(province)"
				+ "\n其中省市和运营商用汉字，如电信，浙江，教育网，上海"
				+ "\n-print指定是否打印符合条件的ip";
		options.addOption("h",false,help);
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			String file_name = "ip_";
			if(cmd.hasOption("h") ||cmd.getOptions().length == 0){
				System.out.println(help);
				System.exit(1);
			}
			if(!cmd.hasOption("ip")){
				if(!cmd.hasOption("f") || !cmd.hasOption("c")){
					System.out.println("必须指定输入文件和ip所在的列");
					System.exit(1);
				}
			}
			if(cmd.hasOption("i") && cmd.hasOption("p")){
				//查询某省市的ip，读入这个省市的ip段
				String province = cmd.getOptionValue("p");
				String isp = cmd.getOptionValue("i");
				if("电信".equals(isp))
					file_name += "dianxin";
				else if("联通".equals(isp))
					file_name += "liantong";
				else if("教育网".equals(isp))
					file_name += "edu";
				else if("移动".equals(isp))
					file_name += "yidong";
				else if("铁通".equals(isp))
					file_name += "tietong";
				else if("网通".equals(isp))
					file_name += "wangtong";
				else{
					System.out.println("暂不支持 " + isp +"。");
					System.exit(1);
				}
				Map<String,String> map = new IpQuery().getIpOfAProvince(file_name + ".xml",province);
				String file_input = cmd.getOptionValue("f");
				int column = Integer.valueOf(cmd.getOptionValue("c"));
				int count = 0,size = 0;
				if(cmd.hasOption("b")){
					final LineIterator it = FileUtils.lineIterator(new File(file_input), "UTF-8");
					try {
						while (it.hasNext()) {
							++size;
							final String line = it.nextLine();
							if(line.trim() == "")
								continue;
							String[] params = line.split("\t");
							String ip = params[column];
							boolean flag = contains(ip,map);
							if(flag){
								count += 1;
								if(cmd.hasOption("print"))
									System.out.println(line);
							}
						}
					} finally {
						it.close();
					}
				}else{
					List<String> lines = readFile(file_input);
					size = lines.size();
					for(String line : lines){
						String[] params = line.split("\t");
						String ip = params[column];
						boolean flag = contains(ip,map);
						if(flag){
							count += 1;
							if(cmd.hasOption("print"))
								System.out.println(line);
						}
					}
				}
				
				System.out.println("共有ip: "+ size+"\n");
				System.out.println(province + isp +"的 ip 数共有：" + count+"\n");
				System.out.println("非" + province + isp +" 的ip数共有：" + (size-count)+"\n");
				DecimalFormat df = new DecimalFormat("0.00%"); 
				System.out.println(province + isp + "ip的占比：" +df.format(count/(double)size)+"\n");
			}else if(cmd.hasOption("i")){
				System.out.println("查有多少个某运营商的ip，暂不提供。");
			}else if(cmd.hasOption("p")){
				file_name += "china";
				String file_input = cmd.getOptionValue("f");
				int column = Integer.valueOf(cmd.getOptionValue("c"));
				String province = cmd.getOptionValue("p");
				Map<String,String> map = new IpQuery().getIpOfAProvince(file_name + ".xml",province);
				int size =0,count = 0;
				if(cmd.hasOption("b")){
					final LineIterator it = FileUtils.lineIterator(new File(file_input), "UTF-8");
					try {
						while (it.hasNext()) {
							++size;
							final String line = it.nextLine();
							if(line.trim() == "")
								continue;
							String[] params = line.split("\t");
							String ip = params[column];
							boolean flag = contains(ip,map);
							if(flag){
								count += 1;
								if(cmd.hasOption("print"))
									System.out.println(line);
							}
						}
					} finally{
						it.close();
					}
				}else{
					List<String> lines = readFile(file_input);
					size = lines.size();
					for(String line : lines){
						String[] params = line.split("\t");
						String ip = params[column];
						boolean flag = contains(ip,map);
						if(flag){
							count += 1;
							if(cmd.hasOption("print"))
								System.out.println(line);
						}
					}
				}
				System.out.println("共有ip: "+ size+"\n");
				System.out.println(province +" 的ip数共有：" + count+"\n");
				System.out.println("非" + province +" 的ip数共有：" + (size-count)+"\n");
				DecimalFormat df = new DecimalFormat("0.00%"); 
				System.out.println(province + " ip的占比：" +df.format(count/(double)size)+"\n");
			}
			if(cmd.hasOption("ip")){
				String ip = cmd.getOptionValue("ip");
				System.out.println("暂不支持");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ip是否包含在ip段中
	 * @param ip
	 * @return
	 */
	public static boolean contains(String ip,Map<String,String> map){
		for(Map.Entry<String, String> m : map.entrySet()){
			long beginIp = ip2long(m.getKey());
			long endIp = ip2long(m.getValue());
			long longIp = ip2long(ip);
			if(longIp >= beginIp && longIp <= endIp)
				return true;
		}
		return false;
	}
	/**
	 *  把形如127.0.0.1的IP地址转换成一个long值
	 * @param ipAddress
	 * @return
	 */
	public final static long ip2long(String ipAddress) {
		long rtn = 0;
		String[] ss = ipAddress.split("\\.");
		for (int i = 0; i < ss.length; i++) {
			rtn <<= 8;
			rtn += Long.parseLong(ss[i]);
		}
		for (int i = ss.length; i < 4; i++) {
			rtn <<= 8;
		}
		return rtn;
	}

	/**
	 * 求从一个ip地址开始的ip数的具体ip地址
	 * @param beginip
	 * @param count
	 * @return
	 */
	public static String[] getIpList(String beginip,int count){
		try {
			long long_beginip = Long.MIN_VALUE;
			long long_endip = Long.MIN_VALUE;

			// 转换成long值
			long_beginip = ip2long(beginip);
			// 转换成long值
			long_endip = long_beginip + count;

			// 求解范围之内的IP地址
			long[] long_ip = new long[(int) Math.abs(long_beginip - long_endip) + 1];
			for (int k = 0; k <= Math.abs(long_beginip - long_endip); k++) {
				if (long_beginip - long_endip < 0) {
					long_ip[k] = long_beginip + (long) k;
				} else {
					long_ip[k] = long_endip + (long) k;
				}
			}

			// 换成字符串
			String[] strip = new String[4];
			String[] ipList = new String[long_ip.length];
			for (int m = 0; m < long_ip.length; m++) {
				strip[0] = String.valueOf(long_ip[m] & 0x00000000000000ff);
				strip[1] = String.valueOf(long_ip[m] >> 8 & 0x00000000000000ff);
				strip[2] = String
						.valueOf(long_ip[m] >> 16 & 0x00000000000000ff);
				strip[3] = String
						.valueOf(long_ip[m] >> 24 & 0x00000000000000ff);
				ipList[m] = strip[3] + "." + strip[2] + "." + strip[1] + "."
						+ strip[0];
			}

			return ipList;

		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}
	public static long getIpNumber(String beginip, String endip){
		long long_beginip = Long.MIN_VALUE;
		long long_endip = Long.MIN_VALUE;

		// 转换成long值
		long_beginip = ip2long(beginip);
		// 转换成long值
		long_endip = ip2long(endip);

		// 求解范围之内的IP地址
		return Math.abs(long_beginip - long_endip) + 1;
	}
	/**
	 * 求ip地址段间的所有ip地址
	 * @param beginip
	 * @param endip
	 * @return
	 */
	public static String[] getIpList(String beginip, String endip) {
		try {
			long long_beginip = Long.MIN_VALUE;
			long long_endip = Long.MIN_VALUE;

			// 转换成long值
			long_beginip = ip2long(beginip);
			// 转换成long值
			long_endip = ip2long(endip);

			// 求解范围之内的IP地址
			long[] long_ip = new long[(int) Math.abs(long_beginip - long_endip) + 1];
			for (int k = 0; k <= Math.abs(long_beginip - long_endip); k++) {
				if (long_beginip - long_endip < 0) {
					long_ip[k] = long_beginip + (long) k;
				} else {
					long_ip[k] = long_endip + (long) k;
				}
			}

			// 换成字符串
			String[] strip = new String[4];
			String[] ipList = new String[long_ip.length];
			for (int m = 0; m < long_ip.length; m++) {
				strip[0] = String.valueOf(long_ip[m] & 0x00000000000000ff);
				strip[1] = String.valueOf(long_ip[m] >> 8 & 0x00000000000000ff);
				strip[2] = String
						.valueOf(long_ip[m] >> 16 & 0x00000000000000ff);
				strip[3] = String
						.valueOf(long_ip[m] >> 24 & 0x00000000000000ff);
				ipList[m] = strip[3] + "." + strip[2] + "." + strip[1] + "."
						+ strip[0];
			}

			return ipList;

		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 读取文件中的某列的ip地址
	 * @param file
	 * @param column
	 * @param partition
	 * @return
	 * @throws IOException
	 */
	public static List<String> readFile(String file,int column) throws IOException{
		List<String> list = FileUtils.readLines(new File(file));
		List<String> iplist = new ArrayList<String>();
		for(String line :list){
			if(line.trim() == "")
				continue;
			String[] ips = line.trim().split("\t");
			iplist.add(ips[column]);
		}
		return iplist;
	}
	public static List<String> readFile(String file) throws IOException{
		List<String> list = FileUtils.readLines(new File(file));
		List<String> iplist = new ArrayList<String>();
		for(String line :list){
			if(line.trim() == "")
				continue;
			iplist.add(line);
		}
		return iplist;
	}
	/**
	 * 读取XML文件中的某省的Ip地址段
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Map<String,String> getIpOfAProvince(String file_name,String province) throws IOException{
		Map<String,String> map = new HashMap<String,String>();
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(file_name);
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			Element root = document.getRootElement();
			
			List<Element> provinceEle = root.elements();
			for (Element child : provinceEle) {
				if(!child.attributeValue("name").trim().equals(province.trim()))
					continue;
				List<Element> ipEle = child.elements();
				for(Element ip : ipEle){
					String beginip = ip.elementText("beginip");
					String endip = ip.elementText("endip");
					map.put(beginip,endip);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return map;
	}

}
