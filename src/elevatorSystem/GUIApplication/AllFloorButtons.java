package elevatorSystem.GUIApplication;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AllFloorButtons {
	
	//for now direction is a button 
	private Button[] DirectionButtons = new Button[2]; 
	private Button[] RequestButtons = new Button[2];
	
	Image upDirection = new Image("elevatorSystem/GUIApplication/icons/UpArrow.png"); 
	Image downDirection = new Image("elevatorSystem/GUIApplication/icons/DownArrow.png"); 
	
	Image upRequest = new Image("elevatorSystem/GUIApplication/icons/ReqUp.png"); 
	Image downRequest = new Image("elevatorSystem/GUIApplication/icons/ReqDown.png"); 
	
	
	public AllFloorButtons(){		
		for(int i=0; i < 2; i++) {
			if(i==0) {
				DirectionButtons[i]= new Button();				
				DirectionButtons[i].setGraphic(new ImageView(upDirection));
				RequestButtons[i]= new Button(); 
				RequestButtons[i].setGraphic(new ImageView(upRequest));
			}else if(i==1) {
				DirectionButtons[i]= new Button(); 
				DirectionButtons[i].setGraphic(new ImageView(downDirection));
				RequestButtons[i]= new Button(); 
				RequestButtons[i].setGraphic(new ImageView(downRequest));
			}

		}
	}
	
	public Button[] getDirectionButtons() {
		return DirectionButtons; 
	}
	
	public Button[] getRequestButtons() {
		return RequestButtons;
	}
}