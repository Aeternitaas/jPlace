package place.bots;

import java.io.IOException;
import java.util.Random;

/**
 * Row by column bot will place tiles in row major order starting from the top left corner at set intervals
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class RowByColumnBot extends FatherBot {

    /**
     * Time between iterations, provided by the user in milliseconds
     */
    private int time;

    /**
     * Color of the placed tile, provided by the user
     */
    private String color;

    /**
     * A row by column bot will connect to a server (hostname and port) and place tiles of the provided color starting the
     * top right corner and progressing from there.
     * @param hostname hostname of the server provided by the user
     * @param port port of the server provided by the user
     * @param username username of the bot
     * @param color color of the tile to be placed
     * @param time time in between each iteration
     */
    public RowByColumnBot(String hostname, int port, String username, String color, int time) {
        super(hostname, port, username);
        this.time = time;
        this.color = color;
        loop();
    }

    /**
     * This is the core loop of the bot. It starts in the upper left corner and will place tiles in left to right, up to down
     * style until it reaches the end. Then it will repeat.
     */
    private void loop() {
        int size = getBoardSize();
        while (true) {
            if (size != -1) {
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        try {
                            placeTile(c, r, color);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                size = getBoardSize();
            }
        }
    }

    /**
     * The user provides a couple of arguments for the bot
     * @param args hostname, port, username, color, time (in milliseconds)
     */
    public static void main(String[] args) {
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String username = args[2];
            String color = args[3];
            int time = Integer.parseInt(args[4]);
            new RowByColumnBot(host, port, username, color, time);
        } catch (Exception e) {
            System.err.println("There was an error inputting arguements \nFormat should be : host port username color timing(milliseconds): ");
            e.printStackTrace();
        }
    }
}
