package snake;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {


    private Food food = new Food();
    private Wall wall = new Wall();

    private final int DOT_SIZE = 20;
    private final int WIDTH = 1000;
    private final int HEIGHT = 720;

    private final int x[] = new int[100];
    private final int y[] = new int[100];

    enum Game {START, PLAY, TRANSITION, END}

    private Game gameStatus = Game.START;

    private boolean rightDir;
    private boolean downDir;
    private boolean leftDir;
    private boolean upDir;

    private JTextField T_field = new JTextField(10);
    private JButton startBtn = new JButton();
    private JButton exitBtn = new JButton();
    private JButton tryAgainBtn = new JButton();

    private Timer timer;
    private Image StartIMG;
    private Image BgIMG;
    private Image EndIMG;
    private Image headIMG;
    private Image bodyIMG;
    private Image bodyKIMG;
    private Image tailIMG;

    private int timeCounter;
    private int regimenCounter;
    private boolean doRegimen = false;

    private String name;
    private int score;
    private int dots;
    private int round = 5;
    private int scores[] = new int[5];
    private int times[] = new int[5];

    ControllerManager controllers = new ControllerManager();

    public Board() {

        Arrays.fill(scores, 0);
        Arrays.fill(times, 0);

        addKeyListener(new TAdapter());
        setFocusable(true);

        controllers.initSDLGamepad();

        //draw testfield and button
        drawJComponent();
        //load images
        loadImages();
        //initial game
        initGame();
    }

    private void loadImages() {
        //load images
        ImageIcon ii0 = new ImageIcon("src/Snake/images/StartIMG.png");
        StartIMG = ii0.getImage();

        ImageIcon ii1 = new ImageIcon("src/Snake/images/BgIMG.png");
        BgIMG = ii1.getImage();

        ImageIcon ii2 = new ImageIcon("src/Snake/images/EndIMG.png");
        EndIMG = ii2.getImage();

        ImageIcon ii3 = new ImageIcon("src/Snake/images/head.png");
        headIMG = ii3.getImage();

        ImageIcon ii4 = new ImageIcon("src/Snake/images/body.png");
        bodyIMG = ii4.getImage();

        ImageIcon ii5 = new ImageIcon("src/Snake/images/bodyK.png");
        bodyKIMG = ii5.getImage();

        ImageIcon ii6 = new ImageIcon("src/Snake/images/tail.png");
        tailIMG = ii6.getImage();
    }

    private void initGame() {
        //set dots size and timeCounter
        dots = 3;
        timeCounter = 0;
        regimenCounter = 0;
        doRegimen = false;
        //initials first mokhtasat of snake
        for (int z = 0; z < dots; z++) {
            y[z] = 80 - z * 20;
            x[z] = 60;
        }

        //random first mokhtasat of food and wall
        food.random();
        wall.random();
        //reset score
        score = 0;
        //init first direction
        rightDir = false;
        leftDir = false;
        upDir = false;
        downDir = true;
        //create new timer
        timer = new Timer(100, this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        ControllerState currState = controllers.getState(0);
        if (currState.isConnected) {
            if (currState.dpadUpJustPressed || currState.leftStickY > 0.8) {
                goUp();
            } else if (currState.dpadDownJustPressed || currState.leftStickY < -0.8) {
                goDown();
            } else if (currState.dpadRightJustPressed || currState.leftStickX > 0.8) {
                goRight();
            } else if (currState.dpadLeftJustPressed || currState.leftStickX < -0.8) {
                goLeft();
            }
        }

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        switch (gameStatus) {
            case START: {
                //draw background image
                g.drawImage(StartIMG, 0, 0, this);
                //draw textfield and buttoons
                T_field.setBounds(490, 370, 180, 45);
                startBtn.setBounds(700, 440, 125, 80);
                break;
            }
            case PLAY: {
                //draw background image
                g.drawImage(BgIMG, 0, 0, this);
                //draw Snake
                drawSnake(g);
                //draw food
                g.drawImage(food.getImage(), food.getX(), food.getY(), this);
                //draw wall
                for (int i = 0; i < wall.getLength(); i++) {
                    g.drawImage(wall.getImage(), wall.getX()[i], wall.getY()[i], this);
                }
                //draw your score and best score
                Font font2 = new Font("chiller", Font.BOLD, 28);
                g.setColor(Color.white);
                g.setFont(font2);
                g.drawString(getMax() + "", 720, 790);
                g.drawString(score + " ", 720, 755);
                g.drawString("- " + name, 755, 755);
                //draw timeCounter and Snake length
                Font font3 = new Font("chiller", Font.BOLD, 20);
                g.setColor(Color.yellow);
                g.setFont(font3);
                g.drawString("Time: " + timeCounter / 10, 450, 755);
                g.drawString("Length: " + dots, 450, 790);
                //draw regimen
                if (doRegimen) {
                    g.drawImage(food.getRegimen(), food.getRegimenX(), food.getRegimenY(), this);
                    g.setFont(font2);
                    g.drawString("" + (((100 - regimenCounter) / 10) + 1), 920, 750);
                    g.drawRect(870, 770, 100, 20);
                    g.fillRect(872, 772, 100 - regimenCounter, 17);
                    if (100 - regimenCounter < 0) {
                        doRegimen = false;
                        regimenCounter = 0;
                    }
                }
                break;
            }
            case TRANSITION: {
                //stop the timer
                timer.stop();
                //draw background image
                g.drawImage(EndIMG, 0, 0, this);
                //draw buttons
                tryAgainBtn.setBounds(470, 300, 260, 80);
                tryAgainBtn.setVisible(true);
                break;
            }
            case END: {
                //stop the timer
                timer.stop();
                //draw background image
                g.drawImage(EndIMG, 0, 0, this);
                //draw buttons
                exitBtn.setBounds(650, 400, 155, 80);
                exitBtn.setVisible(true);

                //Save Score
                Save.setScore(name, scores, times);
                break;
            }
        }
    }

    private int getMax() {
        int max = score;
        for (int i:scores) {
            if(i > max)
                max = i;
        }

        return max;
    }

    private void randomFoodAndWall() {
        food.random();
        wall.random();

        while (true) {
            for (int i = 0; i < wall.getLength(); i++)
                if ((food.getX() == wall.getX()[i]) && (food.getX() == wall.getY()[i])) {
                    food.random();
                    continue;
                }

            break;
        }

        while (true) {
            for (int i = 0; i < wall.getLength(); i++)
                for (int j = 0; j < dots; j++)
                    if ((wall.getX()[i] == x[j]) && (wall.getY()[i] == y[j])) {
                        wall.random();
                        continue;
                    }

            break;
        }
    }

    private void drawSnake(Graphics g) {
        double angle = 0;

        for (int i = 0; i < dots; i++) {
            if (i == 0) {

                if (rightDir) angle = 0;
                if (upDir) angle = Math.PI / 2;
                if (downDir) angle = -Math.PI / 2;
                if (leftDir) angle = -Math.PI;

                drawPartOfImage(g, i, angle, headIMG);

            } else if (i == dots - 1) {

                if (x[i - 1] > x[i]) angle = 0;
                if (y[i - 1] < y[i]) angle = Math.PI / 2;
                if (y[i - 1] > y[i]) angle = -Math.PI / 2;
                if (x[i - 1] < x[i]) angle = -Math.PI;

                drawPartOfImage(g, i, angle, tailIMG);

            } else {

                Image image = null;
                if ((x[i] < x[i + 1] && y[i] > y[i - 1]) || (x[i] < x[i - 1] && y[i] > y[i + 1])) {
                    angle = Math.PI / 2;
                    image = bodyKIMG;
                } else if ((x[i] < x[i + 1] && y[i] < y[i - 1]) || (x[i] < x[i - 1] && y[i] < y[i + 1])) {
                    angle = 0;
                    image = bodyKIMG;
                } else if ((x[i] > x[i + 1] && y[i] < y[i - 1]) || (x[i] > x[i - 1] && y[i] < y[i + 1])) {
                    angle = -Math.PI / 2;
                    image = bodyKIMG;
                } else if ((x[i] > x[i + 1] && y[i] > y[i - 1]) || (x[i] > x[i - 1] && y[i] > y[i + 1])) {
                    angle = Math.PI;
                    image = bodyKIMG;
                } else if (x[i - 1] < x[i]) {
                    angle = 0;
                    image = bodyIMG;
                } else if (y[i - 1] < y[i]) {
                    angle = Math.PI / 2;
                    image = bodyIMG;
                } else if (y[i - 1] > y[i]) {
                    angle = -Math.PI / 2;
                    image = bodyIMG;
                } else if (x[i - 1] > x[i]) {
                    angle = -Math.PI;
                    image = bodyIMG;
                }

                drawPartOfImage(g, i, angle, image);

            }
        }

    }

    private void drawPartOfImage(Graphics g, int i, double angle, Image image) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(x[i] % WIDTH + 10, y[i] % HEIGHT + 10);
        g2d.rotate(-angle);
        g.drawImage(image, -10, -10, this);
        g2d.rotate(angle);
        g2d.translate(-x[i] % WIDTH - 10, -y[i] % HEIGHT - 10);
    }

    private void drawJComponent() {

        T_field.setFont(new Font("chiller", Font.BOLD, 34));
        add(T_field);

        startBtn.setText("Start");
        startBtn.setFont(new Font("chiller", Font.BOLD, 50));
        startBtn.setBackground(Color.GREEN);
        startBtn.addActionListener(arg0 -> {
            timer.start();
            gameStatus = Game.PLAY;
            name = T_field.getText();
            T_field.setVisible(false);
            startBtn.setVisible(false);
        });
        add(startBtn);

        exitBtn.setText("Exit !");
        exitBtn.setVisible(false);
        exitBtn.setFont(new Font("chiller", Font.BOLD, 50));
        exitBtn.setBackground(Color.RED);
        exitBtn.addActionListener(arg0 -> System.exit(0));
        add(exitBtn);

        tryAgainBtn.setText("Next Trial!");
        tryAgainBtn.setVisible(false);
        tryAgainBtn.setFont(new Font("chiller", Font.BOLD, 50));
        tryAgainBtn.setBackground(Color.GREEN);
        tryAgainBtn.addActionListener(arg0 -> {
            initGame();
            timer.start();
            gameStatus = Game.PLAY;
            exitBtn.setVisible(false);
            tryAgainBtn.setVisible(false);
        });
        add(tryAgainBtn);
    }

    private void move() {

        //shift dots of snake one unit to back
        for (int i = dots; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        //add one unit to the head of snake
        if (leftDir) x[0] -= DOT_SIZE;
        if (rightDir) x[0] += DOT_SIZE;
        if (upDir) y[0] -= DOT_SIZE;
        if (downDir) y[0] += DOT_SIZE;

        if (x[0] < 0) {
            for (int i = 0; i < dots; i++)
                x[i] += WIDTH;
        }
        if (y[0] < 0) {
            for (int i = 0; i < dots; i++)
                y[i] += HEIGHT;
        }

    }

    private void checkIfGameEnd() {

        //check if snake accident to his body
        for (int i = dots; i > 0; i--)
            if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
                scores[5 - round] = score;
                times[5 - round] = timeCounter / 10;
                round--;
                gameStatus = (round <= 0) ? Game.END : Game.TRANSITION;
            }

        //check if snake accident to the wall
        for (int i = 0; i < wall.getLength(); i++)
            if ((x[0] % WIDTH == wall.getX()[i]) && (y[0] % HEIGHT == wall.getY()[i])) {
                scores[5 - round] = score;
                times[5 - round] = timeCounter / 10;
                round--;
                gameStatus = (round <= 0) ? Game.END : Game.TRANSITION;
            }

    }

    private void goUp() {
        if (!downDir) {
            upDir = true;
            rightDir = false;
            leftDir = false;
        }
    }

    private void goRight() {
        if (!leftDir) {
            rightDir = true;
            upDir = false;
            downDir = false;
        }
    }

    private void goLeft() {
        if (!rightDir) {
            leftDir = true;
            upDir = false;
            downDir = false;
        }
    }

    private void goDown() {
        if (!upDir) {
            downDir = true;
            rightDir = false;
            leftDir = false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (true) {

            //plus timeCounter
            timeCounter++;
            //Move snake :||
            move();
            //check if Game end or not
            checkIfGameEnd();
            //check if snake eat food or nor
            if ((x[0] % WIDTH == food.getX()) && (y[0] % HEIGHT == food.getY())) {

                score++;
                dots += 2;
                if ((score > 0) && (score % 4 == 0))
                    doRegimen = true;
                randomFoodAndWall();
            }
            //regimenCounter
            if (doRegimen) {
                regimenCounter++;

                if ((x[0] % WIDTH == food.getRegimenX()) && (y[0] % HEIGHT == food.getRegimenY())) {
                    doRegimen = false;
                    regimenCounter = 0;
                    dots -= 3;
                }
            }

        }
        repaint();
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    goLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    goRight();
                    break;
                case KeyEvent.VK_UP:
                    goUp();
                    break;
                case KeyEvent.VK_DOWN:
                    goDown();
            }
        }
    }

}