package place.bots;

import place.PlaceTile;

import java.io.IOException;

/**
 * He protec
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class ProtecBot extends FatherBot{

    /**
     * The time between each iteration (in milliseconds)
     */
    private int time;

    /**
     * 2D array of tiles to protec
     */
    private PlaceTile[][] protecArea;

    /**
     * A single tile bot will place a tile at a set interval
     *
     * @param hostname hostname of the server
     * @param port port of the server
     * @param username username of the bot
     * @param startX the top left corner x value
     * @param startY the top left corner y value
     * @param endX the bottom right corner x value
     * @param endY the bottom right corner y value
     * @param time the time interval between iterations (in milliseconds)
     */
    public ProtecBot(String hostname, int port, String username, int startX, int startY, int endX, int endY, int time) {
        super(hostname, port, username);
        this.time = time;
        int xDif = endX - startX;
        int yDif = endY - startY;

        if(xDif < 0 || yDif < 0){
            System.err.println("The area of protection is not positive. Bot will self-destruct");
            System.exit(1);
        }

        protecArea = new PlaceTile[xDif][yDif];

        for(int r = 0; r < xDif; r++){
            for(int c = 0; c < yDif; c++){
                try {
                    protecArea[r + startX][c + startY] = getBoard().getTile(r + startX, c + startY);
                } catch(ArrayIndexOutOfBoundsException AIOBE){
                    System.err.println("The area of protection is not on or fully on the board");
                    System.exit(1);
                }
            }
        }
        loop();
    }

    /**
     * This is the core loop of the protection bot. It will replace each tile in an area with the original tile.
     */
    private void loop(){
        while(true) {
            try {
                for (PlaceTile[] aProtecArea : protecArea) {
                    for (PlaceTile anAProtecArea : aProtecArea) {
                        System.out.println(anAProtecArea.getRow() + " " + anAProtecArea.getCol() + " " + anAProtecArea.getColor().toString());
                        placeTile(anAProtecArea.getRow(), anAProtecArea.getCol(), anAProtecArea.getColor().toString());
                    }
                }
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
     * @param args hostname, port, username, row, column, end row, end column, time for each iteration (in milliseconds)
     */
    public static void main(String[] args){
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String username = args[2];
            int row = Integer.parseInt(args[3]);
            int col = Integer.parseInt(args[4]);
            int endRow = Integer.parseInt(args[5]);
            int endCol = Integer.parseInt(args[6]);
            int time = Integer.parseInt(args[7]);
            new ProtecBot(host, port, username, row, col, endRow, endCol, time);
        } catch (Exception e){
            System.err.println("There was an error inputting arguements \nFormat should be : host port username row col color timing(seconds): ");
            e.printStackTrace();
        }
    }
}
