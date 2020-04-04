package elevatorSystem.scheduler.helpers;


public class GUIPacket {
	private Object obj; 
	private VariableChangedCode opCode; 
	private int num;
	private int currFloor;
	private String list;
	
	public GUIPacket(Object obj, VariableChangedCode opCode) {
		this.obj = obj; 
		this.opCode = opCode; 	
	}
	
	public GUIPacket(Object obj, VariableChangedCode opCode, int currFloor, boolean doors) {
		this.obj = obj; 
		this.opCode = opCode; 	
		this.currFloor = currFloor;
		
	}
	
	public GUIPacket(Object obj, VariableChangedCode opCode, String s) {
		this.obj = obj; 
		this.opCode = opCode; 
		list = s;
	}
	
	public GUIPacket(Object obj, VariableChangedCode opCode, int num) {
		this.obj = obj; 
		this.opCode = opCode; 	
		this.num = num;
	}
	
	public Object getObject() {
		return obj; 
	}
	
	public VariableChangedCode getCode() {
		return opCode; 
	}
	
	public int getNum() {
		return num;
	}
	
	public int getCurrentFloor() {
		return currFloor;
	}
	
	public String getString() {
		return list;
	}
}