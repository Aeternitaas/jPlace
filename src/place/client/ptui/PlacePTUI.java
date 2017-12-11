package place.client.ptui;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * The PlacePTUI class creates a plain text version of the board provided by the server. It allows the user to change tiles
 * through text input
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class PlacePTUI {

    /**
     * The client side board that is manipulated by commands from the server
     */
    public PlaceBoard board;

    /**
     * Username of this client
     */
    private String username;

    /**
     * The server socket connection
     */
    private Socket serverConn;

    /**
     * The output stream to the server
     */
    private ObjectOutputStream outputStream;

    /**
     * The input stream from the server
     */
    private ObjectInputStream inputStream;

    /**
     * A separate thread to listen from commands from the server
     */
    private PTUIServerListener serverListener;

    /**
     * The PlacePTUI provides a connection to the server and client using plain text
     * @param host hostname of the server
     * @param port port of the server
     * @param username username of the client
     */
    public PlacePTUI(String host, int port, String username){
        this.username = username;
        try{
            serverConn = new Socket(host, port);
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
            if(confirmConnect.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS){
                System.out.println("Login Success : " + confirmConnect.getData());
                PlaceRequest<?> boardRequest = (PlaceRequest<?>) inputStream.readUnshared();
                if(boardRequest.getType() == PlaceRequest.RequestType.BOARD){
                    board = (PlaceBoard)boardRequest.getData();
                    serverListener = new PTUIServerListener(inputStream, this);
                    serverListener.start();
                    System.out.println("Board recieved:");
                    System.out.println(board);
                    loop();
                }
            } else if(confirmConnect.getType() == PlaceRequest.RequestType.ERROR){
                System.err.println("Login unsuccessful : " + confirmConnect.getData());
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the core loop of the PTUI_client class. It waits for the client to input text in the form of row, column, color
     * to create a tile to send to the server
     * @throws IOException if there is no connection to the server
     */
    private void loop() throws IOException{
        Scanner in = new Scanner(System.in);
        String input;
        while(!(input = in.nextLine()).equals("-1")){
            try {
                String[] commands = input.split(" ");
                int row = Integer.parseInt(commands[0]);
                int col = Integer.parseInt(commands[1]);
                String color = commands[2];
                int colorInt = Integer.parseInt(color, 16);
                PlaceColor placeColor = PlaceColor.values()[colorInt];
                PlaceTile tile = new PlaceTile(row, col, username, placeColor ,System.currentTimeMillis());
                PlaceRequest<PlaceTile> tileRequest = new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, tile);

                outputStream.writeUnshared(tileRequest);
                outputStream.flush();
            } catch(NumberFormatException nfe){
                System.out.println("That command was not valid. Format: row col color");
            }
        }
        close();
    }

    /**
     * Closes the connection to server and stop the server listener thread
     */
    private void close(){
        serverListener.kill();
        try {
            serverConn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * The user provides some arguemnts for the PTUI client
     * @param args hostname, port, username
     */
    public static void main(String[] args){
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String username = args[2];
            new PlacePTUI(host, port, username);
        } catch (Exception e){
            System.err.println("There was an error inputting arguements : ");
            e.printStackTrace();
        }
    }
}
