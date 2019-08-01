package com.mashibing.netty.other.model;

import java.io.Serializable;
 
public class Response implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6236340795725143988L;
	private int age;
	private String name;
 
	public Response() {
	}
 
	public Response(int age, String name) {
		this.age = age;
		this.name = name;
	}
 
	public int getAge() {
		return age;
	}
 
	public void setAge(int age) {
		this.age = age;
	}
 
	public String getName() {
		return name;
	}
 
	public void setName(String name) {
		this.name = name;
	}
 
	@Override
	public String toString() {
		return "Response [age=" + age + ", name=" + name + "]";
	}
 
}