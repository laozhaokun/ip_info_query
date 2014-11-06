package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @ClassName: HTMLProcessUtil
 * @Description: TODO
 * @author zhaohf@asiainfo.com
 * @date 2014年9月9日 上午9:14:22
 * IP数据来源：http://ips.chacuo.net/
 * 整理IP数据成为配置文件
 */
public class HTMLProcessUtil {
	private static final String REGEX_TITLE = "<dt>(\\D{1,10})</dt>";
	private static final String REGEX_ITEM = "<dd>\\D*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\D*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\D*</dd>";

	 public static void main(String[] args) throws IOException {
	//
	// String test=" feffdsfwef <dt>香港(HK)</dt>"+
	// "<dd><span class=\"v_l\">202.97.96.0</span><span class=\"v_r\">202.97.111.255</span><div class=\"clearfix\"></div></dd>"+
	// "<dd><span class=\"v_l\">202.97.112.0</span><span class=\"v_r\">202.97.127.255</span><div class=\"clearfix\"></div></dd>"+
	// "<dt>安徽(AH)</dt>"+
	// " <dd><span class=\"v_l\">36.56.0.0</span><span class=\"v_r\">36.63.255.255</span><div class=\"clearfix\"></div></dd>"+
	// "  <dd><span class=\"v_l\">103.22.16.0</span><span class=\"v_r\">103.22.19.255</span><div class=\"clearfix\"></div></dd> fwefwef<dd>fe</dd>";
	 String ips = FileUtils.readFileToString(new File("C:\\Users\\zhaohf\\Desktop\\ipduan.txt"));
//		String str = read_from_url("http://ipcn.chacuo.net/view/i_CNCGROUP");
//		 String  str = "";
		Map<String, List<String[]>> map = getIPInfo(ips);
		IpConfig.writeXML("ip_jiangshu.xml", map);
//		for (Map.Entry<String, List<String[]>> m : data.entrySet()) {
//			List<String[]> list = m.getValue();
//			for (String[] str : list) {
//				System.out.println(str[0] + "\t" + str[1]);
//			}
//		}
	}

	public static String read_from_url(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			return doc.getElementsByClass("list").html();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 从给定字符串中，以默认规则提取各省ip信息 Map<String, List<String[]>>
	 * 以省名称为key;其中List<String[]>代表一个省所有可能的分段；其中String[]是IP区间的2个起始值；
	 * 
	 * @param test
	 * @return
	 */
	public static Map<String, List<String[]>> getIPInfo(String test) {
		Map<String, List<String[]>> data = new LinkedHashMap<String, List<String[]>>();
		List<String> result = fetchContent(test, REGEX_TITLE);
		List<String> datas = splicString(result, test);
		for (String items : datas) {
			String provinceName = fetchOneGroup(items, REGEX_TITLE).get(0);
			List<String[]> itemDatas = fetchMutiGroup(items, REGEX_ITEM);
			data.put(provinceName, itemDatas);
		}
		return data;
	}

	/**
	 * 切分字符串
	 * 
	 * @param result
	 * @param test
	 * @return
	 */
	private static List<String> splicString(List<String> result, String test) {
		if (result == null || result.size() == 0) {
			return null;
		}
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < result.size(); i++) {
			if (i == result.size() - 1) {
				res.add(test.substring(test.indexOf(result.get(i))));
				continue;
			}

			int start = test.indexOf(result.get(i));
			int end = test.indexOf(result.get(i + 1));
			res.add(test.substring(start, end));
		}
		return res;
	}

	/**
	 * 如果没有分组，抛出异常；否则返回List<String[]>,其中String[]是装载着每个分组值，注意代表整个表达式的分组不包含在内；
	 * 
	 * @param test
	 * @param matcherStr
	 * @return
	 */
	private static List<String[]> fetchMutiGroup(String test, String matcherStr) {
		Pattern pp = Pattern.compile(matcherStr);
		Matcher matcher = pp.matcher(test);
		List<String[]> result = new ArrayList<String[]>();
		int groupCount = matcher.groupCount();
		while (matcher.find()) {
			if (groupCount == 0) {
				throw new RuntimeException("必须要有1个以上的分组");
			} else {
				List<String> items = new ArrayList<String>();
				for (int i = 1; i <= groupCount; i++) {
					items.add(matcher.group(i));
				}
				result.add(items.toArray(new String[0]));
			}
		}
		return result;
	}

	/**
	 * 如果分组不是一个，抛出异常；否则返回List<String>;
	 * 
	 * @param test
	 * @param matcherStr
	 * @return
	 */
	private static List<String> fetchOneGroup(String test, String matcherStr) {
		Pattern pp = Pattern.compile(matcherStr);
		Matcher matcher = pp.matcher(test);
		List<String> result = new ArrayList<String>();
		int groupCount = matcher.groupCount();
		while (matcher.find()) {
			if (groupCount != 1) {
				throw new RuntimeException("只能有1个的分组");
			}
			result.add(matcher.group(1));
		}
		return result;
	}

	/**
	 * 返回所有匹配的字符列表
	 * 
	 * @param test
	 * @param matcherStr
	 * @return
	 */
	private static List<String> fetchContent(String test, String matcherStr) {
		Pattern pp = Pattern.compile(matcherStr);
		Matcher matcher = pp.matcher(test);
		List<String> result = new ArrayList<String>();
		while (matcher.find()) {
			result.add(matcher.group());

		}
		return result;
	}
}
