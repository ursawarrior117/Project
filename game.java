/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// package Main;

/**
 *
 * @author Hải Đoàn
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.lang.InterruptedException;
class Game
    extends Object {

  //bat su kien Start,Resume,pause,Finish,GameOver,level,score
  private SquareBoard board = null;

  private SquareBoard previewBoard = new SquareBoard(5, 5);

  private Figure[] figures = {
      new Figure(Figure.SQUARE_FIGURE),
      new Figure(Figure.LINE_FIGURE),
      new Figure(Figure.S_FIGURE),
      new Figure(Figure.Z_FIGURE),
      new Figure(Figure.RIGHT_ANGLE_FIGURE),
      new Figure(Figure.LEFT_ANGLE_FIGURE),
      new Figure(Figure.TRIANGLE_FIGURE),
//      new Figure(Figure.L_FIGURE),
      new Figure(Figure.POINTS_FIGURE),
	  new Figure(Figure.NEW_FIGURE),
  };

  private GamePanel component = null;

  private GameThread thread = null;

  private int level = 1;

  private int score = 0;
  
  private int times =0;

  private Figure figure = null;

  private Figure nextFigure = null;
  
  private int nextRotation = 0;
  
  private int h = 0, m = 0, s = 0;
  
  private Calendar now = Calendar.getInstance();

  private boolean preview = true;

  private boolean moveLock = false;
  
  private JTextField txtTime = new JTextField(5);
  
  private JPanel content = new JPanel();
  
  public Game() 
  {
    this(10, 20);
  }
  public Game(int width, int height) {
    board = new SquareBoard(width, height);
    board.setMessage("HELLO !!");
    thread = new GameThread();
  }

  public void quit() {
    thread = null;
  }

  public Component getComponent() {
    if (component == null) {
      component = new GamePanel();
    }
    return component;
  }

  private void handleStart() {

    // Reset score and figures
    level = 1;
    score = 0;
    figure = null;
    nextFigure = randomFigure();//hinh tiep theo la bat ky
    nextFigure.rotateRandom();// chieu xoay hinh tiep theo la bat ky
    nextRotation = nextFigure.getRotation();

    // Reset components
    board.setMessage(null);
    board.clear();
    previewBoard.clear();
    handleLevelModification();
    handleScoreModification();
    component.button.setLabel("Pause");

    // Start game thread
    thread.reset();
  }

  //Game over
  private void handleGameOver() {

    // Stop game thred
    thread.setPaused(true);

    // Reset figures
    if (figure != null) {
      figure.detach();
    }
    figure = null;
    if (nextFigure != null) {
      nextFigure.detach();
    }
    nextFigure = null;

    // Handle components
    board.setMessage("MẤY THẰNG GÀ !!");
    component.button.setLabel("Start");
  }

  //ve dich va bat dau choi lai tu dau
  private void handleFinish() {

    // Stop game thred
    thread.setPaused(true);

    // Reset figures
    if (figure != null) {
      figure.detach();
    }
    figure = null;
    if (nextFigure != null) {
      nextFigure.detach();
    }
    nextFigure = null;

    // Handle components
    board.setMessage("Finish");
    component.button.setLabel("Start");
  }

  //Khi an button Pause
  private void handlePause() {
    thread.setPaused(true);
    board.setMessage("Paused");
    component.button.setLabel("Resume");
  }

  //khi an button Resume
  private void handleResume() {
    board.setMessage(null);
    component.button.setLabel("Pause");
    thread.setPaused(false);
  }

  //dat level len giao dien
  private void handleLevelModification() {
    component.levelLabel.setText("Level: " + level);
    thread.adjustSpeed();
  }

  //dat score len giao dien
  private void handleScoreModification() {
    component.scoreLabel.setText("Score: " + score);
  }
  //dat time len gd
/*  private void handleTimesModification(){
  	component.timesLabel.setText("Times:"+times);
  }
*/
  //bat dau choi
  private void handleFigureStart() {
    int rotation;

    // Move next figure to current
    figure = nextFigure;
    moveLock = false;
    rotation = nextRotation;
    nextFigure = randomFigure();
    nextFigure.rotateRandom();
    nextRotation = nextFigure.getRotation();

    // Handle figure preview
    if (preview) {
      previewBoard.clear();
      nextFigure.attach(previewBoard, true);
      nextFigure.detach();
    }

    // Attach figure to game board
    figure.setRotation(rotation);
    if (!figure.attach(board, false)) {
      previewBoard.clear();
      figure.attach(previewBoard, true);
      figure.detach();
      handleGameOver();
    }
  }

  //bat su kien tang cua diem,level,finish
  private void handleFigureLanded() {

    // Check and detach figure
    if (figure.isAllVisible()) {

    }
    else {
      handleGameOver();
      return;
    }
    figure.detach();
    figure = null;

    // Check for full lines or create new figure
    if (board.hasFullLines()) {
      int m = board.removeFullLines();
      // System.out.print(m);
      score = m * 100;

      if (score >= level * 500) {
        level = level + 1;
        this.handleLevelModification(); // update len giao dien
      }
      if (level == 10) {
        thread.setPaused(true);
        handleFinish();
        //component.button.setEnabled(false);
      }
      handleScoreModification();

      if (level < 20 && board.getRemovedLines() / 20 > level) {
        level = board.getRemovedLines() / 20;
        handleLevelModification();
      }
    }
    else {
      handleFigureStart();
    }
  }

  private synchronized void handleTimer() {
    if (figure == null) {
      handleFigureStart();
    }
    else if (figure.hasLanded()) {
      handleFigureLanded();
    }
    else {
      figure.moveDown();
    }
  }

  private synchronized void handleButtonPressed() {
    if (nextFigure == null) {
      handleStart();
    }
    else if (thread.isPaused()) {
      handleResume();
    }
    else {
      handlePause();
    }
  }

  //bat su kien ban phiem cho Button
  private synchronized void handleKeyEvent(KeyEvent e) {

    // Handle start, pause and resume
    if (e.getKeyCode() == KeyEvent.VK_P) {
      handleButtonPressed();
      return;
    }

    // Don't proceed if stopped or paused
    if (figure == null || moveLock || thread.isPaused()) {
      return;
    }

    // Handle remaining key events
    // cac su kien 
    //sang trai
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:// bat su kien di chuyen sang trai
        figure.moveLeft();//hinh di chuyen sang trai
        break;
	//sang phai
      case KeyEvent.VK_RIGHT:// phai
        figure.moveRight();
        break;
	//xuong
      case KeyEvent.VK_DOWN:// di chuyen xuong, neu dung moveAllDown thi se xuong luon dat
        figure.moveAllWayDown();
        break;
	//phim len + space = xoay hinh
      case KeyEvent.VK_UP:
      case KeyEvent.VK_SPACE:
        if (e.isControlDown()) {
          figure.rotateRandom();
        }
        else if (e.isShiftDown()) {
          figure.rotateClockwise();
        }
        else {
          figure.rotateCounterClockwise();
        }
        break;
	  case KeyEvent.VK_D:
	  	if (score <10000){
	  		score=score+100;
	  		handleScoreModification();
	  	}
	  break;
	  case KeyEvent.VK_A:
	  	if(level>1){
	  		level--;
	  		handleLevelModification();
	  	}
	  break;
      case KeyEvent.VK_S:
        if (level < 10) {
          level++;
          handleLevelModification();
        }
        break;

      case KeyEvent.VK_N:
        preview = !preview;
        if (preview && figure != nextFigure) {
          nextFigure.attach(previewBoard, true);
          nextFigure.detach();
        }
        else {
          previewBoard.clear();
        }
        break;
    }
  }

  private Figure randomFigure() {
    return figures[ (int) (Math.random() * figures.length)];
  }

