package info.gabrielszabo.jdi.data;

public class MultiParamConstructor {
	
	public ParamInterface paramIntf;
	public TestInterface paramTestIntf;
	
	public MultiParamConstructor(ParamInterface param1, TestInterface param2) {
		this.paramIntf = param1;
		this.paramTestIntf = param2;
	}

}
