# SYSC 3303 Project - Elevator System Iteration v5.0


### Lab 4, Team 3 – Tufayl Dhalla, Nayeeb Mowla, Abdillahi Nur, Mariana Rafael-White, Sahil Sharma

---

***Welcome!***

In this README file you will find a breakdown of the responsibilities split between the group members, setup instructions and a brief explanation behind the naming of all Java files.

---

***Authors and their contributions***

Tufayl Dhalla
>  Worked on modifying Scheduler to match iteration 4 requirements. Assisted in refactoring code/fixing minor errors for ElevatorSubsystem and FloorSubsystem classes. Refactored code in all classes wherever possible.

<br>

Nayeeb Mowla
> Worked on modifying Scheduler to match iteration 4 requirements. Assisted in refactoring code/fixing minor errors for 
ElevatorSubsystem and FloorSubsystem classes. Refactored code in all classes wherever possible.


<br>

Abdillahi Nur
>  Worked on modifying Scheduler to match iteration 4 requirements. Assisted in refactoring code/fixing minor errors for ElevatorSubsystem and FloorSubsystem classes. Refactored code in all classes wherever possible.

<br>

Mariana Rafael-White
>  Worked on modifying Scheduler to match iteration 4 requirements. Assisted in refactoring code/fixing minor errors for ElevatorSubsystem and FloorSubsystem classes. Refactored code in all classes wherever possible.

<br>

Sahil Sharma
>  Worked on modifying Scheduler to match iteration 4 requirements. Assisted in refactoring code/fixing minor errors for ElevatorSubsystem and FloorSubsystem classes. Refactored code in all classes wherever possible.

<br>

---

***Contents of .zip***


