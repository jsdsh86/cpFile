package com.jsd.aim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
public class ReadAimDetial implements Callable{
	
	private File detailLog;
	public ReadAimDetial(File detailLog){
		this.detailLog = detailLog;
	}
	@Override
	public Map<String,String> call()  {
		Map<String,String> aimMap = new HashMap<String,String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader (new FileInputStream(detailLog)));
			String readLine = new String();
			while((readLine = br.readLine())!=null){
				String[] str = readLine.split("=");
				aimMap.put(str[0], str[1]);
			}
		} catch (FileNotFoundException e) {
			aimMap=null;
			e.printStackTrace();
		} catch (IOException e) {
			aimMap=null;
			e.printStackTrace();
		}
		return aimMap;
	}
	
}
