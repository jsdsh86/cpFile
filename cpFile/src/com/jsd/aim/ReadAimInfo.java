package com.jsd.aim;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * @author jsd
 *先读取aim目录total.log的个数和文件长度
 */
public class ReadAimInfo implements Callable<ArrayList<Long>>{
	private File tolLog;
	public ReadAimInfo(File tolLog){
		this.tolLog = tolLog;
	}
	@Override
	public ArrayList<Long>  call()  {
		ArrayList<Long> tolList = new ArrayList<Long>();
		RandomAccessFile raf = null;
		long num=0;
		long size=0;
		try {
			raf = new RandomAccessFile(tolLog,"r");
			num = raf.readLong();
			size = raf.readLong();

		} catch (FileNotFoundException e) {
			tolList=null;
			e.printStackTrace();
		} catch(EOFException eo){
			
		}catch (IOException e) {
			tolList=null;
			e.printStackTrace();
		}finally{
			tolList.add(num);
			tolList.add(size);
			if(raf!=null){
				try {
					raf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return tolList;
	}
}