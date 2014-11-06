package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.tendcloud.iplocation.core.IpLocationQueryInternal;
import com.tendcloud.iplocation.core.Location;

public class GetIpLocation {
	public static void main(String[] args) throws NumberFormatException, IOException {
		if(args.length != 3){
			System.exit(1);
			System.out.println("Usage: <input> <clumon> <output>");
		}
		IpLocationQueryInternal iplocation = new IpLocationQueryInternal();
		List<String> list = read_file_lines(args[1],Integer.valueOf(args[2]));
		List<String> result = new ArrayList<String>();
		for(String ip : list){
			Location l = iplocation.getLocationFromIp(ip);
			String line  = ip + "\t"+l.countryCode+"\t"+l.provinceCode+"\t"+l.cityCode;
			result.add(line);
		}
		FileUtils.writeLines(new File(args[3]), result);

	}
	public static List<String> read_file_lines(String filepath,int column) throws IOException{
		File file = new File(filepath);
		List<String> list = FileUtils.readLines(file);
		List<String> result = new ArrayList<String>();
		for(String li :list){
			if(li.trim() == "")
				continue;
			String params[] = li.split("\t");
			if(params.length < column)
				continue;
			result.add(params[column]);
		}
		return result;
	}
}
