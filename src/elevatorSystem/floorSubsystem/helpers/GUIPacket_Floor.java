package elevatorSystem.floorSubsystem.helpers;

import elevatorSystem.miscellaneous.Status;

public class GUIPacket_Floor {
	private Object obj; 
	private LampChangedCode opCode; 
	private int elevatorID;
	private Status status;
	
	public GUIPacket_Floor(Object obj, LampChangedCode opCode, int elevator) {
		this.obj = obj; 
		this.opCode = opCode; 	
		elevatorID = elevator;
	}
	
	public GUIPacket_Floor(Object obj, LampChangedCode opCode) {
		this.obj = obj; 
		this.opCode = opCode; 
	}
	
	public GUIPacket_Floor(Object obj, LampChangedCode opCode, int elevator, Status status) {
		this.obj = obj; 
		this.opCode = opCode;
		elevatorID = elevator;
		this.status = status;
	}
	
	public Object getObject() {
		return obj; 
	}
	
	public LampChangedCode getCode() {
		return opCode; 
	}
	
	public int getElevatorID() {
		return elevatorID;
	}
	
	public Status getStatus() {
		return status;
	}
	
	
}
