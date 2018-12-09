package application;

import Input.IOmanager;
import draw.GameScreen;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import logic.Animals;
import logic.Gamelogic;
import logic.Hunters;
import logic.ScoreCount;
import sharedObject.IRenderableHolder;


public class GameManager {
	
	private Stage mainStage;
	private Scene game;
	private AnchorPane root;
	private GameTimer gameTimer;
	private HBox timerBox;
	//private BalloonArray bArray;
	private PauseButton pauseButton;
//	private Thread t;
	public static boolean isPause = false;
	private ScoreCount scoreCount;
	private PauseSubscene pauseSubscene;
	private static boolean isGameOver = false;
	private AnimationTimer anime;
	private static int animalPrevSec = 0;
	private static int hunterPrevSec = 0;
	private static int accelPrevSec = 0;
	private GameScreen gScreen;
	private Gamelogic gLogic;
	
//	private final String BACKGROUND_PATH = ClassLoader.getSystemResource("images/c.jpg").toString();
//	private final String BACKGROUND_STYLE = "-fx-background-image: url(" + BACKGROUND_PATH + "); " 
//												+ "-fx-background-size: cover;";
	private final String CURSOR_PATH = ClassLoader.getSystemResource("images\\cursor_pointerFlat_shadow.png")
			.toString();
	
	// CONSTRUCTOR	
	public GameManager() {
		System.out.println("--------------- Game Stage ---------------");
		root = new AnchorPane();
		game = new Scene(root, 1366, 768);
		mainStage = new Stage();
		mainStage.setScene(game);
		gScreen = new GameScreen(1366, 768);
		gLogic = new Gamelogic();
		root.getChildren().add(gScreen);
		//createBackground();
		createTimer();
		createPauseButton();
		createScoreCount();
		createSubscene();
		customCursor();
		setKeyPress();
		start();
		replay();
	}
	
	public Stage getMainStage() {
		return mainStage;
	}
	
//	public void createBackground() {
//		root.setStyle(BACKGROUND_STYLE);
//	}
	
	public void createPauseButton() {
		pauseButton = new PauseButton();
		AnchorPane.setRightAnchor(pauseButton, 30.0);
		AnchorPane.setTopAnchor(pauseButton, 20.0);
		root.getChildren().add(pauseButton);		
	}
	
	public void createTimer() {
		gameTimer = new GameTimer();
		timerBox = gameTimer.getTimerBox();
		AnchorPane.setRightAnchor(timerBox, 90.0);
		AnchorPane.setTopAnchor(timerBox, 25.0);
		root.getChildren().add(timerBox);
	}
	
	public void createScoreCount() {
		scoreCount = new ScoreCount();
		AnchorPane.setRightAnchor(scoreCount, 30.0);
		AnchorPane.setTopAnchor(scoreCount, 70.0);
		root.getChildren().add(scoreCount);
	}
	
