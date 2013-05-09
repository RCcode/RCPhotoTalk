package com.rcplatform.phototalk.request;

import java.io.File;


public class FileRequest extends Request{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private File file;
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
}
