package com.jdi.data;

import com.jdi.annotations.Discriminator;

public class TestInterfaceImpl5 {
	
	private MyEnum value;
	
	public TestInterfaceImpl5(@Discriminator("VALUE_B") MyEnum value) {
		this.value = value;
	}
	
	public MyEnum getEnum() {
		return value;
	}

}
