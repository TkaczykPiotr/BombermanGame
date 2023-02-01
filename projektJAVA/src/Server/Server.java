package Server;

import java.util.logging.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * klasa serwer tworzaca socket, watki oraz przyjmujaca polaczenie od graczy i wysylanie informacji do nich o ich numerze id
 */
public class Server {

    protected static final Logger log = Logger.getLogger(Server.class.getName());


    static ServerSocket serverSocket;
    static Users[] user = new Users[2];


    /**
     * Metoda main
     * @param args argument
     * @throws Exception watek
     */
    public  static void main(String[] args) throws Exception {
        Logger.getLogger("my error log").addHandler(new FileHandler("src/Server/log.txt", true));

        System.out.println("Starting server...");
        serverSocket = new ServerSocket(7777); //tworzenie socketow
        System.out.println("Server Started...");
        Logger.getLogger("my error log").log(Level.INFO, "Serwer zostal uruchomiony" , serverSocket);
        while(true){
            Socket socket = serverSocket.accept();
            for(int i=0; i<2; i++){
                if(user[i]==null){
                    System.out.println("Connection from: " + socket.getInetAddress());
                    Logger.getLogger("my error log").log(Level.INFO, "Gracz: "+ i + " Przyszedl z:" ,socket.getInetAddress());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    user[i] = new Users(out,in,user,i);
                    Thread th = new Thread(user[i]);
                    th.start();
                    break;
                }
            }

        }

    }

}

/**
 * Klasa zczytujaca informacje od gracza i przesylajaca te informacje to wszytkich innych graczy
 */
class Users implements Runnable{
    DataOutputStream out;
    DataInputStream in;
    Users[] user = new Users[10];
    String name;
    int playerid;
    int playeridin;
    int xin;
    int yin;
    boolean right,left,up,down;
    boolean isBomb, isDead;


    public Users(DataOutputStream out, DataInputStream in, Users[] user, int pid){
        this.out = out;
        this.in = in;
        this.user = user;
        this.playerid= pid;

    }
    @Override
    public  void run() {
        try{
            out.writeInt(playerid);

        } catch (IOException e1) {
            System.out.println("Failed to send PlayerID");
            Logger.getLogger("my error log").log(Level.INFO, "Blad wyslania numeru id gracza: " , playerid);
        }
        while(true){
            try{

                playeridin = in.readInt();
                xin = in.readInt();
                yin = in.readInt();
                right = in.readBoolean();
                left = in.readBoolean();
                up = in.readBoolean();
                down = in.readBoolean();
                isBomb = in.readBoolean();
                isDead = in.readBoolean();

                for(int i =0; i<2; i++){
                    if(user[i] !=null){
                        user[i].out.writeInt(playeridin);
                        user[i].out.writeInt(xin);
                        user[i].out.writeInt(yin);
                        user[i].out.writeBoolean(right);
                        user[i].out.writeBoolean(left);
                        user[i].out.writeBoolean(up);
                        user[i].out.writeBoolean(down);
                        user[i].out.writeBoolean(isBomb);
                        user[i].out.writeBoolean(isDead);
                    }

                }
            } catch (IOException e) {
                user[playerid] = null;


            }
        }

    }
}
