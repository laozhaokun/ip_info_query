package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
/** 
 * @ClassName: GetIpDetailsFromTaobao 
 * @Description: TODO 
 * @author zhaohf@asiainfo.com 
 * @date 2014年9月9日 上午9:17:35 
 * 从淘宝ip库中查询Ip信息
 */
public class GetIpDetailsFromTaobao {
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage:<inpu_file> <column>");
		}
		try {
			List<String> list = FileUtils.readLines(new File(args[0]));
			for(String line :list){
				if(line.trim() == "")
					continue;
				String params[] = line.trim().split("\t");
				getIpInfo(params[Integer.valueOf(args[1])]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getIpInfo(String ip) {
		try {
			URL url = new URL("http://ip.taobao.com/service/getIpInfo.php?ip="
					+ ip);
			HttpURLConnection htpcon = (HttpURLConnection) url.openConnection();
			htpcon.setRequestMethod("GET");
			htpcon.setDoOutput(true);
			htpcon.setDoInput(true);
			htpcon.setUseCaches(false);

			InputStream in = htpcon.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in));
			StringBuffer temp = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null) {
				temp.append(line).append("\r\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			JSONObject obj = (JSONObject) JSON.parse(temp.toString());
			if (obj.getIntValue("code") == 0) {
				JSONObject data = obj.getJSONObject("data");
				StringBuffer buf = new StringBuffer();
				buf.append(ip + "\t");
				buf.append(data.getString("country")+"\t");
				buf.append(data.getString("region") + "\t");
				buf.append(data.getString("city") + "\t");
				buf.append(data.getString("isp"));
				System.out.println(buf.toString());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
