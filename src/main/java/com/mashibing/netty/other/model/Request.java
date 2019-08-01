package com.mashibing.netty.other.model;

import java.io.Serializable;
 
public class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7033707301911915196L;
	private String url;
 
	public Request() {
	}
 
	public Request(String url) {
		this.url = url;
	}
 
	public String getUrl() {
		return url;
	}
 
	public void setUrl(String url) {
		this.url = url;
	}
 
	@Override
	public String toString() {
		return "Request [url=" + url + "]";
	}
 
}