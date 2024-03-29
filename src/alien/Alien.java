package alien;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import alien.entities.AlienEntity;
import alien.entities.AlienShotEntity;
import alien.entities.Entity;
import alien.entities.ShipEntity;
import alien.entities.ShotEntity;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic.
 * <p>
 * Display management will consist of a loop that cycles round all
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 * <p>
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 *
 * @author Kevin Glass
 * @author Davide Pastore
 */
public class Alien extends Canvas {
    /**
     * The strategy that allows us to use accelerate page flipping
     */
    private BufferStrategy strategy;
    /**
     * True if the game is currently "running", i.e. the game loop is looping
     */
    private boolean gameRunning = true;
    /**
     * The list of all the entities that exist in our game
     */
    private ArrayList entities = new ArrayList();
    /**
     * The list of entities that need to be removed from the game this loop
     */
    private ArrayList removeList = new ArrayList();
    /**
     * The entity representing the player
     */
    private Entity ship;
    /**
     * The speed at which the player's ship should move (pixels/sec)
     */
    private double moveSpeed = 300;
    /**
     * The time at which last fired a shot
     */
    private long lastFire = 0;
    /**
     * The interval between our players shot (ms)
     */
    private long firingInterval = 500;
    /**
     * The number of aliens left on the screen
     */
    private int alienCount;
    /**
     * The second in which the game is running
     */
    private long second;
    /**
     * The frame rate
     */
    private int frameRate;
    /**
     * The frame rate per second
     */
    private int frameRatePerSecond;

    /**
     * The message to display which waiting for a key press
     */
    private String message = "";
    /**
     * True if we're holding up game play until a key has been pressed
     */
    private boolean waitingForKeyPress = true;


    /**
     * True if the left cursor key is currently pressed
     */
    private boolean leftPressed = false;
    /**
     * True if the right cursor key is currently pressed
     */
    private boolean rightPressed = false;
    /**
     * True if we are firing
     */
    private boolean firePressed = false;
    /**
     * True if game logic needs to be applied this loop, normally as a result of a game event
     */
    private boolean logicRequiredThisLoop = false;
    /**
     * True if game is paused
     */
    private boolean pausedGame = false;

    private int numberOfShots;
    private int trials = 5;
    private int scores[] = new int[5];
    private String accuracies[] = new String[5];

    /**
     * Construct our game and set it running.
     */
    public Alien() {
        // create a frame to contain our game
        JFrame container = new JFrame("Space Invaders 101");

        // get hold the content of the frame and set up the resolution of the game
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setLayout(null);

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, 800, 600);
        panel.add(this);

