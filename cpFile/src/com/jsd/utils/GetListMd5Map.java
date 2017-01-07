package com.jsd.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetListMd5Map implements Callable<Map<String, String>> {
	private List<String>files;
	private int beg;
	private int end;
	public GetListMd5Map(List<String>files,int beg,int end){
		this.files=files;
		this.beg=beg;
		this.end=end;
	}
	@Override
	public Map<String, String> call() throws Exception {
		Map<String, String> listMd5s = new Utils().getListMd5s(files, beg, end);
		return listMd5s;
	}


}
