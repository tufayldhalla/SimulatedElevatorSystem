package elevatorSystem.GUIApplication;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.*;
import javafx.stage.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import elevatorSystem.Controller;
import elevatorSystem.floorSubsystem.helpers.Floor;
import elevatorSystem.floorSubsystem.helpers.GUIPacket_Floor;
import elevatorSystem.floorSubsystem.helpers.InputFileReader;
import elevatorSystem.floorSubsystem.helpers.LampChangedCode;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Status;
import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;
import elevatorSystem.scheduler.helpers.ElevatorStatus;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.VariableChangedCode;
import elevatorSystem.scheduler.states.AddNewDestination;
import elevatorSystem.scheduler.states.ArrivedAtFloor;
import elevatorSystem.scheduler.states.FaultHandler;
import elevatorSystem.scheduler.states.SelectElevator;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class GUIApplication extends Application implements Observer {

	//private static final double SCALE_FACTOR = 0.8;

	enum MediaPlaying {
		MEDIA1, MEDIA2, MEDIA3
	};

	MediaPlaying mediaPlaying = MediaPlaying.MEDIA1;
	Button testFile = new Button("View Input");
	Controller elevatorSystem = new Controller(this, true);
	Double speed = 1.0;
	ChoiceBox<String> cb;
	ChoiceBox<String> cb2;
	ImageView sad = new ImageView("elevatorSystem/GUIApplication/icons/sad.png");

	// private AllFloorButtons[] floorButtons;
	TextArea scheduler = new TextArea();
	TextArea currentFloorNum1 = new TextArea();
	TextArea currentFloorNum2 = new TextArea();
	TextArea currentFloorNum3 = new TextArea();
	TextArea currentFloorNum4 = new TextArea();

	TextArea pending1 = new TextArea();
	TextArea pending2 = new TextArea();
	TextArea pending3 = new TextArea();
	TextArea pending4 = new TextArea();

	TextArea direction1 = new TextArea();
	TextArea direction2 = new TextArea();
	TextArea direction3 = new TextArea();
	TextArea direction4 = new TextArea();

	TextArea status1 = new TextArea();
	TextArea status2 = new TextArea();
	TextArea status3 = new TextArea();
	TextArea status4 = new TextArea();

	TextArea yesOrNo1 = new TextArea();
	TextArea yesOrNo2 = new TextArea();
	TextArea yesOrNo3 = new TextArea();
	TextArea yesOrNo4 = new TextArea();

	String file_contents = "";

	int[] carLocation = new int[NUM_ELEVATORS];
	Button[][] panel1;
	Button[][] panel2;
	Button[][] panel3;
	Button[][] panel4;

	private static final int NUM_FLOORS = IntData.NUM_FLOORS.getNum();
	private static final int NUM_ELEVATORS = IntData.NUM_ELEVATORS.getNum();

	Label[] upDirectionButton = new Label[NUM_FLOORS];
	Label[] downDirectionButton = new Label[NUM_FLOORS];
	Label[][] upDirectionLamp = new Label[NUM_FLOORS][NUM_ELEVATORS];
	Label[][] downDirectionLamp = new Label[NUM_FLOORS][NUM_ELEVATORS];
	Rectangle[][] cars = new Rectangle[NUM_ELEVATORS][NUM_FLOORS];
	Text[][] carText = new Text[NUM_ELEVATORS][NUM_FLOORS];
	StackPane[][] carsAndText = new StackPane[NUM_ELEVATORS][NUM_FLOORS];

	Image notLitButton = new Image("elevatorSystem/GUIApplication/icons/notLit.png");
	Image litButton = new Image("elevatorSystem/GUIApplication/icons/Lit.png");
	Image upDirection = new Image("elevatorSystem/GUIApplication/icons/UpArrow.png");
	Image downDirection = new Image("elevatorSystem/GUIApplication/icons/DownArrow.png");
	Image upRequest = new Image("elevatorSystem/GUIApplication/icons/ReqUp.png");
	Image downRequest = new Image("elevatorSystem/GUIApplication/icons/ReqDown.png");
	Image litUpDirection = new Image("elevatorSystem/GUIApplication/icons/LitUpArrow.png");
	Image litDownDirection = new Image("elevatorSystem/GUIApplication/icons/LitDownArrow.png");
	Image litUpRequest = new Image("elevatorSystem/GUIApplication/icons/LitReqUp.png");
	Image litDownRequest = new Image("elevatorSystem/GUIApplication/icons/LitReqDown.png");

	URL resource = getClass().getResource("icons/music.wav");
	Media media = new Media(resource.toString());
	URL resource2 = getClass().getResource("icons/music2.wav");
	Media media2 = new Media(resource2.toString());
	URL resource3 = getClass().getResource("icons/music3.wav");
	Media media3 = new Media(resource3.toString());
	URL faultResource = getClass().getResource("icons/scream.wav");
	Media faultMedia = new Media(faultResource.toString());
	MediaPlayer mediaPlayer = new MediaPlayer(media);
	MediaPlayer mediaPlayer2 = new MediaPlayer(media2);
	MediaPlayer mediaPlayer3 = new MediaPlayer(media3);
	MediaPlayer faultMediaPlayer = new MediaPlayer(faultMedia);

	int screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
	int screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
	HBox root;
	StackPane[][] carsAndDoors = new StackPane[NUM_ELEVATORS][NUM_FLOORS];
	Rectangle[][] doorsL = new Rectangle[NUM_ELEVATORS][NUM_FLOORS];
	Rectangle[][] doorsR = new Rectangle[NUM_ELEVATORS][NUM_FLOORS];
	TranslateTransition transitionR;
	ScaleTransition STransitionR;
	TranslateTransition transitionL;
	ScaleTransition STransitionL;
	Color sahil = Color.BROWN;

	@Override
	public void start(Stage stage) throws Exception {

		// *****************************************************************************************************************//
		// ****************************************Elevator
		// Section***********************************************************//
		// *****************************************************************************************************************//
		GridPane elevators = new GridPane();
		elevators.setPadding(new Insets(2, 2, 2, 2));
		elevators.setVgap(screenWidth / 200);
		elevators.setHgap(screenHeight / 200);

		// *****************Elevator 1 **********************//
		BorderPane Elevator1 = new BorderPane();
		// Elevator1.setPrefSize(400, 500);
		Elevator1.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-radius: 5;" + "-fx-border-color: grey;");

		GridPane ButtonPanel1 = new GridPane();
		ButtonPanel1.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

		ButtonPanel1.setHgap(2);
		ButtonPanel1.setVgap(2);
		panel1 = new Button[4][6];
		int count = 1;
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 4; col++) {
				if (count <= 22) {
					panel1[col][row] = new Button();
					panel1[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
							+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
							+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
							+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
					panel1[col][row].setText(count + "");
					count++;
					ButtonPanel1.add(panel1[col][row], col, row);
				}
			}
		}

		GridPane InfoElev1 = new GridPane();

		// Max height of info grid pane
		InfoElev1.setMaxHeight(10);

		Elevator1.setLeft(InfoElev1);
		Elevator1.setRight(ButtonPanel1);

		// Elevator Number Title
		Label elevatorNumber1 = new Label("ELEVATOR1");
		Elevator1.setTop(elevatorNumber1);

		// Current Floor of elevator 1
		Label currentFloor1 = new Label("Current Floor: ");
		// currentFloorNum1.setEditable(false);
		currentFloorNum1.setEditable(false);
		currentFloorNum1.setMaxWidth(screenWidth / 10);
		currentFloorNum1.setStyle("-fx-opacity:1;");
		InfoElev1.add(currentFloor1, 0, 0);
		InfoElev1.add(currentFloorNum1, 1, 0);

		// Current status (Working or not) of Elevator 1
		Label elevStatus1 = new Label("Working: ");
		status1.setEditable(false);
		status1.setMaxWidth(screenWidth / 10);
		status1.setStyle("-fx-opacity:1;");
		InfoElev1.add(elevStatus1, 0, 1);
		InfoElev1.add(status1, 1, 1);

		// Current direction of elevator 1
		Label directionTravelling1 = new Label("Direction Traveling: ");
		direction1.setEditable(false);
		direction1.setMaxWidth(screenWidth / 10);
		direction1.setStyle("-fx-opacity:1;");
		InfoElev1.add(directionTravelling1, 0, 2);
		InfoElev1.add(direction1, 1, 2);

		// Current status of elevator 1 (Moving or Stopped)
		Label isMoving1 = new Label("Travel Status: ");
		yesOrNo1.setEditable(false);
		yesOrNo1.setMaxWidth(screenWidth / 10);
		yesOrNo1.setStyle("-fx-opacity:1;");
		InfoElev1.add(isMoving1, 0, 3);
		InfoElev1.add(yesOrNo1, 1, 3);

		// All Pending requests in elevator 1's queue
		Label requests1 = new Label("Pending Requests: ");
		pending1 = new TextArea();
		pending1.setEditable(false);
		pending1.setMaxWidth(screenWidth / 10);
		pending1.setStyle("-fx-opacity:1;");
		InfoElev1.add(requests1, 0, 4);
		InfoElev1.add(pending1, 1, 4);

		// **************End Elevator1 **************************************//

		// ********************Elevator 2************************************//
		BorderPane Elevator2 = new BorderPane();
		// Elevator2.setPrefSize(500, 600);
		Elevator2.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-radius: 5;" + "-fx-border-color: grey;");

		GridPane ButtonPanel2 = new GridPane();
		ButtonPanel2.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

		ButtonPanel2.setHgap(2);
		ButtonPanel2.setVgap(2);
		panel2 = new Button[4][6];
		int count2 = 1;
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 4; col++) {
				if (count2 <= 22) {
					panel2[col][row] = new Button();
					panel2[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
							+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
							+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
							+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
					panel2[col][row].setText(count2 + "");
					count2++;
					ButtonPanel2.add(panel2[col][row], col, row);
				}
			}
		}

		GridPane InfoElev2 = new GridPane();
		// Max height of info grid pane
		InfoElev2.setMaxHeight(10);
		Elevator2.setLeft(InfoElev2);
		Elevator2.setRight(ButtonPanel2);

		// Elevator Number Title
		Label elevatorNumber2 = new Label("ELEVATOR2");
		Elevator2.setTop(elevatorNumber2);

		// Current Floor of elevator 2
		Label currentFloor2 = new Label("Current Floor: ");
		currentFloorNum2.setEditable(false);
		currentFloorNum2.setMaxWidth(screenWidth / 10);
		currentFloorNum2.setStyle("-fx-opacity:1;");
		InfoElev2.add(currentFloor2, 0, 0);
		InfoElev2.add(currentFloorNum2, 1, 0);

		// Current status (Working or not) of Elevator 2
		Label elevStatus2 = new Label("Working: ");
		status2.setEditable(false);
		status2.setMaxWidth(screenWidth / 10);
		status2.setStyle("-fx-opacity:1;");
		InfoElev2.add(elevStatus2, 0, 1);
		InfoElev2.add(status2, 1, 1);

		// Current direction of elevator 2
		Label directionTravelling2 = new Label("Direction Traveling: ");
		direction2.setEditable(false);
		direction2.setMaxWidth(screenWidth / 10);
		direction2.setStyle("-fx-opacity:1;");
		InfoElev2.add(directionTravelling2, 0, 2);
		InfoElev2.add(direction2, 1, 2);

		// Current status of elevator 2 (Moving or Not)
		Label isMoving2 = new Label("Travel Status: ");
		yesOrNo2.setEditable(false);
		yesOrNo2.setMaxWidth(screenWidth / 10);
		yesOrNo2.setStyle("-fx-opacity:1;");
		InfoElev2.add(isMoving2, 0, 3);
		InfoElev2.add(yesOrNo2, 1, 3);

		// All Pending requests in elevator 2's queue
		Label requests2 = new Label("Pending Requests: ");
		pending2 = new TextArea();
		pending2.setEditable(false);
		pending2.setMaxWidth(screenWidth / 10);
		pending2.setStyle("-fx-opacity:1;");
		InfoElev2.add(requests2, 0, 4);
		InfoElev2.add(pending2, 1, 4);
		// ********************************END elevator2
		// *****************************************//

		// *********************************Elevator
		// 3********************************************//
		BorderPane Elevator3 = new BorderPane();
		// Elevator3.setPrefSize(500, 600);
		Elevator3.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-radius: 5;" + "-fx-border-color: grey;");

		GridPane ButtonPanel3 = new GridPane();
		ButtonPanel3.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

		ButtonPanel3.setHgap(2);
		ButtonPanel3.setVgap(2);
		panel3 = new Button[4][6];
		int count3 = 1;
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 4; col++) {
				if (count3 <= 22) {
					panel3[col][row] = new Button();
					panel3[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
							+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
							+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
							+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
					panel3[col][row].setText(count3 + "");
					count3++;
					ButtonPanel3.add(panel3[col][row], col, row);
				}
			}
		}

		GridPane InfoElev3 = new GridPane();
		// Max height of info grid pane
		InfoElev3.setMaxHeight(10);
		Elevator3.setLeft(InfoElev3);
		Elevator3.setRight(ButtonPanel3);

		// Elevator Number Title
		Label elevatorNumber3 = new Label("ELEVATOR3");
		Elevator3.setTop(elevatorNumber3);

		// Current Floor of elevator 3
		Label currentFloor3 = new Label("Current Floor: ");
		currentFloorNum3.setEditable(false);
		currentFloorNum3.setMaxWidth(screenWidth / 10);
		currentFloorNum3.setStyle("-fx-opacity:1;");
		InfoElev3.add(currentFloor3, 0, 0);
		InfoElev3.add(currentFloorNum3, 1, 0);

		// Current status (Working or not) of Elevator 3
		Label elevStatus3 = new Label("Working: ");
		status3.setEditable(false);
		status3.setMaxWidth(screenWidth / 10);
		status3.setStyle("-fx-opacity:1;");
		InfoElev3.add(elevStatus3, 0, 1);
		InfoElev3.add(status3, 1, 1);

		// Current direction of elevator 3
		Label directionTravelling3 = new Label("Direction Traveling: ");
		direction3.setEditable(false);
		direction3.setMaxWidth(screenWidth / 10);
		direction3.setStyle("-fx-opacity:1;");
		InfoElev3.add(directionTravelling3, 0, 2);
		InfoElev3.add(direction3, 1, 2);

		// Current status of elevator 3 (Moving or Not)
		Label isMoving3 = new Label("Travel Status: ");
		yesOrNo3.setEditable(false);
		yesOrNo3.setMaxWidth(screenWidth / 10);
		yesOrNo3.setStyle("-fx-opacity:1;");
		InfoElev3.add(isMoving3, 0, 3);
		InfoElev3.add(yesOrNo3, 1, 3);

		// All Pending requests in elevator 3's queue
		Label requests3 = new Label("Pending Requests: ");
		pending3 = new TextArea();
		pending3.setEditable(false);
		pending3.setMaxWidth(screenWidth / 10);
		pending3.setStyle("-fx-opacity:1;");
		InfoElev3.add(requests3, 0, 4);
		InfoElev3.add(pending3, 1, 4);
		// ********************************END elevator3
		// *****************************************//

		// *********************************Elevator
		// 4********************************************//
		BorderPane Elevator4 = new BorderPane();
		// Elevator4.setPrefSize(500, 600);
		Elevator4.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-radius: 2;" + "-fx-border-color: grey;");

		GridPane ButtonPanel4 = new GridPane();
		ButtonPanel4.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 2;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

		ButtonPanel4.setHgap(2);
		ButtonPanel4.setVgap(2);
		panel4 = new Button[4][6];
		int count4 = 1;
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 4; col++) {
				if (count4 <= 22) {
					panel4[col][row] = new Button();
					panel4[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
							+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
							+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
							+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
					panel4[col][row].setText(count4 + "");
					count4++;
					ButtonPanel4.add(panel4[col][row], col, row);
				}
			}
		}

		GridPane InfoElev4 = new GridPane();
		// Max height of info grid pane
		InfoElev4.setMaxHeight(10);
		Elevator4.setLeft(InfoElev4);
		Elevator4.setRight(ButtonPanel4);

		// Elevator Number Title
		Label elevatorNumber4 = new Label("ELEVATOR4");
		Elevator4.setTop(elevatorNumber4);

		// Current Floor of elevator 4
		Label currentFloor4 = new Label("Current Floor: ");
		currentFloorNum4.setEditable(false);
		currentFloorNum4.setMaxWidth(screenWidth / 10);
		currentFloorNum4.setStyle("-fx-opacity:1;");
		InfoElev4.add(currentFloor4, 0, 0);
		InfoElev4.add(currentFloorNum4, 1, 0);

		// Current status (Working or not) of Elevator 4
		Label elevStatus4 = new Label("Working: ");
		status4.setEditable(false);
		status4.setMaxWidth(screenWidth / 10);
		status4.setStyle("-fx-opacity:1;");
		InfoElev4.add(elevStatus4, 0, 1);
		InfoElev4.add(status4, 1, 1);

		// Current direction of elevator 4
		Label directionTravelling4 = new Label("Direction Traveling: ");
		direction4.setEditable(false);
		direction4.setMaxWidth(screenWidth / 10);
		direction4.setStyle("-fx-opacity:1;");
		InfoElev4.add(directionTravelling4, 0, 2);
		InfoElev4.add(direction4, 1, 2);

		// Current status of elevator 4 (Moving or Not)
		Label isMoving4 = new Label("Travel Status: ");
		yesOrNo4.setEditable(false);
		yesOrNo4.setMaxWidth(screenWidth / 10);
		yesOrNo4.setStyle("-fx-opacity:1;");
		InfoElev4.add(isMoving4, 0, 3);
		InfoElev4.add(yesOrNo4, 1, 3);

		// All Pending requests in elevator 4's queue
		Label requests4 = new Label("Pending Requests: ");
		pending4 = new TextArea();
		pending4.setEditable(false);
		pending4.setMaxWidth(screenWidth / 10);
		pending4.setStyle("-fx-opacity:1;");
		InfoElev4.add(requests4, 0, 4);
		InfoElev4.add(pending4, 1, 4);
		// ********************************END elevator4
		// *****************************************//

		// add all elevators to elevator section of GUI.
		elevators.add(Elevator1, 0, 0);
		elevators.add(Elevator2, 1, 0);
		elevators.add(Elevator3, 0, 1);
		elevators.add(Elevator4, 1, 1);

		// *****************************************************************************************************************//
		// ****************************************End of Elevator
		// Section***********************************************************//
		// *****************************************************************************************************************//

		// *****************************************************************************************************************//
		// ****************************************Floor
		// Buttons***********************************************************//
		// *****************************************************************************************************************//

		ScrollPane floors = new ScrollPane();
		VBox mainBranch = new VBox();
		HBox[] branch = new HBox[NUM_FLOORS];
		Label[] floorName = new Label[NUM_FLOORS];
		for (int i = NUM_FLOORS - 1; i >= 0; i--) {
			branch[i] = new HBox(2);
			branch[i].setMinHeight(45);

			branch[i].setStyle("-fx-padding: 1;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
					+ "-fx-border-insets: 1;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

			floorName[i] = new Label("Floor\n   " + (i + 1));
			branch[i].getChildren().add(floorName[i]);

			VBox requestButtons = new VBox();

			if (i != NUM_FLOORS - 1) {
				upDirectionButton[i] = new Label();
				ImageView temp = new ImageView(upRequest);
				temp.setFitWidth(screenWidth / 75);
				temp.setPreserveRatio(true);
				upDirectionButton[i].setGraphic(temp);
				requestButtons.getChildren().add(upDirectionButton[i]);
			}
			if (i != 0) {
				downDirectionButton[i] = new Label();
				ImageView temp = new ImageView(downRequest);
				temp.setFitWidth(screenWidth / 75);
				temp.setPreserveRatio(true);
				downDirectionButton[i].setGraphic(temp);
				requestButtons.getChildren().add(downDirectionButton[i]);
			}
			branch[i].getChildren().add(requestButtons);

			VBox[] doorsAndLamps = new VBox[NUM_ELEVATORS];

			for (int j = 0; j < NUM_ELEVATORS; j++) {
				doorsAndLamps[j] = new VBox(2);
				doorsAndLamps[j].setStyle("-fx-padding: 1;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
						+ "-fx-border-insets: 1;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

				HBox lamps = new HBox(2);
//				lamps.setStyle("-fx-padding: 5;" 
//								+ "-fx-border-style: solid inside;" 
//								+ "-fx-border-width: 2;"
//								+ "-fx-border-insets: 1;" 
//								+ "-fx-border-raidus: 5;" 
//								+ "-fx-border-color: grey;");

				if (i != 0) {
					ImageView temp = new ImageView(downDirection);
					temp.setFitWidth(screenWidth / 100);
					temp.setPreserveRatio(true);
					downDirectionLamp[i][j] = new Label();
					downDirectionLamp[i][j].setGraphic(temp);
					lamps.getChildren().add(downDirectionLamp[i][j]);
				}
				if (i != NUM_FLOORS - 1) {
					upDirectionLamp[i][j] = new Label();
					ImageView temp = new ImageView(upDirection);
					temp.setFitWidth(screenWidth / 100);
					temp.setPreserveRatio(true);
					upDirectionLamp[i][j].setGraphic(temp);
					lamps.getChildren().add(upDirectionLamp[i][j]);
				}

				cars[j][i] = new Rectangle();
				cars[j][i].setWidth(50);
				cars[j][i].setHeight(50);
				cars[j][i].setFill(Color.GREY);
				cars[j][i].setStroke(Color.DARKGREY);

				doorsL[j][i] = new Rectangle();
				doorsL[j][i].setWidth(2);
				doorsL[j][i].setHeight(50);
				doorsL[j][i].setFill(sahil);
				doorsL[j][i].setStroke(sahil);

				doorsR[j][i] = new Rectangle();
				doorsR[j][i].setWidth(2);
				doorsR[j][i].setHeight(50);
				doorsR[j][i].setFill(sahil);
				doorsR[j][i].setStroke(sahil);

				carsAndDoors[j][i] = new StackPane();
				carsAndDoors[j][i].getChildren().addAll(cars[j][i], doorsL[j][i], doorsR[j][i]);

				doorsAndLamps[j].getChildren().addAll(lamps, carsAndDoors[j][i]);

				doorsAndLamps[j].setStyle("-fx-padding: 1;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
						+ "-fx-border-insets: 1;" + "-fx-border-raidus: 5;" + "-fx-border-color: grey;");

				branch[i].getChildren().add(doorsAndLamps[j]);
			}

			mainBranch.getChildren().add(branch[i]);

		}

		floors.setContent(mainBranch);

		// *****************************************************************************************************************//
		// *******************************End of Floor
		// Buttons***********************************************************//
		// *****************************************************************************************************************//

		scheduler.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;" + "-fx-border-width: 1;"
				+ "-fx-border-insets: 2;" + "-fx-border-radius: 5;" + "-fx-border-color: grey;");

		scheduler.setMaxWidth(screenWidth / 1.5);
		scheduler.setMaxHeight(screenHeight / 10);
		scheduler.setEditable(false);

		VBox bottomPane = new VBox();
		bottomPane.setPadding(new Insets(5, 5, 5, 5));

		Label schedulerName = new Label("Scheduler Output: ");

		bottomPane.getChildren().add(schedulerName);
		bottomPane.getChildren().add(scheduler);

		root = new HBox(screenWidth / 25);

		VBox leftBranch = new VBox();
		leftBranch.getChildren().addAll(resetSystem(), elevators, bottomPane);
		ScrollPane rightBranch = new ScrollPane();
		rightBranch.setMinWidth(screenWidth / 4.25);
		rightBranch.setContent(floors);
		rightBranch.setVvalue(rightBranch.getVmax()); // start at floor 1 

		root.getChildren().addAll(leftBranch, rightBranch);
		// root.setBottom(bottomPane);

		// Set the Style-properties of the BorderPane
		root.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: grey;");


		Alert errorDialog = new Alert(AlertType.ERROR);
		errorDialog.setTitle("OH NO");
		errorDialog.setHeaderText("Oh no JavaFX error");
		errorDialog.getDialogPane().setMinWidth(screenWidth/4);
		StackPane sadPane = new StackPane();
		sadPane.getChildren().add(sad);
		VBox saddestPane = new VBox();
		sad.setFitWidth(screenWidth / 7);
		sad.setPreserveRatio(true);
		//sadPane.setAlignment(sad,Pos.CENTER);
		saddestPane.getChildren().addAll(sadPane, new Label("\n\nClick OK to exit"));
		errorDialog.getDialogPane().setContent(saddestPane);

		Handler globalExceptionHandler = new Handler(errorDialog);
		Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
		
		// Create the Scene
		Scene scene = new Scene(root);

		// Add the scene to the Stage
		stage.setScene(scene);
		// stage.setPo
		// title
		stage.setTitle("Elevator Simulation GUI");
		// Make Fullscreen
		stage.setMaximized(true);
		// cannot resize
		stage.setResizable(false);
		// display the stage
		stage.show();

		// clip.play(1.0);
		mediaPlayer.setAutoPlay(true);
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		mediaPlayer2.setAutoPlay(false);
		mediaPlayer2.setCycleCount(MediaPlayer.INDEFINITE);
		mediaPlayer3.setAutoPlay(false);
		mediaPlayer3.setCycleCount(MediaPlayer.INDEFINITE);
		faultMediaPlayer.setAutoPlay(false);
		mediaPlayer.play();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
				System.exit(0);
			}
		});
		

	}

