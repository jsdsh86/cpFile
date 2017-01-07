package com.jsd.aim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.jsd.utils.Utils;

public class ReadAimTotl implements Callable<ArrayList<Long>>{
	private File aim;
	public ReadAimTotl(File aim){
		this.aim = aim;
	}
	@Override
	public ArrayList<Long>  call()  {
		ArrayList<Long> tolList = new ArrayList<Long>();
		Utils utils = new Utils();
		tolList.add(utils.getTotNo(aim));
		tolList.add(utils.getTotSize(aim));
		return tolList;
	}
}