////////////////////////////////////////////////////////////////
 private class GameThread
      extends Thread {
    /*cho gach chay theo toc do dinh san ,hay cho dung ...*/

    private boolean paused = true;

    public  int sleepTime = 500;

    public GameThread() {
    }

    public void reset() {
      adjustSpeed();
      setPaused(false);
      if (!isAlive()) {
        this.start();
      }
    }

    public boolean isPaused() {
      return paused;
    }

    public void setPaused(boolean paused) {
      this.paused = paused;
    }
	// toc do chay cua game
    public void adjustSpeed() {
      sleepTime = 4500 / (level + 5) - 250;//1s =1000;
      if (sleepTime < 50) {
        sleepTime = 50;
      }
    }

    public void run() {
      while (thread == this) {
        // Make the time step
        handleTimer();

        // Sleep for some time
        try {
          Thread.sleep(sleepTime);
        }
        catch (InterruptedException ignore) {
          // Do nothing
        }

        // Sleep if paused
        while (paused && thread == this) {
          try {
            Thread.sleep(1000);
          }
          catch (InterruptedException ignore) {
            // Do nothing
          }
        }
      }
    }
  }

  ///////////////////////////////////////////////////////////////
  private class GamePanel
      extends Container {
    //tao giao dien cho Game

    private Dimension size = null;

    private Label scoreLabel = new Label("Score: 0");
   
    private Label TextClockLabel= new Label("Time: 00:00:00");

    private Label levelLabel = new Label("Level: 1");

    private JButton button = new JButton("Start");

    public GamePanel() {
      super();
      initComponents();
    }

    public void paint(Graphics g) {
      Rectangle rect = g.getClipBounds();

      if (size == null || !size.equals(getSize())) {
        size = getSize();
        resizeComponents();
      }
      g.setColor(getBackground());
      g.fillRect(rect.x, rect.y, rect.width, rect.height);
      super.paint(g);
    }

    private void initComponents() {
      GridBagConstraints c;

      // Set layout manager and background
      setLayout(new GridBagLayout());
      setBackground(Configuration.getColor("background", "#ffffcc"));

      // Add game board
      c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.gridheight = 4;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.fill = GridBagConstraints.BOTH;
      this.add(board.getComponent(), c);

      // Add next figure board
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 0.2;
      c.weighty = 0.18;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(15, 15, 0, 15);
      this.add(previewBoard.getComponent(), c);

      // Add score label
      scoreLabel.setForeground(Configuration.getColor("label",
          "#000000"));
      scoreLabel.setAlignment(Label.CENTER);
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.3;
      c.weighty = 0.05;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(0, 15, 0, 15);
      this.add(scoreLabel, c);
	  
      // Add level label
      levelLabel.setForeground(Configuration.getColor("label",
          "#000000"));
      levelLabel.setAlignment(Label.CENTER);
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 0.3;
      c.weighty = 0.05;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(15, 15, 15, 15);
      this.add(levelLabel, c);
      
      // Add time label
      levelLabel.setForeground(Configuration.getColor("label",
          "#000000"));
      levelLabel.setAlignment(Label.CENTER);
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 0.3;
      c.weighty = 0.05;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(15, 15, 15, 15);
      this.add(TextClockLabel, c);
      
      // Add generic button
      button.setBackground(Configuration.getColor("button"," "));
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 3;
      c.weightx = 0.3;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.NORTH;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(15, 15, 15, 15);
      this.add(button, c);

      // Add event handling
      enableEvents(KeyEvent.KEY_EVENT_MASK);
      this.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          handleKeyEvent(e);
        }
      });
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleButtonPressed();
          component.requestFocus();
        }
      });
    }

    /*
      Resizes all the static components, and invalidates the
      current layout.
     */
    private void resizeComponents() {
      Dimension size = scoreLabel.getSize();
      Font font;
      int unitSize;

      // Calculate the unit size
      size = board.getComponent().getSize();
      size.width /= board.getBoardWidth();
      size.height /= board.getBoardHeight();
      if (size.width > size.height) {
        unitSize = size.height;
      }
      else {
        unitSize = size.width;
      }

      // Adjust font sizes
      font = new Font("SansSerif",
                      Font.BOLD,
                      3 + (int) (unitSize / 1.8));
      scoreLabel.setFont(font);
      levelLabel.setFont(font);
      font = new Font("SansSerif",
                      Font.PLAIN,
                      2 + unitSize / 2);
      button.setFont(font);

      // Invalidate layout
      scoreLabel.invalidate();
      levelLabel.invalidate();
      TextClockLabel.invalidate();
      button.invalidate();
    }
  }
}

