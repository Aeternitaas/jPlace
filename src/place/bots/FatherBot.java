package place.bots;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Fatherbot is the superclass for all bots. It creates the connection to the server as well as manages tile placement.
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class FatherBot {

    /**
     * Username of this bot
     */
    private String username;

    /**
     * Server socket connection
     */
    private Socket serverConn;

    /**
     * Outputstream from the server
     */
    private ObjectOutputStream outputStream;

    /**
     * Inputstream from the server
     */
    private ObjectInputStream inputStream;

    private PlaceBoard board;

    /**
     * This will connect to a server, provide the server with a username and then see if the server accepts it. If so,
     * it will also recieve a board.
     *
     * @param hostname hostname of the server
     * @param port port of the server
     * @param username username of the bot
     */
    public FatherBot(String hostname, int port, String username){
        this.username = username;

        try{
            serverConn = new Socket(hostname, port);
            outputStream = new ObjectOutputStream(serverConn.getOutputStream());
        } catch(IOException IOE){
            IOE.printStackTrace();
        }


        PlaceRequest<String> sendUser = new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username);
        try {
            outputStream.writeUnshared(sendUser);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream = new ObjectInputStream(serverConn.getInputStream());
            PlaceRequest<?> confirmConnect = (PlaceRequest<?>) inputStream.readUnshared();
            if (confirmConnect.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS) {
                System.out.println("Login Success : " + confirmConnect.getData());
                PlaceRequest<?> boardRequest = (PlaceRequest<?>) inputStream.readUnshared();
                if (boardRequest.getType() == PlaceRequest.RequestType.BOARD) {
                    board = (PlaceBoard) boardRequest.getData();
                }
            } else if (confirmConnect.getType() == PlaceRequest.RequestType.ERROR) {
                System.err.println("Login unsuccessful : " + confirmConnect.getData());
                System.exit(1);
            }
        } catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
        }
    }

    /**
     * The method sends a request to place a tile to the server
     * @param row row of the tile
     * @param col column of the tile
     * @param color color of the tile
     * @throws IOException if the server is not connected
     */
    public void placeTile(int row, int col, String color) throws IOException {
        int colorInt = Integer.parseInt(color, 16);
        PlaceColor placeColor = PlaceColor.values()[colorInt];
        PlaceTile tile = new PlaceTile(row, col, username, placeColor ,System.currentTimeMillis());
        PlaceRequest<PlaceTile> tileRequest = new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, tile);
        outputStream.writeUnshared(tileRequest);
        outputStream.flush();
    }

    /**
     * returns the size of the board, if it exists. If not it will return -1
     * @return board size or -1
     */
    public int getBoardSize(){
        int return_ = -1;
        try {
            return_ = board.DIM;
        } catch(NullPointerException ignored){}
        return return_;
    }

    /**
     * returns the board
     * @return board
     */
    public PlaceBoard getBoard(){
        return board;
    }
}
