package com.jsd.src;

import java.io.File;
import java.util.List;

import com.jsd.utils.Utils;

public class MvFiles {

	public static void main(String[] args) {
		File src = new File("");
		File aim = new File("");
		Utils utils = new Utils();
		List<String> allFile = utils.listAllFile(src, 80*1024*1024);
		
	}

}
