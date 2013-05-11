package com.rcplatform.tigase;

public class TigaseNode {

	private String domain;

	private String ip;

	private int port;
	
	private int weight;
	
	private int status;
	
	private String remark;
	
	public void  setDomain(String domain){
		this.domain = domain;
	}
	
	public String getDomain(){
		return this.domain;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getIp(){
		return ip;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setWeight(int weight){
		this.weight = weight;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return status;
	}
	
	public void setRemark(String remark){
		this.remark = remark;
	}
	
	
	public String getRemark(){
		return this.remark;
	}

}