package com.jsd.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsd.start.CpFile;
import com.jsd.utils.Utils;

public class CpFiles implements Callable<Map<String, String>> {
	private ArrayList<Entry<String,String>> cpList;
	private int beg;
	private int end;
	private static Logger logger = LogManager.getLogger(CpFiles.class);
	public CpFiles(ArrayList<Entry<String,String>> cpList,int beg,int end){
		this.cpList = cpList;
		this.beg = beg;
		this.end=end;
	}
	/* 
	 * 如果失败,返回失败的map<md5,path>,正确返回空
	 * 
	 */
	@Override
	
	public Map<String,String> call() {
		Map<String,String>failMap=new HashMap<String,String>();
		FileInputStream fis=null;
		FileOutputStream fos = null;
		for(int i=beg;i<end; i++){
			try {
				fis = new FileInputStream(cpList.get(i).getValue());
				String newName = Utils.getNewName(cpList.get(i).getValue());
				fos =new FileOutputStream(newName);
				byte[] buf = new byte[1024*10];
				int len;
				while((len =fis.read(buf))!=-1){
					fos.write(buf,0,len);
				}
				fos.flush();
				
				//写入detail.log
				Map tepMap = new HashMap();
				tepMap.put(cpList.get(i).getKey(), newName);
				Utils.wirteDataToFile(tepMap, CpFile.detailLog);
				logger.info("write "+cpList.get(i).getValue()+" into "+CpFile.aimDic+" new name is "+newName);
				
			} catch (FileNotFoundException e) {
				failMap.put(cpList.get(i).getKey(), cpList.get(i).getValue());
				e.printStackTrace();
			} catch (IOException e) {
				failMap.put(cpList.get(i).getKey(), cpList.get(i).getValue());
				e.printStackTrace();
			} catch (InterruptedException e) {
				failMap.put(cpList.get(i).getKey(), cpList.get(i).getValue());
				e.printStackTrace();
			}
			
			
		}
		return failMap;
	}

}
