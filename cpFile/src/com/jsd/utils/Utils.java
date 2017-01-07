package com.jsd.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.jsd.start.CpFile;

public class Utils {
	
	/**
	 * 读取目录下所有文件绝对路径,放入 list
	 * 
	 * @param dir
	 * @return
	 */
	public List<String> listAllFile(File dir) {
		List<String> list = new ArrayList<String>();
		listDirFiles(dir, list);
		return list;
	}

	public void listDirFiles(File dir, List<String> list) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				listDirFiles(file, list);
			} else {
				if(!file.getName().contains("detail.log"))
				list.add(file.getAbsolutePath());
			}
		}
	}
	public List<String> listAllFile(File dir,long size) {
		List<String> list = new ArrayList<String>();
		listDirFiles(dir, list,size);
		return list;
	}

	public void listDirFiles(File dir, List<String> list,long size) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				listDirFiles(file, list);
			} else {
				if(file.length()>size)
				list.add(file.getAbsolutePath());
			}
		}
	}
	
	/**
	 * 读取config下键值对,放入map
	 * 
	 * @param config
	 * @return
	 */
	public Map<String, String> readConfigInof(String config) {
		Map<String, String> map = new HashMap<String, String>();
				InputStreamReader is;
				InputStream ist = this.getClass().getClassLoader().getResourceAsStream(config);
				try {
					is = new InputStreamReader(ist,
							"utf-8");
					Properties prop = new Properties();
					prop.load(is);
					map.put(FileConst.aim.getValue(),
							(String) prop.get(FileConst.aim.getValue()));
					map.put(FileConst.src.getValue(),
							prop.getProperty(FileConst.src.getValue()));
				} catch (UnsupportedEncodingException | FileNotFoundException e) {
					map = null;
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		return map;
	}

	public int getSysPre() {
		return (Runtime.getRuntime().availableProcessors());
	}

	public String getMD5(String file) {
		File fl = new File(file);
		String MD5 = com.jsd.base.MD5utils.parseToMd5(fl);
		return MD5;
	}
	public String getMD5(File file) {
		String MD5 = com.jsd.base.MD5utils.parseToMd5(file);
		return MD5;
	}
	public Map<String, String> getMD5Map(List<String> files) {
		Map<String, String> map = new HashMap<String, String>();
		for (String file : files) {
			map.put(getMD5(file), file);
		}
		return map;
	}
	public Map<String, String> getFilesMD5Map(List<File> files) {
		Map<String, String> map = new HashMap<String, String>();
		for (File file : files) {
			map.put(getMD5(file), file.getAbsolutePath());
		}
		return map;
	}
	// 为了防止程序中途退出后,没有写入,导致处理数据未写入而丢失数据,处理数据的时候必须处理一条记录一条,其他情况随便
	public static void wirteDataToFile(Map<String, String> map, File file) {
		FileLock tryLock = null;
		RandomAccessFile raf = null;
		ByteBuffer byteBuffer = null;
		try {
			raf = new RandomAccessFile(file,"rw");
			raf.seek(file.length());
			FileChannel channel = raf.getChannel();
			while(true){
				try{
					tryLock = channel.tryLock();
					break;
				}catch(Exception e){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
			for (Entry<String, String> entry : map.entrySet()) {
				byteBuffer = ByteBuffer.wrap((entry.getKey() + "=" + entry.getValue()+"\r\n").getBytes());
				channel.write(byteBuffer);
				byteBuffer.clear();
			}
			tryLock.release();
			channel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		// TODO	logger.(map.toString() + " haven't write!");
		} catch (IOException e) {
			e.printStackTrace();
		// TODO	System.out.println(map.toString() + " haven't write!");
		} finally {
			if(raf !=null)
				try {
					raf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}

	/**
	 * 获得files从begin-end的MD5码,只取 beg-end之间的files
	 * 
	 * @param files
	 * @param begin
	 * @param end
	 * @return
	 */
	public Map<String, String> getListMd5s(List<String> files, int begin,
			int end) {
		Map resultMap = new HashMap<String,String>();
		for (int i = begin; i < end; i++) {
			resultMap.put(getMD5(files.get(i)), files.get(i));
		}
		return resultMap;
	}

	/**
	 * 
	 * @param aMap
	 * @param bMap
	 * @return aMap中有,bMap中没有的
	 */
	public Map<String, String> getUnexistMap(Map<String, String> aMap,
			Map<String, String> bMap) {
		Map<String, String> resultMap = new HashMap<String, String>();
		for (Entry<String, String> entry : aMap.entrySet()) {
			if (!entry.getValue().equals(bMap.get(entry.getKey()))) {
				resultMap.put(entry.getKey(), entry.getValue());
			}
		}
		return resultMap;
	}

	public static String getCurTime(String timeFormat){
		SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
		return sdf.format(new Date());
	}
	public static String getNewName(String srcName) throws InterruptedException{
		if(srcName.contains(".")){
			String srcFileType = (String) srcName.subSequence(srcName.lastIndexOf(".")+1,srcName.length());
			String curFileName = CpFile.nameQueue.take()+"."+srcFileType;
			return CpFile.aimDic+File.separatorChar+curFileName;
			
		}else {
			return srcName;
		}
	}
	
	public long getTotNo(File file) {
		long no=0;
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(File f:files){
				if(f.isDirectory()){
					getTotNo(f);
				}else{
					no++;
				}
			}
		}
		return no;
	}

	public long getTotSize(File file){
		long size =0;
		if(file.isDirectory()){
			 File[] files= file.listFiles();
			for(File f:files){
				if(f.isDirectory()){
					getTotSize(f);
				}else{
					if(!f.getAbsolutePath().contains("detail.log")){
						if(f.getAbsolutePath().contains("total.log")){
							size+=16;
						}else{
							size+=f.length();
						}
					}
				}
			}
		}
		return size;
	}
	
	public void writeStaticInfo(File tolLog,long no,long size){
		RandomAccessFile raf =null;
		try {
			raf= new RandomAccessFile(tolLog,"rw");
			raf.writeLong(no);
			raf.writeLong(size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(raf!=null)
				raf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void main(String[] args) {


	}

}
