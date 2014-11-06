package util;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/** 
 * @ClassName: IpConfig 
 * @Description: TODO 
 * @author zhaohf@asiainfo.com 
 * @date 2014年9月9日 上午9:16:19 
 * 
 */
public class IpConfig {
//	public static void main(String[] args) {
//		IpConfig ip = new IpConfig();
//		ip.parseXML("ip_yidong.xml","");
//	}

	public void parseXML(String file_name,String province){
		try {
			InputStream inputStream = this.getClass().getResourceAsStream(file_name);
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			Element root = document.getRootElement();
			
			List<Element> provinceEle = root.elements();
			for (Element child : provinceEle) {
				System.out.println("name: " + child.attributeValue("name"));
				List<Element> ipEle = child.elements();
				for(Element ip : ipEle){
					System.out.println("beginip: " + ip.elementText("beginip"));
					System.out.println("endip: " + ip.elementText("endip"));
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	public static void writeXML(String file_name,Map<String, List<String[]>> map) {
		try {
			XMLWriter writer = null;// 声明写XML的对象

			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");// 设置XML文件的编码格式

			File file = new File(file_name);
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("global");

			Element country = root.addElement("country");
			country.addAttribute("name", "");
			
			for(Map.Entry<String, List<String[]>> m : map.entrySet()){
				String province = m.getKey();
				Element provinceEle = country.addElement("province");
				provinceEle.addAttribute("name",province);
				List<String[]> iplist = m.getValue();
				int i= 0 ;
				for(String[] str : iplist){
					Element ip = provinceEle.addElement("ip");
					ip.addAttribute("id", i+++"");
					
					Element beginip = ip.addElement("beginip");
					beginip.setText(str[0]);
					
					Element endip = ip.addElement("endip");
					endip.setText(str[1]);
				}
			}
			

			writer = new XMLWriter(new FileWriter(file), format);
			writer.write(document);
			writer.close();
			System.out.println("Finished !");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}