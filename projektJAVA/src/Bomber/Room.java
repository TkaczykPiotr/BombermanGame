package Bomber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.io.IOException;

/**
 * Klasa tworzaca poczekalnie
 */
public class Room extends JFrame implements ActionListener {
    /**
     * guzik wyjscia
     */
    JButton close;
    /**
     * guzik wejscia
     */
    JButton room1_button;
    /**
     * tlo aplikacji
     */
    ImageIcon background_image;

    /**
     * tekst nazwy gracza
     */
    JTextField userName;

    /**
     * konstruktor tworzacy panele, guziki, pola tekstowe
     */
    public Room() {

        //font
        Font f = new Font("Arial-Black", Font.BOLD, 50);
        Font c = new Font("Arial-Black", Font.BOLD, 15);

        //header
        JPanel heading;
        heading = new JPanel();
        heading.setBackground(new Color(0, 0, 0, 0));
        heading.setBounds(0, 70, 600, 100);
        JLabel name = new JLabel("POCZEKALNIA");
        name.setBounds(200, 50, 400, 50);
        name.setFont(f);
        heading.add(name);

        //napis
        JPanel napis = new JPanel();
        napis.setLayout(new BorderLayout());
        napis.setSize(300, 150);
        //napis.setBackground(new Color(0,0,0,0));
        napis.setBounds(200, 200, 100, 35);


        JLabel czcionka = new JLabel("NAZWA", SwingConstants.CENTER);
        czcionka.setFont(c);
        czcionka.setBackground(new Color(255, 255, 255));
        czcionka.setBounds(50, 50, 150, 50);
        napis.add(czcionka);
        napis.setBorder(BorderFactory.createLineBorder(Color.black, 5));

        //login panel
        JPanel login = new JPanel();
        login.setLayout(new BorderLayout());
        login.setSize(300, 150);
        login.setBackground(new Color(0, 0, 0, 60));
        login.setBounds(300, 200, 100, 33);


        //panel tekstowy
        userName = new JTextField("");
        userName.setBounds(50, 50, 150, 50);
        //userName=username.getText();
        login.add(userName);


        //pokoje dołanczanie
        JPanel rooms = new JPanel();
        rooms.setSize(150, 50);
        rooms.setBackground(new Color(255, 255, 255));
        rooms.setBounds(200, 300, 200, 50);
        rooms.setBorder(BorderFactory.createLineBorder(Color.black, 5));

        JLabel room1 = new JLabel("POKOJ:", SwingConstants.LEFT);
        room1.setFont(c);
        room1.setBackground(new Color(255, 255, 255));
        rooms.add(room1);


        room1_button = new JButton("SELECT");
        room1_button.setBounds(50, 250, 100, 50);
        rooms.add(room1_button);
        room1_button.addActionListener(this);


        //buttons 2
        JPanel menu2 = new JPanel();
        menu2.setLayout(new BorderLayout());
        menu2.setSize(300, 150);
        menu2.setBackground(new Color(0, 0, 0, 0));
        menu2.setBounds(220, 450, 150, 50);
        menu2.setBorder(BorderFactory.createLineBorder(Color.black, 5));

        close = new JButton("WYJSCIE");
        close.setBounds(50, 250, 100, 50);
        close.setBackground(new Color(255, 255, 255));
        menu2.add(close);

        close.addActionListener(this);

        //frame
        setTitle("Bomberman");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //bacground
        background_image = new ImageIcon("src/resources/bg.png");

        Image img = background_image.getImage();
        Image temp_img = img.getScaledInstance(600, 600, Image.SCALE_SMOOTH);
        background_image = new ImageIcon(temp_img);
        JLabel background = new JLabel("", background_image, JLabel.CENTER);


        background.add(rooms);
        background.add(napis);
        background.add(login);
        background.add(menu2);
        background.add(heading);
        background.setBounds(0, 0, 600, 600);
        add(background);

        setVisible(true);
        //rst


    }

    /**
     * metoda main tworzaca obiekt
     * @param args argument
     */
    public static void main(String[] args) {
        new Room();

    }


    /**
     * metoda odczytajacy czy dane guziki zostaly wcisniete
     * @param e parametr
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == close) {
            this.setVisible(false);
        }
        if (e.getSource() == room1_button) {

            System.out.println(userName);
            if (userName.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Podaj nazwe uzytkownika!!!");
            } else {
                game.Bomberman.username = userName.getText();
                this.setVisible(false);

            }
        }

    }
}
