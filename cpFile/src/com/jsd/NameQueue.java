package com.jsd;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.jsd.utils.Utils;

public class NameQueue implements Runnable {
	private ArrayBlockingQueue<String> nameQueue;
	 transient final ReentrantLock lock = new ReentrantLock();  
	public NameQueue(ArrayBlockingQueue<String> nameQueue){
		this.nameQueue=nameQueue;
	}
	@Override
	public void run() {
		String curTime = Utils.getCurTime("yyyyMMddHH");
		final ReentrantLock lock = this.lock; // 保证本对象先写完800个名字之后,别人才可以取出名字
		do{
			lock.lock(); 
			for(int i=0;i<800;i++){
				try {
					nameQueue.put(curTime+i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lock.unlock();
		}while(nameQueue.size()<1);
	}

}
