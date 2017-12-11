package place.bots;

import java.io.IOException;
import java.util.Random;

/**
 * Randomized bot will place random tiles in a random spot on the board at set intervals
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class RandomizedBot extends FatherBot{

    /**
     * Time (in milliseconds) for each tile placement request
     */
    private int time;

    /**
     * This bot will connect to a server (hostname and port are supplied through the commandline by the user) and spam a server, at interval time,
     * with randomized tiles
     * @param hostname hostname to connect (supplied by user)
     * @param port port to connect (supplied by user)
     * @param username username of the bot (supplied by user)
     * @param time time between tile requests (in milliseconds supplied by user)
     */
    public RandomizedBot(String hostname, int port, String username, int time) {
        super(hostname, port, username);
        this.time = time;
        loop();
    }

    /**
     * The core loop of the bot. It will loop forever once started. It selects a color and coordinates randomly and then
     * requests a tile every iteration. It then sleep for a time before looping again.
     */
    private void loop(){
        Random r = new Random();
        int size = getBoardSize();
        while(true) {
            String color = Integer.toString(r.nextInt(16), 16);
            if(size != -1) {
                try {
                    placeTile(r.nextInt(size), r.nextInt(size), color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                size = getBoardSize();
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * The user provides a couple of arguments to give the robot information
     * @param args host, port, username, time between iterations (in milliseconds)
     */
    public static void main(String[] args){
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String username = args[2];
            int time = Integer.parseInt(args[3]);
            new RandomizedBot(host, port, username, time);
        } catch (Exception e){
            System.err.println("There was an error inputting arguements \nFormat should be : host port username timing(milliseconds): ");
            e.printStackTrace();
        }
    }
}
