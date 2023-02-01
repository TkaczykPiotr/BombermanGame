package game;

import Bomber.Room;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.util.Random;


/**
 * Głowna klasa
 */
public class Bomberman extends JPanel implements Runnable, KeyListener {



    /**
     * nazwa gracza
     */
    public static String username;

    //watki sockety i wezly
    /**
     * watek
     */
    Thread thread;
    /**
     * socket
     */
    static Socket socket;
    /**
     * wyjscie
     */
    static DataOutputStream out;
    /**
     * wejscie
     */
    static DataInputStream in;

    /////
    /**
     * numer gracza podany przez serwer
     */
    int playerid;
    /**
     * wartosc logiczna czy zostal uruchomiony watek
     */
    boolean isRunning;

    //obiekty
    /**
     * obiekt bomby
     */
    Bomb bomb;
    /**
     * obiekt bonusu
     */
    Power power;
    /**
     * obiekt gracz
     */
    Player player = new Player();
    /**
     * obiekt drugiego gracz
     */
    PlayerBot playerBot = new PlayerBot();
    /**
     * obiekt generator
     */
    Random generator = new Random();

    /**
     * widok
     */
    BufferedImage view;
    /**
     * skrzyneczka
     */
    Image box;
    /**
     * sciana
     */
    Image wall;
    /**
     * gracz poczatkowy-obraz
     */
    Image playerCentre;
    /**
     * drugi gracz poczatkowy-obraz
     */
    Image playerBotCentre;
    /**
     * obrot gracza
     */
    Image playerAnimUp, playerAnimDown, playerAnimRight, playerAnimLeft;
    /**
     * obrot drugiego gracza
     */
    Image playerBotAnimUp, playerBotAnimDown, playerBotAnimRight, playerBotAnimLeft;
    /**
     * bombka
     */
    Image bombAnim;
    /**
     * eksplozja
     */
    Image explosion;
    /**
     * eksplozja lewa,prawa,gora,dol
     */
    Image explosionLeft, explosionRight, explosionUp, explosionDown;
    /**
     * bonus
     */
    Image powerUp;


    /**
     * scena
     */
    int[][] scene;

    //rozmiar bloku
    /**
     * rozmiar bloku
     */
    int titleSize = 50;

    //predkosc graczy
    /**
     * predkosc graczy
     */
    int speed = 4;
    //obrot graczy
    /**
     * wartosc logiczna obrotu gracza
     */
    boolean right, left, up, down;
    /**
     * wartosc logiczna obrotu drugiego gracza
     */
    boolean rightBot, leftBot, upBot, downBot;
    //
    /**
     * wartosc logiczna bombki, smierci 1 gracza, smierci 2 gracza
     */
    boolean goBomb, deadPlayer, deadBotPlayer;

    //bombka wybuch
    /**
     * zmienne liczace bombke do wybuchu
     */
    int frameBomb = 0, intervalBomb = 7, indexAnimBomb = 0;
    /**
     * zmienne liczace bombke do wybuchu 2
     */
    int frameExplosion = 0, intervalExplosion = 3, indexAnimExplosion = 0;


    //rozmiar mapy
    /**
     * szerokosc mapy
     */
    final int WIDTH = 550;
    /**
     * wysokosc mapy
     */
    final int HEIGHT = 550;


    /**
     * Konstruktor do klasy glownej, tworzy 2 watki i laczy sie z serwerem.
     * Pobiera informacje o nacisnieciu klawisza
     */
    public Bomberman() {


        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);

