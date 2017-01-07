package com.jsd.start;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsd.NameQueue;
import com.jsd.aim.ReadAimDetial;
import com.jsd.aim.ReadAimInfo;
import com.jsd.aim.ReadAimTotl;
import com.jsd.src.CpFiles;
import com.jsd.utils.FileConst;
import com.jsd.utils.GetListMd5Map;
import com.jsd.utils.Utils;

public class CpFile {
	/*
	 * config.cfg
	 */
	public static String aimDic;
	public static File detailLog;
	private static final Logger logger = LogManager.getLogger(CpFile.class);
	public static final ArrayBlockingQueue<String> nameQueue= new ArrayBlockingQueue<String>(800);
	
	@SuppressWarnings("unchecked")
	public static void doCp(){
		Utils utils = new Utils();
		Map<String, String> readConfigInof = utils.readConfigInof("config.cfg");
		String aim = readConfigInof.get(FileConst.aim.getValue());
		String src = readConfigInof.get(FileConst.src.getValue());
		File aimFil = new File(aim);
		File srcFil = new File(src);
		aimDic=aimFil.getAbsolutePath();
		int sysPre = utils.getSysPre()<2?2:utils.getSysPre();
		logger.info(Calendar.getInstance()+" begin ...");
		Map<String,String> aimDetailMap = new HashMap<String,String>(); // detail.log中记录的map
		List<Long> aimTolList = null;
		List<Long> aimTolCurLst = null;
		Map<String,String> aimCurDetailMap = new HashMap<String,String>(); // aim实际的map
		List<String> aimFileList = utils.listAllFile(aimFil); // aim的fileList
		List<String> srcFileList = utils.listAllFile(srcFil); // src的fileList
		Map<String,String> srcDetailMap= new HashMap<String,String>(); // src的map
		
		ArrayList<Entry<String,String>> cpArrayList = new ArrayList<Entry<String,String>>(); //需要复制的list
		

		FutureTask<Map<String, String>> aimDet = null;
		FutureTask<ArrayList<Long>> aimTol = null;
		FutureTask<ArrayList<Long>> aimTolFut = null;
		
		File aimTolLog = null;
		
		Thread putName = new Thread(new NameQueue(nameQueue),"nameQueueGen-Thread");
		putName.start();
		//准备工作,2.1步骤
		if(aimFil.exists()){
			detailLog=new File(aim+File.separator+"detail.log");
			if(!detailLog.exists())
				try {
					detailLog.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			aimDet = new FutureTask<Map<String, String>>(new ReadAimDetial(detailLog));
			Thread getAimDetailMap = new Thread(aimDet);
			getAimDetailMap.start(); // 读取aim下detail.log的map	
			aimTolLog = new File(aim+File.separator+"total.log");
			if(!aimTolLog.exists()){
				try {
					aimTolLog.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("total.log "+aimTolLog.getAbsolutePath()+" is not exist!");
				}
			}
			if(aimTolLog.exists()){
				aimTol = new FutureTask<ArrayList<Long>>(new ReadAimInfo(aimTolLog));
				Thread getAimTolLog = new Thread(aimTol); //读取aim下total.log的list; 
				getAimTolLog.start();
			}else{
				// 重做一次
			}
			
			File aimTolFile = new File(aim);
			aimTolFut = new FutureTask<ArrayList<Long>>(new ReadAimTotl(aimTolFile)); // 读取aim下实际的文件数量和大小
			Thread getAimTol = new Thread(aimTolFut);
			getAimTol.start();
		}else{
			logger.info(aim+" aim file does't exist ");
		}
		
		ArrayList<FutureTask<Map<String, String>>> aimFTsk = new ArrayList<FutureTask<Map<String,String>>>();
		ArrayList<FutureTask<Map<String, String>>> srcFTsk = new ArrayList<FutureTask<Map<String,String>>>();
		PrintWriter printWriter=null;
		try {
			aimDetailMap = (Map<String, String>) aimDet.get(); // detail.log下所有记录
			aimTolList = (List<Long>) aimTol.get(); //total.log下记录个数和长度
			aimTolCurLst = (List<Long>) aimTolFut.get(); // 实际记录个数和长度
			if(aimTolList.get(0).longValue()!=aimTolCurLst.get(0).longValue() || aimTolList.get(1).longValue()!=aimTolCurLst.get(1).longValue()){
				ExecutorService aimMD5Trd = Executors.newFixedThreadPool(sysPre/2);
				ExecutorService srcMD5Trd = Executors.newFixedThreadPool(sysPre/2);
				for(int i=0; i<sysPre/2; i++){
					int beg = aimFileList.size()/(sysPre/2)*i;
					int end = i==(sysPre/2)-1?aimFileList.size():(aimFileList.size()/(sysPre/2)*(i+1)); 
				FutureTask<Map<String,String>> getAimMd5s = new FutureTask<Map<String, String>>(new GetListMd5Map(aimFileList, beg, end)); // aim MD5 get
				aimMD5Trd.submit(getAimMd5s);
				aimFTsk.add(getAimMd5s);
				beg =  srcFileList.size()/(sysPre/2)*i;
				end = i==(sysPre/2)-1?srcFileList.size():(srcFileList.size()/(sysPre/2)*(i+1));
				logger.debug("srcFileList"+" : "+srcFileList+" : "+beg+" :"+end);
				FutureTask<Map<String, String>> getSrcMd5s = new FutureTask<Map<String, String>>(new GetListMd5Map(srcFileList, beg, end)); //src MD5 get
				srcMD5Trd.submit(getSrcMd5s);
				srcFTsk.add(getSrcMd5s);
				}
				
				for(int i=0;i<aimFTsk.size();i++){
					Map<String,String> temAimCur = aimFTsk.get(i).get();
					aimCurDetailMap.putAll(temAimCur); // aim md5 map
				}
				printWriter = new PrintWriter(new FileWriter(detailLog,true),true);
				for(Entry<String,String> curEntry:aimCurDetailMap.entrySet()){
					if(!aimDetailMap.containsKey(curEntry.getKey())){
						if(!curEntry.getValue().contains("detail.log") && !curEntry.getValue().contains("total.log"))
						printWriter.println(curEntry.getKey()+"="+curEntry.getValue());
					}else{
						File rmFile = new File(curEntry.getValue());
						if(rmFile.exists()&&!aimDetailMap.containsValue(curEntry.getValue())){
							rmFile.delete();
							logger.info("you have copy a exist file "+curEntry.getValue()+" whitout used this app, the file name is "+
									aimDetailMap.get(curEntry.getKey())+" now,its deleted"
									);
						}
					}
				}
				
				for(int i=0;i<srcFTsk.size();i++){
					Map<String,String> temAimCur = srcFTsk.get(i).get();
					srcDetailMap.putAll(temAimCur); // src md5 map
				}
				
		
				if(printWriter!=null){
					printWriter.close();
				}
				
				aimMD5Trd.shutdown();
				srcMD5Trd.shutdown();
			}else{
				aimCurDetailMap = aimDetailMap;
				ExecutorService srcMD5Trd = Executors.newFixedThreadPool(sysPre/2);
				for(int i=0; i<sysPre/2; i++){
					int beg =  srcFileList.size()/(sysPre/2)*i;
					int end = (srcFileList.size()/(sysPre/2)*(i+1))>=srcFileList.size()?srcFileList.size():(srcFileList.size()/(sysPre/2)*(i+1));
					FutureTask<Map<String, String>> getSrcMd5s = new FutureTask<Map<String, String>>(new GetListMd5Map(srcFileList, beg, end)); //src MD5 get
					srcMD5Trd.submit(getSrcMd5s);
					srcFTsk.add(getSrcMd5s);
				}
				for(int i=0;i<srcFTsk.size();i++){
					Map<String,String> temAimCur = srcFTsk.get(i).get();
					srcDetailMap.putAll(temAimCur); // src md5 map
				}
				srcMD5Trd.shutdown();
			}
			for(Entry<String,String> curEntry:srcDetailMap.entrySet()){
				if(!aimCurDetailMap.containsKey(curEntry.getKey())){
					cpArrayList.add(curEntry);
				}
			}
			ArrayList<FutureTask<Map<String,String>>> cpFilesList = new ArrayList<FutureTask<Map<String,String>>>();
			Map <String,String>cpResltMap= new HashMap<String,String>();
			ExecutorService cpFilesEx = Executors.newFixedThreadPool(sysPre/2);
			for(int i=0; i<sysPre/2; i++){
				int beg = cpArrayList.size()/(sysPre/2)*i;
				int end = i==(sysPre/2)-1?cpArrayList.size():(cpArrayList.size()/(sysPre/2)*(i+1)); 
				logger.debug(cpArrayList.toString()+beg+" : "+end);
				FutureTask<Map<String,String>> cpFilesFut = new FutureTask<Map<String,String>> (new CpFiles(cpArrayList, beg, end)); // aim MD5 get
				cpFilesEx.submit(cpFilesFut);
				cpFilesList.add(cpFilesFut);
			}
			for(int i=0; i<cpFilesList.size();i++){
				
				cpResltMap.putAll(cpFilesList.get(i).get());
			}
			
			long totNo = utils.getTotNo(aimFil);
			long totSize = utils.getTotSize(aimFil);
			utils.writeStaticInfo(aimTolLog, totNo, totSize);
			cpFilesEx.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			aimDetailMap = null;
		} catch (IOException e) {

			e.printStackTrace();
		} 
	
	}
	
	public static void main(String[] args) {
		long beg = System.currentTimeMillis();
		doCp();
		long end = System.currentTimeMillis();
		System.out.println(end-beg);
	}
}
