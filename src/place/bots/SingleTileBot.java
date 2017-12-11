package place.bots;

import java.io.IOException;

/**
 * Single tile bot places tile in one spot on the board at set intervals
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class SingleTileBot extends FatherBot{

    /**
     * The row of the tile to place
     */
    private int x;

    /**
     * The column of the tile to place
     */
    private int y;

    /**
     * The color of the tile to place
     */
    private String color;

    /**
     * The time between tile placements in milliseconds
     */
    private int time;

    /**
     * A single tile bot will place a tile at a set interval
     *
     * @param hostname hostname of the server
     * @param port port of the server
     * @param username username of the bot
     * @param x the row of the tile
     * @param y the column of the tile
     * @param color the color of the tile
     * @param time the time interval between iterations (in milliseconds)
     */
    public SingleTileBot(String hostname, int port, String username, int x, int y, String color, int time) {
        super(hostname, port, username);
        this.x = x;
        this.y = y;
        this.color = color;
        this.time = time;
        loop();
    }

    /**
     * This is the core loop of the single tile bot will place a single tile at coordinates supplied by the user at
     * a time (in milliseconds) supplied by the user
     */
    private void loop(){
        while(true) {
            try {
                placeTile(x, y, color);
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

    /**
     * The user provides a couple of arguements
     * @param args hostname, port, username, row, column, color, time for each iteration (in milliseconds)
     */
    public static void main(String[] args){
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String username = args[2];
            int row = Integer.parseInt(args[3]);
            int col = Integer.parseInt(args[4]);
            String color = args[5];
            int time = Integer.parseInt(args[6]);
            new SingleTileBot(host, port, username, row, col, color, time);
        } catch (Exception e){
            System.err.println("There was an error inputting arguements \nFormat should be : host port username row col color timing(seconds): ");
            e.printStackTrace();
        }
    }
}