	public void createSubscene() {
		pauseSubscene = new PauseSubscene();
		VBox box = new VBox(10);
		ButtonGenerator replayButton = new ButtonGenerator("REPLAY");
		replayButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				hideSubscene();
				replay();
			}
		});
		ButtonGenerator menuButton = new ButtonGenerator("MENU");
		menuButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				Main.showMenuStage();
				Main.closeGameStage();
			}
		});
		ButtonGenerator exitButton = new ButtonGenerator("EXIT");
		exitButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				Platform.exit();
			}
		});
		box.getChildren().addAll(replayButton, menuButton, exitButton);
		box.setLayoutY(80);
		box.setLayoutX(400);
		SliderBar slide = new SliderBar();
		slide.setLayoutY(350);
		slide.setLayoutX(120);
		LabelGenerator pausing = new LabelGenerator("PAUSING");
		pausing.setLayoutX(60);
		pausing.setLayoutY(80);
		pausing.setStyle("-fx-text-fill: #000000; -fx-font-family: 'Joystix Monospace'; -fx-font-size: 50; ");
		pauseSubscene.getSubScenePane().getChildren().addAll(box, slide, pausing);
	}
	
	public void showSubscene() {
		root.getChildren().add(pauseSubscene);
		pauseSubscene.transitionIn();
	}
	
	public void hideSubscene() {
		Thread t = new Thread(() -> {
			try {
				pauseSubscene.transitionOut();
				Thread.sleep(300);
				Platform.runLater(() -> {
					root.getChildren().remove(pauseSubscene);					
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		});
		t.start();
	}
	
	public void createGameplay() {
		
		System.out.println("GAME START");
		anime = new AnimationTimer() {
			
			@Override
			public void handle(long arg0) {
				updateEntities(GameTimer.getSecond());
				gScreen.drawComponent();
				gLogic.logicUpdate();
				IRenderableHolder.getInstance().update();
				IOmanager.postupdate();
				scoreCount.update();
				if (isGameOver) { 
					gameOver();
				}
			}
			
		};
		anime.start();
		
	}
	
	private void updateEntities(int sec) {
		createHunters(sec);
		createAnimal(sec);
		//accelerate(sec);
		if(sec == 59) {
			animalPrevSec = 0;
			hunterPrevSec = 0;
			accelPrevSec = 0;
		}
	}
	
	private void createAnimal(int sec) {
		if(sec-animalPrevSec == 3) {
			System.out.println("addAnimal");
			double posX = gScreen.createRamdonPos();
			String aKey = gScreen.createAnimalsKey();
			Animals a = new Animals(posX, -(Animals.HEIGHT+20) , aKey);
			System.out.println(posX + " " + aKey + " " + a.getZ());
			gLogic.addNewObj(a); 
			animalPrevSec = sec;
		}
	}
	
	private void createHunters(int sec) {
		if(sec - hunterPrevSec == 2) {
			System.out.println("addHunter");
			double posX = gScreen.createRamdonPos();
			String hKey = gScreen.createHuntersKey();
			Hunters h = new Hunters(posX, -Hunters.HEIGHT, hKey);
			System.out.println(posX + " " + hKey + " " + h.getZ());
			gLogic.addNewObj(h);
			hunterPrevSec = sec;
		}
	}
	
	private void accelerate(int sec) {
		if(sec - accelPrevSec == 30) {
			accelPrevSec = sec;
		}
	}
		
	public void setKeyPress() {
		
		game.setOnKeyPressed((KeyEvent e)->{
			String code = e.getCode().toString();
			System.out.println(code);
			if(!IOmanager.getpressed())
				IOmanager.setTriggered(code, true);
			IOmanager.setPressed(true);
		});
		
		game.setOnKeyReleased((KeyEvent e)->{
			IOmanager.setPressed(false);
		});

		pauseButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				if (isPause) unpause();
				else pause();
			}
		});
	}
	
	public void pause() {
		isPause = true;
		anime.stop();
		gameTimer.pause();
		MediaManager.getInstance().pauseGamePath();
		showSubscene();
	}
	
	public void unpause() {
		isPause = false;
		anime.start();
		gameTimer.unpause();
		MediaManager.getInstance().resumeGamePath();
		hideSubscene();
	}
	
	public void start() {
		createGameplay();
		gameTimer.start();
	}
	
	public void replay() {
		isPause = false;
		isGameOver = false;
		gLogic.clear();
		IRenderableHolder.getInstance().clear();
		animalPrevSec = 0;
		hunterPrevSec = 0;
		accelPrevSec = 0;
		anime.start();
		pauseButton.restart();
		gameTimer.unpause();
		gameTimer.reset();
		scoreCount.resetScore();
	}
	
	public static boolean getGameOver() {
		return isGameOver;
	}
	
	public static void setGameOver() {
		isGameOver = true;
	}
	
	private void gameOver() {
		PauseSubscene gameOverSs = new PauseSubscene();
		root.getChildren().add(gameOverSs);
		gameOverSs.transitionIn();
		LabelGenerator gO = new LabelGenerator("GAME OVER");
		gO.setFont(new Font("Joystix Monospace", 48));
		gO.setAlignment(Pos.CENTER);
		gO.setPrefWidth(gameOverSs.getWidth());
		gO.setLayoutY(49);
		gameOverSs.getSubScenePane().getChildren().add(gO);
		LabelGenerator text_score = new LabelGenerator("Your Score Is");
		LabelGenerator score = new LabelGenerator("" + scoreCount.getScoreCount());
		text_score.setFont(new Font("Joystix Monospace", 40));
		text_score.setAlignment(Pos.CENTER);
		score.setAlignment(Pos.CENTER);
		text_score.setPrefWidth(gameOverSs.getWidth());
		text_score.setLayoutY(140);
		score.setFont(new Font("Joystix Monospace", 72));
		score.setPrefWidth(gameOverSs.getWidth());
		score.setLayoutY(189);
		gameOverSs.getSubScenePane().getChildren().addAll(text_score, score);
		
		ButtonGenerator againBtn = new ButtonGenerator("Again"); 
		againBtn.setLayoutX((gameOverSs.getWidth() / 2) - 272);
		againBtn.setLayoutY(310);
		againBtn.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				gameOverSs.transitionOut();
				replay();
			}
		});
		ButtonGenerator menuBtn = new ButtonGenerator("Menu");
		menuBtn.setLayoutX((gameOverSs.getWidth() / 2) + 20);
		menuBtn.setLayoutY(310);
		menuBtn.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				Main.showMenuStage();
				Main.closeGameStage();
			}
		});
		gameOverSs.getSubScenePane().getChildren().addAll(againBtn,menuBtn);
		
		anime.stop();
		gameTimer.pause();	
		pauseButton.setDisable(true);
	}
	
	protected void customCursor() {
		Image customCur = new Image(CURSOR_PATH);
		game.setCursor(new ImageCursor(customCur));
	}
	
}
