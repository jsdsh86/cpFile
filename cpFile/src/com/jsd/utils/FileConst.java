package com.jsd.utils;

public enum FileConst {
	aim("aim"),src("src");
	private String value;
	FileConst(String s){
		this.value = s;
	}
	public String getValue(){
		return value;
	}
}