//		  Scale scale = new Scale(SCALE_FACTOR, SCALE_FACTOR);  
//        scale.setPivotX(0);  
//        scale.setPivotY(0);  
//        root.getTransforms().setAll(scale);

	public void openDoors(int i, int j) {

		transitionL = new TranslateTransition();
		STransitionL = new ScaleTransition(Duration.millis(200 / speed), doorsL[i][j]);
		transitionL.setDuration(Duration.millis(200 / speed));
		transitionL.setNode(doorsL[i][j]);

		transitionR = new TranslateTransition();
		STransitionR = new ScaleTransition(Duration.millis(200 / speed), doorsR[i][j]);
		transitionR.setDuration(Duration.millis(200 / speed));
		transitionR.setNode(doorsR[i][j]);

		transitionL.setFromX(0);
		transitionL.setToX(-8);
		STransitionL.setByX(6);

		transitionL.setAutoReverse(true);
		transitionL.setCycleCount(2);
		STransitionL.setAutoReverse(true);
		STransitionL.setCycleCount(2);

		// transitionL.setFromX(0);
//		transitionL.setToX(8);
//		transitionL.setByX(0);

		transitionR.setFromX(0);
		transitionR.setToX(7);
		STransitionR.setByX(6);
		transitionR.setAutoReverse(true);
		transitionR.setCycleCount(2);
		STransitionR.setAutoReverse(true);
		STransitionR.setCycleCount(2);
		// transitionR.setFromX(0);
//		transitionR.setToX(-7);
//		STransitionR.setByX(0);

		transitionL.play();
		STransitionL.play();
		transitionR.play();
		STransitionR.play();
	}

	private void setUpDirLamp(int id, int floorNum, Status setting) {
		if (setting == Status.ON) {
			ImageView temp = new ImageView(litUpDirection);
			temp.setFitWidth(screenWidth / 100);
			temp.setPreserveRatio(true);
			upDirectionLamp[floorNum - 1][id - 1].setGraphic(temp);
		} else {
			ImageView temp = new ImageView(upDirection);
			temp.setFitWidth(screenWidth / 100);
			temp.setPreserveRatio(true);
			Platform.runLater(() -> {
				upDirectionLamp[floorNum - 1][id - 1].setGraphic(temp);
			});
		}
	}

	private void setDownDirLamp(int id, int floorNum, Status setting) {
		if (setting == Status.ON) {
			ImageView temp = new ImageView(litDownDirection);
			temp.setFitWidth(screenWidth / 100);
			temp.setPreserveRatio(true);
			Platform.runLater(() -> {
				downDirectionLamp[floorNum - 1][id - 1].setGraphic(temp);
			});
		} else {
			ImageView temp = new ImageView(downDirection);
			temp.setFitWidth(screenWidth / 100);
			temp.setPreserveRatio(true);
			downDirectionLamp[floorNum - 1][id - 1].setGraphic(temp);
		}
	}

	private void setUpReqButton(int floorNum, boolean setting) {
		if (setting) {
			ImageView temp = new ImageView(litUpRequest);
			temp.setFitWidth(screenWidth / 75);
			temp.setPreserveRatio(true);
			upDirectionButton[floorNum - 1].setGraphic(temp);
		} else {
			ImageView temp = new ImageView(upRequest);
			temp.setFitWidth(screenWidth / 75);
			temp.setPreserveRatio(true);
			upDirectionButton[floorNum - 1].setGraphic(temp);

		}
	}

	private void setDownReqButton(int floorNum, boolean setting) {
		if (setting) {
			ImageView temp = new ImageView(litDownRequest);
			temp.setFitWidth(screenWidth / 75);
			temp.setPreserveRatio(true);
			downDirectionButton[floorNum - 1].setGraphic(temp);

		} else {
			ImageView temp = new ImageView(downRequest);
			temp.setFitWidth(screenWidth / 75);
			temp.setPreserveRatio(true);
			downDirectionButton[floorNum - 1].setGraphic(temp);
		}
	}

	// set panels to be public
	private void setCarButton(int id, int floor, boolean setting) {
		int col = floor % 4;
		int row = floor / 4;
		if (col != 0) {
			col--;
		} else {
			col = 3;
		}

		if (row != 0) {
			if (floor % 4 == 0) {
				row--;
			}
		}

		if (id == 1) {
			if (setting) {
				panel1[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_Lit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			} else {
				panel1[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			}

		} else if (id == 2) {
			if (setting) {
				panel2[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_Lit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			} else {
				panel2[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			}
		} else if (id == 3) {
			if (setting) {
				panel3[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_Lit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			} else {
				panel3[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			}
		} else {
			if (setting) {
				panel4[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_Lit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			} else {
				panel4[col][row].setStyle("-fx-background-image: url(elevatorSystem/GUIApplication/icons/E_NotLit.png);"
						+ "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;"
						+ "-fx-background-size: 35px, 35px;" + "-fx-background-radius:35px; " + "-fx-min-width: 35px; "
						+ "-fx-min-height: 35px; " + "-fx-max-width: 35px; " + "-fx-max-height: 35px;");
			}
		}
	}

	private HBox resetSystem() {

		testFile.setTooltip(new Tooltip("Press me to view input"));
		testFile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				Alert helpDialog = new Alert(AlertType.INFORMATION);
				helpDialog.setTitle("Input");
				helpDialog.setHeaderText("The elevator system has the following input:");
				helpDialog.setContentText(file_contents);
				helpDialog.show();
			}
		});

		String[] files = { "", 
				"text_files/inputs/input3.txt", "text_files/inputs/input4.txt", "text_files/inputs/input5.txt", "text_files/inputs/input6.txt", "text_files/inputs/input7.txt",
				"text_files/inputs/newInput.txt" };

		/*
		 * Choice box for selecting which input/test case
		 */
		cb = new ChoiceBox<String>(
				FXCollections.observableArrayList("", "Input3", "Input4", "Input5", "Input6","Input7" ,"New"));
		cb.setDisable(true);
		cb.setCursor(Cursor.DEFAULT);
		cb.setTooltip(new Tooltip("Select the input file"));
		cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			// @SuppressWarnings("rawtypes")
			public void changed(ObservableValue ov, Number value, Number new_value) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() {

						if (new_value.intValue() == 0) {
							// do nothing
						} else if (new_value.intValue() == files.length - 1) {
							cb.setDisable(true);
							/*
							 * Window for entering new input
							 */
							Platform.runLater(() -> {
//								
//							Task<Void> task3 = new Task<Void>() {
//								@Override
//								public Void call() {
								Dialog<String> dialog = new Dialog<String>();
								dialog.setTitle("Create new input");
								dialog.setHeaderText("Enter new input");
								dialog.setResizable(true);
								TextArea text1 = new TextArea("Time Floor FloorButton CarButton Fault");
								text1.setEditable(false);
								text1.setMaxHeight(10);
								TextArea text2 = new TextArea();
								Button helpButton = new Button("Help");
								helpButton.setTooltip(new Tooltip("Press me for help!"));
								helpButton.setOnAction(new EventHandler<ActionEvent>() {
									public void handle(ActionEvent t) {
										Alert helpDialog = new Alert(AlertType.INFORMATION);
										helpDialog.setTitle("Help");
										helpDialog.setHeaderText("Enter input in the following format:");
										helpDialog.setContentText("\t14:05:15.0 4 Up 8 1 \n" + "\t14:05:12.0 3 Down 1 1 \n"
												+ "\t14:05:40.0 1 Up 4 1\n\n" + "Note:\n" + "\tFault = 1 for no fault\n"
												+ "\tFault = 2 for car stuck between floors\n"
												+ "\tFault = 3 for car doors stuck open");
										helpDialog.showAndWait();
									}
								});

								VBox vBox = new VBox(2);
								vBox.getChildren().addAll(text1, text2, helpButton);
								dialog.getDialogPane().setContent(vBox);

								ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
								ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
								dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
								dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

								dialog.setResultConverter(new Callback<ButtonType, String>() {
									@Override
									public String call(ButtonType b) {

										if (b == buttonTypeOk) {
											if (text2.getText().equals("LOVE SNOWBOARDING")) {
												Platform.runLater(() -> {
													Image image = new Image("elevatorSystem/GUIApplication/icons/franks.png"); // pass
																																						// in
																																						// the
																																						// image
																																						// path
													root.setCursor(new ImageCursor(image));
												});
											} else if (text2.getText().equals("HATE SNOWBOARDING")) {
												Platform.runLater(() -> {
													cb.setCursor(Cursor.DEFAULT);
													cb2.setCursor(Cursor.DEFAULT);
													root.setCursor(Cursor.DEFAULT);
												});
											}
											elevatorSystem.newInputFile(files[new_value.intValue()], text2.getText());
										}
										return null;
									}
								});

								dialog.showAndWait();
								Task<Void> task2 = new Task<Void>() {
									@Override
									public Void call() {
										elevatorSystem.resetInput(files[new_value.intValue()], speed);
										return null;
									}
								};
								// });
								new Thread(task2).start();
								// });
								// return null;
								// }
								// };new Thread(task3).start();
							});
						} else {
							cb.setDisable(true);
							elevatorSystem.resetInput(files[new_value.intValue()], speed);
						}
						return null;
					}
				};
				new Thread(task).start();
			}
		});

		/*
		 * Select Speed first before input
		 */
		Double[] speeds = { 0.0, 0.25, 0.5, 1.0, 1.25, 1.5, 2.0 };
		cb2 = new ChoiceBox<String>(
				FXCollections.observableArrayList("", "0.25x", "0.5x", "1.0x", "1.25x", "1.5x", "2.0x"));
		cb2.setTooltip(new Tooltip("Select the speed"));
		cb2.setCursor(Cursor.DEFAULT);

		cb2.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@SuppressWarnings("rawtypes")
			public void changed(ObservableValue ov, Number value, Number new_value) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() {
						if (new_value.intValue() == 0) {
							// do nothing
						} else {
							cb2.setDisable(true);
							speed = speeds[new_value.intValue()];
							cb.setDisable(false);
						}
						return null;
					}
				};
				new Thread(task).start();
			}
		});

		Slider volumeSlider = new Slider();
		volumeSlider.setValue(100);
		volumeSlider.valueProperty().addListener(new InvalidationListener() {

			public void invalidated(javafx.beans.Observable ov) {
				if (volumeSlider.isValueChanging()) {
					mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
					mediaPlayer2.setVolume(volumeSlider.getValue() / 100.0);
					mediaPlayer3.setVolume(volumeSlider.getValue() / 100.0);
					faultMediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
				}
			}
		});

		ImageView music_icon = new ImageView("elevatorSystem/GUIApplication/icons/music_icon.png");
		music_icon.setFitWidth(screenWidth / 75);
		music_icon.setPreserveRatio(true);
		Button changeMusic = new Button("", music_icon);
		changeMusic.setTooltip(new Tooltip("Press me to change music!"));
		changeMusic.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				if (mediaPlaying == MediaPlaying.MEDIA1) {
					mediaPlayer.stop();
					mediaPlayer2.play();
					mediaPlaying = MediaPlaying.MEDIA2;
				}else if(mediaPlaying == MediaPlaying.MEDIA2) {
					mediaPlayer2.stop();
					mediaPlayer3.play();
					mediaPlaying = MediaPlaying.MEDIA3;
				}
				else if (mediaPlaying == MediaPlaying.MEDIA3){
					mediaPlayer3.stop();
					mediaPlayer.play();
					mediaPlaying = MediaPlaying.MEDIA1;
				}
			}
		});
		HBox top = new HBox();
		top.getChildren().addAll(new Label("Select speed: "), cb2, new Label("\tSelect input file: "), cb,
				new Label("\t"), testFile, new Label("\tMusic Control:"), volumeSlider, new Label("\t"), changeMusic);
		top.setPadding(new Insets(5, 5, 5, 5));

		return top;

	}

	public static void main(String args[]) {
		launch(args);
	}

	private void setCarLocation(int elevatorID, int floor, boolean working) {
		cars[elevatorID - 1][carLocation[elevatorID - 1]].setFill(Color.GREY);
		carLocation[elevatorID - 1] = floor - 1;
		if (working) {
			cars[elevatorID - 1][carLocation[elevatorID - 1]].setFill(Color.SPRINGGREEN);
		} else {
			cars[elevatorID - 1][carLocation[elevatorID - 1]].setFill(Color.RED);

		}
	}

	private void setWorking(int elevatorID, boolean working) {
		if (working) {
			cars[elevatorID - 1][carLocation[elevatorID - 1]].setFill(Color.SPRINGGREEN);
		} else {
			cars[elevatorID - 1][carLocation[elevatorID - 1]].setFill(Color.RED);
			faultMediaPlayer.play();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {
				if (o instanceof ElevatorStatus && arg instanceof GUIPacket) {
					GUIPacket received = (GUIPacket) arg;
					ElevatorStatus status = (ElevatorStatus) received.getObject();
					int floorNumber = status.getElevatorFloor();
					if (status.getID() == 1) {
						if (received.getCode() == VariableChangedCode.ELEVATOR_FLOOR) {
							boolean workingStatus = status.isWorking();
							currentFloorNum1.setText(floorNumber + "");
							Platform.runLater(() -> {
								setCarLocation(1, floorNumber, workingStatus);
							});
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_DIRECTION) {
							direction1.setText(status.getElevatorDirection() + "");
							if (status.getElevatorDirection() == StringData.IDLE) {
								yesOrNo1.setText("Stopped");
							} else {
								yesOrNo1.setText("Moving");
							}
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_WORKING) {
							status1.setText(status.isWorking() + "");
							if (!status.isWorking()) {
								yesOrNo1.setText("Stuck");
								Platform.runLater(() -> {
									setWorking(1, false);
								});
							} else {
								if (status.getElevatorDirection() == StringData.IDLE) {
									yesOrNo1.setText("Stopped");
								} else {
									yesOrNo1.setText("Moving");
								}
								Platform.runLater(() -> {
									setWorking(1, true);
								});
							}
						} 
					}
					if (status.getID() == 2) {
						if (received.getCode() == VariableChangedCode.ELEVATOR_FLOOR) {
							boolean workingStatus = status.isWorking();
							currentFloorNum2.setText(floorNumber + "");
							Platform.runLater(() -> {
								setCarLocation(2, floorNumber, workingStatus);
							});
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_DIRECTION) {
							direction2.setText(status.getElevatorDirection() + "");
							if (status.getElevatorDirection() == StringData.IDLE) {
								yesOrNo2.setText("Stopped");
							} else {
								yesOrNo2.setText("Moving");
							}
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_WORKING) {
							status2.setText(status.isWorking() + "");
							if (!status.isWorking()) {
								yesOrNo2.setText("Stuck");
								Platform.runLater(() -> {
									setWorking(2, false);
								});
							} else {
								if (status.getElevatorDirection() == StringData.IDLE) {
									yesOrNo2.setText("Stopped");
								} else {
									yesOrNo2.setText("Moving");
								}
								Platform.runLater(() -> {
									setWorking(2, true);
								});
							}
						} 
					}
					if (status.getID() == 3) {
						if (received.getCode() == VariableChangedCode.ELEVATOR_FLOOR) {
							boolean workingStatus = status.isWorking();
							currentFloorNum3.setText(floorNumber + "");
							Platform.runLater(() -> {
								setCarLocation(3, floorNumber, workingStatus);
							});
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_DIRECTION) {
							direction3.setText(status.getElevatorDirection() + "");
							if (status.getElevatorDirection() == StringData.IDLE) {
								yesOrNo3.setText("Stopped");
							} else {
								yesOrNo3.setText("Moving");
							}
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_WORKING) {
							status3.setText(status.isWorking() + "");
							if (!status.isWorking()) {
								yesOrNo3.setText("Stuck");
								Platform.runLater(() -> {
									setWorking(3, false);
								});
							} else {
								if (status.getElevatorDirection() == StringData.IDLE) {
									yesOrNo3.setText("Stopped");
								} else {
									yesOrNo3.setText("Moving");
								}
								Platform.runLater(() -> {
									setWorking(3, true);
								});
							}

						}
					}
					if (status.getID() == 4) {
						if (received.getCode() == VariableChangedCode.ELEVATOR_FLOOR) {
							boolean workingStatus = status.isWorking();
							currentFloorNum4.setText(floorNumber + "");
							Platform.runLater(() -> {
								setCarLocation(4, floorNumber, workingStatus);
							});
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_DIRECTION) {
							direction4.setText(status.getElevatorDirection() + "");
							if (status.getElevatorDirection() == StringData.IDLE) {
								yesOrNo4.setText("Stopped");
							} else {
								yesOrNo4.setText("Moving");
							}
						} else if (received.getCode() == VariableChangedCode.ELEVATOR_WORKING) {
							status4.setText(status.isWorking() + "");
							if (!status.isWorking()) {
								yesOrNo4.setText("Stuck");
								Platform.runLater(() -> {
									setWorking(4, false);
								});
							} else {
								if (status.getElevatorDirection() == StringData.IDLE) {
									yesOrNo4.setText("Stopped");
								} else {
									yesOrNo4.setText("Moving");
								}
								Platform.runLater(() -> {
									setWorking(4, true);
								});
							}
						}
					}

				} else if (o instanceof AddNewDestination || o instanceof SelectElevator || o instanceof FaultHandler
						|| o instanceof ArrivedAtFloor) {
					String floorsRequested1 = "";
					String floorsRequested2 = "";
					String floorsRequested3 = "";
					String floorsRequested4 = "";
					GUIPacket received = (GUIPacket) arg;
					ElevatorStatus status = (ElevatorStatus) received.getObject();
					if (received.getCode() == VariableChangedCode.ELEVATOR_FLOORSTOVISIT) {
						if (status.getID() == 1) {
							floorsRequested1 = "";
							for (int i = 0; i < status.getFloorsToVisit().size(); i++) {
								floorsRequested1 += "[" + status.getFloorsToVisit().get(i).getFloorToService() + "] ";
								if (status.getFloorsToVisit().get(i).getDestinationFloor() != -1)
									floorsRequested1 += "[" + status.getFloorsToVisit().get(i).getDestinationFloor() + "] ";
							}

							pending1.setText(floorsRequested1);

						} else if (status.getID() == 2) {
							floorsRequested2 = "";

							for (int i = 0; i < status.getFloorsToVisit().size(); i++) {
								floorsRequested2 += "[" + status.getFloorsToVisit().get(i).getFloorToService() + "] ";
								if (status.getFloorsToVisit().get(i).getDestinationFloor() != -1)
									floorsRequested2 += "[" + status.getFloorsToVisit().get(i).getDestinationFloor() + "] ";
							}
							pending2.setText(floorsRequested2);

						} else if (status.getID() == 3) {
							floorsRequested3 = "";
							for (int i = 0; i < status.getFloorsToVisit().size(); i++) {
								floorsRequested3 += "[" + status.getFloorsToVisit().get(i).getFloorToService() + "] ";
								if (status.getFloorsToVisit().get(i).getDestinationFloor() != -1)
									floorsRequested3 += "[" + status.getFloorsToVisit().get(i).getDestinationFloor() + "] ";
							}

							pending3.setText(floorsRequested3);
						} else {
							floorsRequested4 = "";
							for (int i = 0; i < status.getFloorsToVisit().size(); i++) {
								floorsRequested4 += "[" + status.getFloorsToVisit().get(i).getFloorToService() + "] ";
								if (status.getFloorsToVisit().get(i).getDestinationFloor() != -1)
									floorsRequested4 += "[" + status.getFloorsToVisit().get(i).getDestinationFloor() + "] ";
							}
							pending4.setText(floorsRequested4);
						}

					}
				} else if (o instanceof InputFileReader) {
					if (arg instanceof String) {
						String fileContents = (String) arg;
						file_contents = fileContents;
					} else if (arg instanceof Boolean) {
						Boolean failure = (Boolean) arg;
						if (failure) {
							Platform.runLater(() -> {
								Alert invalidAlert = new Alert(Alert.AlertType.ERROR);
								invalidAlert.setHeaderText("Invalid input");
								invalidAlert.setContentText("Choose another input file.");
								invalidAlert.showAndWait();
								file_contents = "";
								cb2.setDisable(false);
								cb.getSelectionModel().selectFirst();
								cb2.getSelectionModel().selectFirst();
							});
						}
					}
				} else if (o instanceof Scheduler) {
					if (arg instanceof Boolean) {
						Boolean simDone = (Boolean) arg;
						if (simDone) {
							Platform.runLater(() -> {
								Alert doneAlert = new Alert(Alert.AlertType.INFORMATION);
								doneAlert.setHeaderText("Done");
								doneAlert.setContentText("All requests serviced. Choose another input file.");
								doneAlert.showAndWait();
								cb2.setDisable(false);
								cb.getSelectionModel().selectFirst();
								cb2.getSelectionModel().selectFirst();
							});
						}
					} else if (arg instanceof GUIPacket) {
						GUIPacket received = (GUIPacket) arg;
						if (received.getCode() == VariableChangedCode.ELEVATOR_SCHEDULERLIST) {
							scheduler.setText(received.getString());
						}
					}
				}

				else if (o instanceof elevatorSystem.elevatorSubsystem1.helpers.Elevator) {
					if (arg instanceof GUIPacket) {
						GUIPacket received = (GUIPacket) arg;

						if (received.getCode() == VariableChangedCode.ELEVATOR_DOORS) {

							int doors_floor = received.getCurrentFloor();
							Platform.runLater(() -> {
								openDoors(0, doors_floor - 1);
							});

						} else {
							elevatorSystem.elevatorSubsystem1.helpers.Elevator status = (elevatorSystem.elevatorSubsystem1.helpers.Elevator) received
									.getObject();
							int floorDestButton = received.getNum();
							boolean buttonOn = (status.getButtonLamps()[floorDestButton - 1] == Status.ON);
							Platform.runLater(() -> {
								setCarButton(1, floorDestButton, buttonOn);
							});
						}
					}
				} else if (o instanceof elevatorSystem.elevatorSubsystem2.helpers.Elevator) {
					if (arg instanceof GUIPacket) {

					}
					GUIPacket received = (GUIPacket) arg;
					if (received.getCode() == VariableChangedCode.ELEVATOR_DOORS) {

						int doors_floor = received.getCurrentFloor();
						Platform.runLater(() -> {
							openDoors(1, doors_floor - 1);
						});

					} else {
						elevatorSystem.elevatorSubsystem2.helpers.Elevator status = (elevatorSystem.elevatorSubsystem2.helpers.Elevator) received
								.getObject();
						int floorDestButton = received.getNum();
						boolean buttonOn = (status.getButtonLamps()[floorDestButton - 1] == Status.ON);
						Platform.runLater(() -> {
							setCarButton(2, floorDestButton, buttonOn);
						});
					}

				} else if (o instanceof elevatorSystem.elevatorSubsystem3.helpers.Elevator) {
					if (arg instanceof GUIPacket) {
						GUIPacket received = (GUIPacket) arg;
						if (received.getCode() == VariableChangedCode.ELEVATOR_DOORS) {
							int doors_floor = received.getCurrentFloor();
							Platform.runLater(() -> {
								openDoors(2, doors_floor - 1);
							});

						} else {
							elevatorSystem.elevatorSubsystem3.helpers.Elevator status = (elevatorSystem.elevatorSubsystem3.helpers.Elevator) received
									.getObject();
							int floorDestButton = received.getNum();
							boolean buttonOn = (status.getButtonLamps()[floorDestButton - 1] == Status.ON);
							Platform.runLater(() -> {
								setCarButton(3, floorDestButton, buttonOn);
							});
						}
					}
				} else if (o instanceof elevatorSystem.elevatorSubsystem4.helpers.Elevator) {
					if (arg instanceof GUIPacket) {
						GUIPacket received = (GUIPacket) arg;
						if (received.getCode() == VariableChangedCode.ELEVATOR_DOORS) {
							int doors_floor = received.getCurrentFloor();
							Platform.runLater(() -> {
								openDoors(3, doors_floor - 1);
							});

						} else {
							elevatorSystem.elevatorSubsystem4.helpers.Elevator status = (elevatorSystem.elevatorSubsystem4.helpers.Elevator) received
									.getObject();
							int floorDestButton = received.getNum();
							boolean buttonOn = (status.getButtonLamps()[floorDestButton - 1] == Status.ON);
							Platform.runLater(() -> {
								setCarButton(4, floorDestButton, buttonOn);
							});
						}
					}
				} else if (o instanceof Floor && arg instanceof GUIPacket_Floor) {
					GUIPacket_Floor received = (GUIPacket_Floor) arg;
					Floor status = (Floor) received.getObject();
					Status setting = received.getStatus();
					int floorNum = status.getFloorNum();
					int carId = received.getElevatorID();
					if (received.getCode() == LampChangedCode.UP_ARROW) {
						Platform.runLater(() -> {
							setUpDirLamp(carId, floorNum, setting);
						});
					} else if (received.getCode() == LampChangedCode.DOWN_ARROW) {
						if (setting == Status.OFF) {
						}
						Platform.runLater(() -> {
							setDownDirLamp(carId, floorNum, setting);
						});
					} else if (received.getCode() == LampChangedCode.REQ_UP) {
						boolean lampStatus = status.getUpRequestLamp() == Status.ON;
						Platform.runLater(() -> {
							setUpReqButton(floorNum, lampStatus);
						});
					} else if (received.getCode() == LampChangedCode.REQ_DOWN) {
						boolean lampStatus = status.getDownRequestLamp() == Status.ON;
						Platform.runLater(() -> {
							setDownReqButton(floorNum, lampStatus);
						});
					}

				}
				return null;
			}
		};

		new Thread(task).start();
	}
}

class Handler implements Thread.UncaughtExceptionHandler {
	Alert errorDialog;
	Handler(Alert toShow){
		this.errorDialog = toShow;
	}

	public void uncaughtException(Thread t, Throwable e) {
		//Platform.runLater(() -> {
			
			errorDialog.showAndWait();

			Platform.exit();
			System.exit(0);
		//});

	}
}