- Java Project (.java files, all required Eclipse files, etc.)
- README
- Timing Diagram ("L4G3_TimingDiagram.pdf")
- Updated UML Class Diagram ("L4G3_ClassDiagram.pdf")
- Text files to prove functionality ("simulation.txt", "output.txt", "arrivalSensors.txt", elevatorButtons.txt", "floorButtons.txt")
- Input file ("input.txt") 

---

***How to run project***


- Extract zip folder and import project to Eclipse as a Java project.
- To view the project in hierarchical view go to the Package Explorer tab and click the "View Menu" button on the top (down error). Then go to Package Presentation >> Hierarchical
- Run controller class, this will instantiate the Scheduler, ElevatorSubsystem<k> and FloorSubsystem and simulate an elevator system.
- Simulated results will be shown in the console.
- Condensed simulated results will be shown in "output.txt" and in "simulation.txt". These files can be found within the project folder.
- Simulation will be "done" when console prints “Calculations done!”
- Iteration 4's calculations can be view both on the console and in files "arrivalSensors.txt", "elevatorButtons.txt" and "floorButtons.txt"
- Test classes in the test packages can be run to view test classes and their results. (run each test individually to avoid bind socket exceptions)

---

***Changelog (v3.0 → 4.0)***


- Fault handling implementation completed
- Bugs from iteration 2 surrounding the multiple elevators resolved
- Calculations measuring the performance of the arrival sensors interface, elevator buttons interface and floor buttons interface are performed
- Implementation to allow for each subsystem to be run on a different machine, granted that the IP address is input by the user starting up each subsystem.

---

***How the elevator system works***


- The InputFileReader class (within the Floor Subsystem) reads in the input file and simulates users pressing floor buttons over time by passing in the requests to the FloorSubsystem through the ServiceRequest state class.
- The FloorSubsystem sends each request received by the InputFileReader class to the Scheduler for scheduling.
- The Scheduler subsystem places each request in a pending request queue to sort at every 'clock cycle' 
- At each 'clock cycle' the scheduler's SelectElevator state class algorithm selects which elevator car will deal with the request. If none match, the request is placed back in the pending request queue to be matched with a car at a later time. Each car that has requests to service is then told to start
- The ElevatorSubsystem<k> starts the motor of their car when told by the Scheduler through the StartElevator state class. Vice versa, it stops the motor through the StopElevator state class.
- The car simulates its arrival at each floor through the FloorSensor class within the ElevatorSubsystem<k>. At each periodic 'sense' of a floor, it sends a message to the Scheduler of this arrival.
- The Scheduler receives notifications of car arrivals at each floor through the ArrivedAtFloor state class. It determines whether the car should stop and if so, alerts the car to stop.
- If there is a car button to be simulated after picking up a user at this floor, pass it to the ElevatorSubsystem<k>'s DestinationSelected state class. 
- If the car is to stop here, it notifies the FloorSubsystem's ElevatorArrived state class of the car arrival.
- The ElevatorSubsystem<k>'s DestinationSelected state class passes the information about the car button pressed to the Scheduler's AddNewDestination state class.
- Note: The 'clock cycle' and the floor 'sense' times can be found in elevatorSystem>>miscellaneous>>IntData enumeration file. Also note that each car starts at a different floor, which also can be found in the same IntData enumeration file.
- There are two types of requests that the scheduler accounts for: PickUp requests are for picking people up at each floor and DropOff requests are for dropping people off (who are currently in a elevator car)

---

***How elevator should react to given input***

- The elevator subsystem has 4 cars, starting at floors 2, 4, 6 and 8. At t = 0, there are requests at three of these floors so those requests are placed in these cars immediately.
- Upon entrance to car ID#1 from floor 2, the stalled fault from the input file is passed in. The drop off request currently in this car is stuck there indefinitely. The elevator car ID#1 should now be broken and take in no more requests. Any pick up requests that were with this car are redistributed.
- Also upon entrance to car ID#4 from floor 7, the doors not closing fault from the input file is passed in. The elevator car ID#4 should now be broken and take in no more requests. Any requests (pick up or drop off) that were associated with this car are redistributed. Because this is a transient fault, the doors are eventually able to close and then the car ID#4 is no longer broken and is able to take in requests.
- Requests that cannot be placed in a car at the moment are placed in the buffer queue, that is output to the console.

---

***How faults are handled (Iteration 3)***


- Faults are placed in the fourth column of the input file as 'Fault'. 
- A fault value of 1 means that there is no fault to be simulated in this request 
- A fault value of 2 means that there is a hard fault (i.e. the car is stuck between floors)
- A fault value of 3 means that there is a transient fault (i.e. the doors wont close)
- Once the pick up request containing the fault has been serviced in Scheduler's ArrivedAtFloor state class, the fault information is passed to the ElevatorSubsystem<k>'s DestinationSelected state class, which then passes it back to the Scheduler's AddNewDestination state class which then passes the fault to ElevatorSubsystem<k>'s StartElevator state class.
- Once the fault is in the StartElevator state class, it is simulated. 
- The fault is created by stopping the elevator, and alerting the scheduler that the car has malfunctioned. 
- The Scheduler then uses its FaultHandler state class to indicate that this car is out of service, and then redistributes any appropriated requests associated with this car. (In the case of the doors not closing, all requests can be redistributed. In the case of the car stalled, only pick up requests can be redistributed)
- Fault's occur if the ElevatorSubsystem does not observe the floor sensor notification be sent in time, when it is expecting one. This is a hard fault meaning that the elevator is stuck. It also redistributes the requests through the FaultHandler.
- Fault's also occur if the door of the elevator won't close. If so, then all the requests associated with the elevator can be redistributed. In this case, the lamp on the floor subsystem turns back on. This is a transient fault, so the doors will eventually be able to close in a simulated time.

---

***How the performance of the Scheduler is measured (Iteration 4)***


- The time it takes for the Scheduler subsystem to first receive the service requests at time t and then send a message to start the elevators to respond to these requests is measured. This data can be found under the Arrival Sensors Interface (this interface because these states lead to the arrival sensors being turned on) (see "arrivalSensors.txt")
- The time it takes for the Scheduler subsystem to first receive that an elevator has arrived at a floor then send a message to stop the elevator in response is measured. This data can be found under the Elevator Buttons Interface (this interface because these states lead to the elevator buttons being turned off) (see "elevatorButtons.txt")
- The time it takes for the Scheduler subsystem to first receive that an elevator has arrived at a floor then send a message to alert the floor subsystem in response is measured. This data can be found under the Floor Buttons Interface (this interface because these states lead to the floor buttons being turned off) (see "floorButtons.txt")
- Note that these times do not include decoding the packets
- The time of several iterations of this program can be found in the files mentioned above. Note that the program has been run many times in advance to obtain a greater sample of data to calculate the mean and variance.
- To clear the data and start new calculations, delete these files from the file directory so new ones with no past data can be created.  
- The timing diagram depicts the mean for each of these values. The calculations after the most recent iteration of running this project before handing this in for submission were output to the console, and can be found in "Calculations.pdf".

---

***Known issues***


- "IntData" times used for threads sleeping may need to be adjusted depending on machine and lab environment.
- JavaFX issues

---