        // Tell AWT not to bother repainting our canvas since we're
        // going to do that our self in accelerated mode
        setIgnoreRepaint(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // add a key input system (defined below) to our canvas
        // so we can respond to key pressed
        addKeyListener(new KeyInputHandler(this));

        ControllerManager controllers = new ControllerManager();
        controllers.initSDLGamepad();

        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean rf = false;
                boolean lf = false;
                boolean ff = false;

                while (true) {
                    ControllerState currState = controllers.getState(0);
                    if (currState.isConnected) {
                        if (currState.dpadRight || currState.leftStickX > 0.8) {
                            setRightPressed(true);
                            rf = true;
                        } else if (currState.dpadLeft || currState.leftStickX < -0.8) {
                            setLeftPressed(true);
                            lf = true;
                        }else if(currState.a) {
                            setFirePressed(true);
                            ff = true;
                        } else {
                            if (rf) {
                                setRightPressed(false);
                                rf = false;
                            }
                            if(lf) {
                                setLeftPressed(false);
                                lf = false;
                            }
                            if(ff) {
                                setFirePressed(false);
                                ff = false;
                            }
                        }
                    }
                }
            }
        }.start();

        // request the focus so key events come to us
        requestFocus();

        // create the buffering strategy which will allow AWT
        // to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        Arrays.fill(scores, 0);
        Arrays.fill(accuracies, "");

        // initialise the entities in our game so there's something
        // to see at startup
        initEntities();
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    public void startGame() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        initEntities();

        // blank out any keyboard settings we might currently have
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
    }

    /**
     * Initialise the starting state of the entities (ship and aliens). Each
     * entitiy will be added to the overall list of entities in the game.
     */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipEntity(this, "images/ship.gif", 370, 550);
        entities.add(ship);

        numberOfShots = 0;

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        for (int row = 0; row < 5; row++) {
            for (int x = 0; x < 12; x++) {
                String[] sprites = new String[]{
                        "images/alien.gif",
                        "images/alien1.gif"
                };
                Entity alien = new AlienEntity(this, sprites, 100 + (x * 50), (50) + row * 30);
                entities.add(alien);
                alienCount++;
            }
        }
    }

    /**
     * Notification from a game entity that the logic of the game
     * should be run at the next opportunity (normally as a result of some
     * game event)
     */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    /**
     * Remove an entity from the game. The entity removed will
     * no longer move or be drawn.
     *
     * @param entity The entity that should be removed
     */
    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    public int[] getScores() {
        return scores;
    }

    public String[] getAccuracies() {
        return accuracies;
    }

    public boolean trialDone() {
        return trials < 1;
    }

    private void handleRound(String msg) {
        System.out.println("trial remained: " + trials);
        DecimalFormat _numberFormat = new DecimalFormat("#0.0");
        scores[5 - trials] = 60 - alienCount;
        accuracies[5 - trials] = _numberFormat.format(100 * scores[5 - trials] / (double) numberOfShots);
        trials--;
        message = trialDone() ? "Trials done. Exit?" : msg;
        message += " (Score: " + scores[5 - trials - 1];
        message += " Accuracy: " + accuracies[5 - trials - 1] + "%)";
        waitingForKeyPress = true;
    }

    /**
     * Notification that the player has died.
     */
    public void notifyDeath() {
        handleRound("Oh no! They got you, next trial?");
    }

    /**
     * Notification that the player has won since all the aliens
     * are dead.
     */
    public void notifyWin() {
        handleRound("Well done!, next trial?");
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled() {
        // reduce the alient count, if there are none left, the player has won!
        alienCount--;

        if (alienCount == 0) {
            notifyWin();
        }

        // if there are still some aliens left then they all need to get faster, so
        // speed up all the existing aliens
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = (Entity) entities.get(i);

            if (entity instanceof AlienEntity) {
                // speed up by 2%
                entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02);
            }
        }
    }

    /**
     * Attempt to fire a shot from the player. Its called "try"
     * since we must first check that the player can fire at this
     * point, i.e. has he/she waited long enough between shots
     */
    public void tryToFire() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(this, "images/shot.gif", ship.getX() + 10, ship.getY() - 30);
        numberOfShots++;
        entities.add(shot);
    }

    /**
     * Attempt to fire a shot from all the aliens. Its called "try"
     * since we must first check that the alien can fire at this
     * point, i.e. probabilistic calculation
     */
    private void tryToFireAlien() {
        Random random = new Random();
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = (Entity) entities.get(i);

            if (entity instanceof AlienEntity) {

                if (random.nextInt(1001) == 1000) {
                    AlienShotEntity shot = new AlienShotEntity(this, "images/alienshot.gif", entity.getX() + 15, entity.getY() + 20);
                    entities.add(shot);
                }
            }
        }

    }

    /**
     * Set the frame rate.
     *
     * @param currentTimeMillis the current milliseconds time.
     */
    public void handleFrameRate(long currentTimeMillis, Graphics2D g) {
        if (currentTimeMillis / 1000 != second) {
            frameRatePerSecond = frameRate;

            // reload the frame rate and second
            frameRate = 1;
            second = currentTimeMillis / 1000;
        } else {
            frameRate++;
        }
    }

    /**
     * Draw the frame rate.
     *
     * @param g
     */
    public void drawFrameRate(Graphics2D g) {
        g.setColor(Color.white);
        g.drawString("FPS: " + frameRatePerSecond, 10, 10);
    }

    /**
     * The main game loop. This loop is running during all game
     * play as is responsible for the following activities:
     * <p>
     * - Working out the speed of the game loop to update moves
     * - Moving the game entities
     * - Drawing the screen contents (entities, text)
     * - Updating game events
     * - Checking Input
     * <p>
     */
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();
        second = lastLoopTime / 1000;

        // keep looping round til the game ends
        while (gameRunning) {
            // work out how long its been since the last update, this
            // will be used to calculate how far the entities should
            // move this loop
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // Get hold of a graphics context for the accelerated
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, 800, 600);

            //Check FPS
            handleFrameRate(lastLoopTime, g);

            if (!pausedGame) {
                // cycle round asking each entity to move itself
                if (!waitingForKeyPress) {
                    for (int i = 0; i < entities.size(); i++) {
                        Entity entity = (Entity) entities.get(i);

                        entity.move(delta);
                    }
                }

                // cycle round drawing all the entities we have in the game
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);

                    entity.draw(g);
                }

                // brute force collisions, compare every entity against
                // every other entity. If any of them collide notify
                // both entities that the collision has occured
                for (int p = 0; p < entities.size(); p++) {
                    for (int s = p + 1; s < entities.size(); s++) {
                        Entity me = (Entity) entities.get(p);
                        Entity him = (Entity) entities.get(s);

                        if (me.collidesWith(him)) {
                            me.collidedWith(him);
                            him.collidedWith(me);
                        }
                    }
                }

                // draw frame rate
                drawFrameRate(g);

                // remove any entity that has been marked for clear up
                entities.removeAll(removeList);
                removeList.clear();

                // if a game event has indicated that game logic should
                // be resolved, cycle round every entity requesting that
                // their personal logic should be considered.
                if (logicRequiredThisLoop) {
                    for (int i = 0; i < entities.size(); i++) {
                        Entity entity = (Entity) entities.get(i);
                        entity.doLogic();
                    }

                    logicRequiredThisLoop = false;
                }

                // if we're waiting for an "any key" press then draw the
                // current message
                if (waitingForKeyPress) {
                    g.setColor(Color.white);
                    g.drawString(message, (800 - g.getFontMetrics().stringWidth(message)) / 2, 250);
                    g.drawString("Press any key", (800 - g.getFontMetrics().stringWidth("Press any key")) / 2, 300);
                }

                // finally, we've completed drawing so clear up the graphics
                // and flip the buffer over
                g.dispose();
                strategy.show();

                // resolve the movement of the ship. First assume the ship
                // isn't moving. If either cursor key is pressed then
                // update the movement appropraitely
                ship.setHorizontalMovement(0);

                if ((leftPressed) && (!rightPressed)) {
                    ship.setHorizontalMovement(-moveSpeed);
                } else if ((rightPressed) && (!leftPressed)) {
                    ship.setHorizontalMovement(moveSpeed);
                }

                // if we're pressing fire, attempt to fire
                if (firePressed) {
                    tryToFire();
                }

                if (!waitingForKeyPress) {
                    tryToFireAlien();
                }
            } else {
                g.setColor(Color.white);
                g.drawString("Game Paused", (800 - g.getFontMetrics().stringWidth("Press P to return")) / 2, 300);

                // finally, we've completed drawing so clear up the graphics
                // and flip the buffer over
                g.dispose();
                strategy.show();
            }

            // finally pause for a bit. Note: this should run us at about
            // 100 fps but on windows this might vary each loop due to
            // a bad implementation of timer
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    /**
     * @return the waitingForKeyPress
     */
    public boolean isWaitingForKeyPress() {
        return waitingForKeyPress;
    }

    /**
     * @param waitingForKeyPress the waitingForKeyPress to set
     */
    public void setWaitingForKeyPress(boolean waitingForKeyPress) {
        this.waitingForKeyPress = waitingForKeyPress;
    }

    /**
     * @return the leftPressed
     */
    public boolean isLeftPressed() {
        return leftPressed;
    }

    /**
     * @param leftPressed the leftPressed to set
     */
    public void setLeftPressed(boolean leftPressed) {
        this.leftPressed = leftPressed;
    }

    /**
     * @return the rightPressed
     */
    public boolean isRightPressed() {
        return rightPressed;
    }

    /**
     * @param rightPressed the rightPressed to set
     */
    public void setRightPressed(boolean rightPressed) {
        this.rightPressed = rightPressed;
    }

    /**
     * @return the pausedGame
     */
    public boolean isPausedGame() {
        return pausedGame;
    }

    /**
     * @param pausedGame the pausedGame to set
     */
    public void setPausedGame(boolean pausedGame) {
        this.pausedGame = pausedGame;
    }

    /**
     * @return the firePressed
     */
    public boolean isFirePressed() {
        return firePressed;
    }

    /**
     * @param firePressed the firePressed to set
     */
    public void setFirePressed(boolean firePressed) {
        this.firePressed = firePressed;
    }

    /**
     * The entry point into the game. We'll simply create an
     * instance of class which will start the display and game
     * loop.
     *
     * @param argv The arguments that are passed into our game
     */
    public static void main(String argv[]) {
        Alien g = new Alien();

        // Start the main game loop, note: this method will not
        // return until the game has finished running. Hence we are
        // using the actual main thread to run the game.
        g.gameLoop();
    }
}
