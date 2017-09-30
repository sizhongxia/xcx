package com.xcx.system.util;

public class Test_IdGenerator {
	public static void main(String[] args) {
		try {
			System.out.println(IdGenerator.getIdGenerator().getId().toString().length());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
