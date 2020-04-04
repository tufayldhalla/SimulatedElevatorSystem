/**
 * 
 */
package elevatorSystem.scheduler.helpers;

import elevatorSystem.miscellaneous.StringData;

/**
 * @author L4F3
 *
 */
public class DropOff extends RequestData{
	
	public DropOff(int floor, StringData direction, int key) {
		super(floor,direction, key);
	}
}
