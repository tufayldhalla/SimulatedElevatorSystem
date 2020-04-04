/**
 * 
 */
package elevatorSystem.scheduler.helpers;

import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;

/**
 * @author L4G3
 *
 */
public class PickUp extends RequestData {

	public PickUp(Scheduler s, int floor, StringData direction, int destination, int fault) {
		super(s, floor, direction, destination, fault); 
	}
	
	public PickUp(int floor, StringData direction, int destination, int fault, int key) {
		super(floor, direction, destination, fault, key); 
	}

}