        try {
            System.out.println("Connecting...");
            socket = new Socket("localhost", 7777);
            System.out.println("Connection Succesful.");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            playerid = in.readInt();
            Input input = new Input(in, this);
            Thread thread = new Thread(input);
            thread.start();


            Thread thread2 = new Thread(this);
            thread2.start();


        } catch (Exception e) {
            System.out.println("Unable to start client");
        }
    }

    /**
     * Metoda main
     * Uruchamia poczekalnie oraz tworzy mape gry.
     * @param args argument
     * @throws InterruptedException watek
     */
    public synchronized static void main(String[] args) throws InterruptedException {

        new Room().setVisible(true);
        while(username==null){
           Thread.sleep(1000);
        }
            JFrame frame = new JFrame("Bomberman");
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new Bomberman());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

    }

    /**
     * Metoda koordynujaca interakcje miedzy watkami w Javie
     * Tworzy nowy watek i go uruchamia
     *
     */
    @Override
    public synchronized void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    /**
     * Metoda sprawdzajaca czy przestrzen po każdej z stron gracza jest wolna
     * @param nextX  nastepny punkt na mapie w przestrzeni X
     * @param nextY     nastepny punkt na mapie w przestrzeni Y
     * @return zwraca true jesli jest wolne i false jesli nie
     */
    public synchronized boolean isFree(int nextX, int nextY) {
        int size = titleSize;

        int nextX_1 = nextX / size;
        int nextY_1 = nextY / size;

        int nextX_2 = (nextX + size - 1) / size;
        int nextY_2 = nextY / size;

        int nextX_3 = nextX / size;
        int nextY_3 = (nextY + size - 1) / size;

        int nextX_4 = (nextX + size - 1) / size;
        int nextY_4 = (nextY + size - 1) / size;

        return !((scene[nextY_1][nextX_1] == 1 || scene[nextY_1][nextX_1] == 2) ||
                (scene[nextY_2][nextX_2] == 1 || scene[nextY_2][nextX_2] == 2) ||
                (scene[nextY_3][nextX_3] == 1 || scene[nextY_3][nextX_3] == 2) ||
                (scene[nextY_4][nextX_4] == 1 || scene[nextY_4][nextX_4] == 2));
    }

    /**
     * Metoda sprawdzająca czy kolejny blok jest pusty.
     * Metoda ta zapisuje do zmiennych  w klasie bomb informacje ile blokow w kazdym kierunku jest wolnych.
     */
    public synchronized void isExploding() {
        int i = 1;
        if (bomb != null) {
            while (scene[bomb.y + i][bomb.x] != 1 && scene[bomb.y + i][bomb.x] != 2) {
                bomb.downPower++;
                i++;
            }
            i = 1;
            while (scene[bomb.y - i][bomb.x] != 1 && scene[bomb.y - i][bomb.x] != 2) {
                bomb.upPower++;
                i++;
            }
            i = 1;
            while (scene[bomb.y][bomb.x + i] != 1 && scene[bomb.y][bomb.x + i] != 2) {
                bomb.rightPower++;
                i++;
            }
            i = 1;
            while (scene[bomb.y][bomb.x - i] != 1 && scene[bomb.y][bomb.x - i] != 2) {
                bomb.leftPower++;
                i++;
            }

        }
    }


    /**
     * Metoda sprawdzajaca czy gracz znajduje sie w polu razenia bomby po wybuchu
     * @param playerY Pozycja Y gracza
     * @param playerX Pozycja X gracza
     * @param bombY Pozycja Y bomby
     * @param bombX Pozycja X bomby
     *  Jesli gracz znajduje sie w polu razenia do zmiennej zapisywana jest wartosc ze umarl
     */
    public synchronized void isDead(int playerY, int playerX, int bombY, int bombX) {
        int isPlayerY = (playerY + (titleSize / 2)) / titleSize;
        int isPlayerX = (playerX + (titleSize / 2)) / titleSize;
        if (isPlayerY == bombY && isPlayerX == bombX) {
            deadPlayer = true;
        }
    }

    /**
     * Metoda Wyswietlajaca informacje ktory z graczy wygral
     */
    public synchronized void showMessageDead(){

        if(playerid==0){
            if(deadPlayer){
                JOptionPane.showMessageDialog(null, "Wygral gracz Zolty");
                System.exit(0);
            }
            if(deadBotPlayer){
                JOptionPane.showMessageDialog(null, "Wygral gracz Czerwony");
                System.exit(0);
            }
        }
        if(playerid==1){
            if(deadPlayer){
                JOptionPane.showMessageDialog(null, "Wygral gracz Zolty");
                System.exit(0);
            }
            if(deadBotPlayer){
                JOptionPane.showMessageDialog(null, "Wygral gracz Czerwomy");
                System.exit(0);
            }
        }


    }

    /**
     * Metoda sprawdzajaca czy zebrano bonus
     * @param playerY pozycja Y gracza
     * @param playerX pozycja X gracza
     * Jesli gracz znajduje sie na tej samej pozycji co bonus do zmiennej w klasie power zapisywana jest informacja ze zostalo zebrane
     */
    public synchronized void isCollecting(int playerY, int playerX) {
        int isPlayerY = (playerY + (titleSize / 2)) / titleSize;
        int isPlayerX = (playerX + (titleSize) / 2) / titleSize;

        if (isPlayerY == power.y && isPlayerX == power.x) {
            power.collected = true;
        }
    }

    /**
     * Metoda generujaca losowo bonus po rozwaleniu skrzynki przez bombe
     * @param y pozyzcja Y po rozwalonej skrzynce
     * @param x pozyzcja X po rozwalonej skrzynce
     *  Jesli wygeneruje sie bonus zostaje stworzony obiekt power i zapisane zostana jego wspolrzedne oraz blok na mapie
     */
    public synchronized void isRandom(int y, int x) {
        int loss = 0;
        loss = generator.nextInt(5);
        if (loss == 1) {
            if (power == null) {
                power = new Power();
                power.x = x;
                power.y = y;
                scene[y][x] = 5;
            }

        }
    }

    /**
     * Metoda aktualizujaca pozycje drugiego gracza na mapie
     * @param pid numer gracza podany przez serwer
     * @param x2 pozycja X drugiego gracza
     * @param y2 pozycja Y drugiego gracza
     */
    public synchronized void updateCoordinates(int pid, int x2, int y2) {

        if(playerid==0){
            if(pid==1){
                playerBot.x = x2;
                playerBot.y = y2;
            }
        }

        if(playerid==1){
            if(pid==0){
                playerBot.x = x2;
                playerBot.y = y2;
            }
        }

    }

    /**
     * Metoda aktualizujaca obrot drugiego gracza
     * @param pid numer gracza podany przez serwer
     * @param right2 informacja o tym ze gracz obrocil sie w prawo
     * @param left2 informacja o tym ze gracz obrocil sie w lewo
     * @param up2 informacja o tym ze gracz obrocil sie w w gore
     * @param down2 informacja o tym ze gracz obrocil sie w dol
     */
    public synchronized void updateObject(int pid, boolean right2, boolean left2, boolean up2, boolean down2) {

    if(playerid==0){
        if(pid==1){
            rightBot = right2;
            leftBot = left2;
            upBot = up2;
            downBot = down2;

        }
    }

    if(playerid==1){
        if(pid==0){
            rightBot = right2;
            leftBot = left2;
            upBot = up2;
            downBot = down2;
         }
        }

    }

    /**
     * Metoda aktualizujaca pozycje bomby
     * @param pid numer gracza podany przez serwer
     * @param x2 pozycja X drugiego gracza
     * @param y2 pozycja Y drugiego gracza
     * @param isBomb informacja o tym czy bomba zostala wypuszczona przez drugiego gracza
     * Jesli drugi gracz puscil bombe tworzony jest nowy obiekt bomb oraz zapisywane sa jego wszystkie parametry
     */
    public synchronized void  updateBombs(int pid, int x2,int y2,boolean isBomb) {


            if(x2<501 && y2<501 && x2>49 && y2>49)
            {
                if(isBomb){
                    if(playerid==0){
                        if(pid==1){
                            bomb = new Bomb();
                            playerBot.x = x2;
                            playerBot.y = y2;
                            bomb.x = (playerBot.x + (titleSize / 2)) / titleSize;
                            bomb.y = (playerBot.y + (titleSize / 2)) / titleSize;
                            scene[bomb.y][bomb.x] = 3;


                        }
                    }

                    if(playerid==1){
                        if(pid==0){
                            bomb = new Bomb();
                            playerBot.x = x2;
                            playerBot.y = y2;
                            bomb.x = (playerBot.x + (titleSize / 2)) / titleSize;
                            bomb.y = (playerBot.y + (titleSize / 2)) / titleSize;
                            scene[bomb.y][bomb.x] = 3;


                        }
                    }
                }
            }
        }


    /**
     * metoda czyszczaca mape po pozostalosciach bomb
     */
    public synchronized  void cleanScene(){

        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if(scene[j][i]==3){
                    if(bomb !=null){
                        if(bomb.exploded){
                            scene[j][i]=0;
                        }
                    }

                }
            }
        }
    }

    /**
     * Metoda zapisujaca do zmiennych czy drugi gracz umarl
     * @param pid numer gracza podany przez serwer
     * @param dead informacja o tym czy drugi gracz umarl z serwera
     */
    public synchronized  void updateDead(int pid, boolean dead) {

        if(playerid==0){
            if(pid==1){
                deadBotPlayer = dead;
            }
        }

        if(playerid==1){
            if(pid==0){
                deadBotPlayer = dead;
            }
        }

    }


    /**
     * Metoda tworzaca mape, postacie , bloki oraz pozycje poczatkowa obu graczy
     */
    public synchronized void start() {
        try {

            view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);


            box = new ImageIcon("src/resources/krejt.png").getImage();
            wall = new ImageIcon("src/resources/blok-kamien.png").getImage();
            playerCentre = new ImageIcon("src/resources/ludzik2-1.png").getImage();
            //
            playerAnimLeft = new ImageIcon("src/resources/ludzik2-4.png").getImage();
            playerAnimRight = new ImageIcon("src/resources/ludzik2-3.png").getImage();
            playerAnimUp = new ImageIcon("src/resources/ludzik2-2.png").getImage();
            playerAnimDown = new ImageIcon("src/resources/ludzik2-1.png").getImage();
            bombAnim = new ImageIcon("src/resources/bombka.png").getImage();
            explosion = new ImageIcon("src/resources/explozja.png").getImage();
            explosionLeft = new ImageIcon("src/resources/bum.png").getImage();
            explosionRight = new ImageIcon("src/resources/bum.png").getImage();
            explosionDown = new ImageIcon("src/resources/bum.png").getImage();
            explosionUp = new ImageIcon("src/resources/bum.png").getImage();
            powerUp = new ImageIcon("src/resources/powerup.png").getImage();


            playerBotCentre = new ImageIcon("src/resources/ludzik1-1.png").getImage();
            playerBotAnimLeft = new ImageIcon("src/resources/ludzik1-4.png").getImage();
            playerBotAnimRight = new ImageIcon("src/resources/ludzik1-3.png").getImage();
            playerBotAnimUp = new ImageIcon("src/resources/ludzik1-2.png").getImage();
            playerBotAnimDown = new ImageIcon("src/resources/ludzik1-1.png").getImage();
            scene = new int[][]{
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 0, 0, 0, 0, 0, 2, 2, 0, 0, 1},
                    {1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1},
                    {1, 2, 2, 2, 1, 0, 1, 2, 2, 2, 1},
                    {1, 2, 1, 1, 1, 0, 1, 1, 1, 2, 1},
                    {1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 1},
                    {1, 2, 1, 1, 1, 2, 1, 1, 1, 0, 1},
                    {1, 2, 2, 2, 1, 2, 1, 2, 2, 0, 1},
                    {1, 0, 1, 2, 1, 2, 1, 2, 1, 0, 1},
                    {1, 0, 0, 2, 2, 2, 2, 2, 0, 0, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},

            };
            //pozycja poczatkowa graczy
            if(playerid==0){
                player.x = titleSize;
                player.y = titleSize;
                playerBot.x = titleSize*9;
                playerBot.y = titleSize*9;
            }
            if(playerid==1){
                player.x = titleSize*9;
                player.y = titleSize*9;
                playerBot.x = titleSize;
                playerBot.y = titleSize;
            }



        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Metoda aktualizujaca takie rzeczy jak:
     * Pozycje gracza
     * Moc bonusu gracza, oraz usuwanie obiektu
     * Bombke, jej wybuch, informacje o tym czy gracz jest na polu razenia oraz po wybuchu usuwa obiekt bombki
     * Obrot gracza
     * Obrot drugiego gracza
     */
    public synchronized void update() {

        //pozycja gracza
        if (right && isFree(player.x + speed, player.y)) {
            player.x += speed;

        }
        if (left && isFree(player.x - speed, player.y)) {
            player.x -= speed;

        }
        if (up && isFree(player.x, player.y - speed)) {
            player.y -= speed;

        }
        if (down && isFree(player.x, player.y + speed)) {
            player.y += speed;

        }



        //bonus
        if (power != null) {
            isCollecting(player.y, player.x);
            if (power.collected) {
                if (player.powerSize < 3) {
                    player.powerSize++;
                }
                scene[power.y][power.x] = 0;
                power = null;
            }
        }




        //bombka
        if (bomb != null) {
            frameBomb++;
            if (frameBomb == intervalBomb) {
                frameBomb = 0;
                indexAnimBomb++;
                if (indexAnimBomb > 2) {
                    indexAnimBomb = 0;
                    bomb.countToExplode++;
                }
                if (bomb.countToExplode >= bomb.intervalToExplode) {
                    bomb.exploded = true;
                    isExploding();
                    if (scene[bomb.y + 1][bomb.x] == 2) { //eksplozja bombki sprawdzajaca czy nastepne bloki sa skrzyneczkami
                        scene[bomb.y + 1][bomb.x] = 0;
                        isRandom(bomb.y + 1, bomb.x);
                    }
                    if (scene[bomb.y - 1][bomb.x] == 2) {
                        scene[bomb.y - 1][bomb.x] = 0;
                        isRandom(bomb.y - 1, bomb.x);
                    }
                    if (scene[bomb.y][bomb.x + 1] == 2) {
                        scene[bomb.y][bomb.x + 1] = 0;
                        isRandom(bomb.y, bomb.x + 1);
                    }

                    if (scene[bomb.y][bomb.x - 1] == 2) {
                        scene[bomb.y][bomb.x - 1] = 0;
                        isRandom(bomb.y, bomb.x - 1);
                    }
                    for (int i = 1; i <= player.powerSize; i++) {
                        if (i < bomb.downPower) {
                            if (scene[bomb.y + i][bomb.x] == 2) {
                                scene[bomb.y + i][bomb.x] = 0;
                                isRandom(bomb.y + i, bomb.x);
                            }
                        }
                        if (i < bomb.upPower) {
                            if (scene[bomb.y - i][bomb.x] == 2) {
                                scene[bomb.y - i][bomb.x] = 0;
                                isRandom(bomb.y - i, bomb.x);
                            }
                        }
                        if (i < bomb.rightPower) {
                            if (scene[bomb.y][bomb.x + i] == 2) {
                                scene[bomb.y][bomb.x + i] = 0;
                                isRandom(bomb.y, bomb.x + i);
                            }
                        }
                        if (i < bomb.leftPower) {
                            if (scene[bomb.y][bomb.x - i] == 2) {
                                scene[bomb.y][bomb.x - i] = 0;
                                isRandom(bomb.y, bomb.x - i);
                            }
                        }

                        //sprawdza czy gracz znajduje sie w polu razenia
                        isDead(player.y, player.x, bomb.y, bomb.x);
                        if (i <= bomb.downPower) {
                            isDead(player.y, player.x, bomb.y + i, bomb.x);
                        }
                        if (i <= bomb.upPower) {
                            isDead(player.y, player.x, bomb.y - i, bomb.x);
                        }
                        if (i <= bomb.rightPower) {
                            isDead(player.y, player.x, bomb.y, bomb.x + i);
                        }
                        if (i <= bomb.leftPower) {
                            isDead(player.y, player.x, bomb.y, bomb.x - i);
                        }


                    }


                }
            }
            if (bomb.exploded) {
                frameExplosion++;
                if (frameExplosion == intervalExplosion) {
                    frameExplosion = 0;
                    indexAnimExplosion++;
                    if (indexAnimExplosion == 4) { //bombka wybucha i na jej miejsce jest stawiane puste pole
                        indexAnimExplosion = 0;
                        scene[bomb.y][bomb.x] = 0;
                        bomb = null;
                    }

                }
            }

        }


        //aktualizacja obrotu gracza
        if (right) {
            playerCentre = playerAnimRight;
        } else if (left) {
            playerCentre = playerAnimLeft;
        } else if (up) {
            playerCentre = playerAnimUp;
        } else if (down) {
            playerCentre = playerAnimDown;
        }

        //aktualizacja obrotu drugiego gracza
        if (rightBot) {
            playerBotCentre = playerBotAnimRight;
        } else if (leftBot) {
            playerBotCentre = playerBotAnimLeft;
        } else if (upBot) {
            playerBotCentre = playerBotAnimUp;
        } else if (downBot) {
            playerBotCentre = playerBotAnimDown;
        }


    }

    /**
     * Metoda rysujaca wszystkie elementy gry czyli:
     * mape, bloki, graczy, skrzynki, bombki, wybuchy, eksplozje
     */
    public synchronized void draw() {
        Graphics2D g2 = (Graphics2D) view.getGraphics();
        g2.setColor(new Color(56, 135, 0));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int size = titleSize;
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if (scene[j][i] == 1) {//sciana
                    g2.drawImage(wall, i * size, j * size, size, size, null);
                } else if (scene[j][i] == 2) { //skrzynka
                    g2.drawImage(box, i * size, j * size, size, size, null);
                } else if (scene[j][i] == 3) {//bombka
                    if (bomb != null) {
                        if (bomb.exploded) {
                            g2.drawImage(explosion, bomb.x * size, bomb.y * size, size, size, null);
                            for (int x = 1; x <= player.powerSize; x++) {//rysuje wybuch w zaleznosci tego czy moze
                                if (x < bomb.rightPower) {
                                    if (scene[bomb.y][bomb.x + x] == 0) {
                                        g2.drawImage(explosionRight, (bomb.x + x) * size, bomb.y * size, size, size, null);
                                    }
                                }
                                if (x < bomb.leftPower) {
                                    if (scene[bomb.y][bomb.x - x] == 0) {
                                        g2.drawImage(explosionLeft, (bomb.x - x) * size, bomb.y * size, size, size, null);
                                    }
                                }
                                if (x < bomb.upPower) {
                                    if (scene[bomb.y - x][bomb.x] == 0) {
                                        g2.drawImage(explosionUp, bomb.x * size, (bomb.y - x) * size, size, size, null);
                                    }
                                }
                                if (x < bomb.downPower) {
                                    if (scene[bomb.y + x][bomb.x] == 0) {
                                        g2.drawImage(explosionDown, bomb.x * size, (bomb.y + x) * size, size, size, null);
                                    }
                                }
                            }

                        }

                        g2.drawImage(bombAnim, i * size, j * size, size, size, null);
                    }

                } else if (scene[j][i] == 5) {//bonus
                    g2.drawImage(powerUp, i * size, j * size, size, size, null);
                }
            }
        }



        //gracze
        g2.drawImage(playerBotCentre, playerBot.x, playerBot.y, size, size, null);
        g2.drawImage(playerCentre, player.x, player.y, size, size, null);


        Graphics g = getGraphics();
        g.drawImage(view, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
    }


    /**
     * Metoda run, która działa po uruchomieniu watku
     * Jesli działa to wykonuje wszystkie najwazniejsze metody oraz wysyla do serwera informacje o graczu
     */
    @Override
        public  void run() {
            try {
                requestFocus();
                start();
                while (isRunning) {
                    if (right || left || up || down || bomb != null) {
                        try { //wysyla informacje do serwera
                            out.writeInt(playerid);
                            out.writeInt(player.x);
                            out.writeInt(player.y);
                            out.writeBoolean(right);
                            out.writeBoolean(left);
                            out.writeBoolean(up);
                            out.writeBoolean(down);
                            out.writeBoolean(goBomb);
                            out.writeBoolean(deadPlayer);
                            goBomb=false;
                            Thread.sleep(12);

                        } catch (Exception e) {
                            System.out.println("Error sending Coordinates");
                        }
                    }
                    //aktualizacja metod
                    update();
                    draw();
                    Thread.sleep(16);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void keyTyped(KeyEvent e) {

        }

    /**
     * Metoda odczytujaca klawiature
     * jesli guzik zostal wcisniety to do zmiennych zapisywana jest o tym informacja
     * @param e parametr
     */
    @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) { //spacja -> bombka zostaje puszczona
                if (bomb == null) {

                    bomb = new Bomb();
                    bomb.x = (player.x + (titleSize / 2)) / titleSize;
                    bomb.y = (player.y + (titleSize / 2)) / titleSize;
                    scene[bomb.y][bomb.x] = 3;
                    goBomb=true;
                    cleanScene();

                }
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                right = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                left = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                up = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                down = true;
            }
        }
    /**
     * Metoda odczytujaca klawiature
     * jesli guzik nie zostal wcisniety to do zmiennych zapisywana jest o tym informacja
     * @param e parametr
     */
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                right = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                left = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                up = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                down = false;
            }
        }
    }


/**
 * klasa pobierajaca informacje od serwera oraz wykonujaca metody aktualizujace
 */
class Input implements Runnable{
    DataInputStream in;
    Bomberman client;
    public Input(DataInputStream in, Bomberman c){
        this.in = in;
        this.client = c;
    }

    public  void run() {
        while(true){
            try{
                //czytanie informacji od serwera
                int playerid = in.readInt();
                int x = in.readInt();
                int y = in.readInt();
                boolean rightObj = in.readBoolean();
                boolean leftObj = in.readBoolean();
                boolean upObj = in.readBoolean();
                boolean downObj = in.readBoolean();
                boolean isBomb = in.readBoolean();
                boolean isDead = in.readBoolean();


                //aktualizacja tych informacji
                client.updateBombs(playerid,x,y,isBomb);
                client.updateObject(playerid,rightObj,leftObj,upObj,downObj);
                client.updateCoordinates(playerid,x,y);
                client.updateDead(playerid,isDead);
                client.showMessageDead();



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}



