package place.network;

import place.PlaceBoard;
import place.PlaceTile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * Client thread that handles individual communications with each client
 * that connects.
 * 
 * @author Ket-Meng Cheng
 * @author Ethan Cantor
 */
public class ClientServerThread extends Thread{
    
    /**
     * Socket connection for each client.
     */
    private Socket sock;

    /**
     * Input stream that handles incoming protocol messages.
     */
    private ObjectInputStream in;

    /**
     * Output stream that handles outcoming protocol messages.
     */
    private ObjectOutputStream out;

    /**
     * PrintWriter for writing to log.
     */
    private PrintWriter pw;

    /**
     * Name of each of the clients.
     */
    private String name;

    /**
     * Server-side board instance used to ensure valid moves.
     */
    private PlaceBoard board; 

    /**
     * Map used to ensure unique clients and handle distribution
     * of board updates.
     */
    private Map< String, ObjectOutputStream > clients; 

    /**
     * Determinant on whether or not to continue playing.
     */
    private boolean go;
    
    public ClientServerThread( String name, Socket sock, ObjectInputStream in, ObjectOutputStream out, PrintWriter pw, PlaceBoard board, Map< String, ObjectOutputStream > clients, boolean go ) throws IOException, ClassNotFoundException { 
        this.sock = sock; 
        this.name = name;
        this.in = in;
        this.out = out;
        this.pw = pw;
        this.board = board;
        this.clients = clients;
        this.go = go;
    }

    /**
     * Main run method for the thread, used to communicate a login
     * confimation and a representation of a board.
     */
    public synchronized void run() {
        System.out.println( "Player " + this.name + " (" + this.sock.getRemoteSocketAddress() + ") has connected!" );
        PlaceRequest<String> loginSuccess = new PlaceRequest<>( PlaceRequest.RequestType.LOGIN_SUCCESS, "You have connected to the server, you are now playing on a " + this.board.DIM + "x" + this.board.DIM + " board." );
        PlaceRequest<PlaceBoard> boardReq = new PlaceRequest<>( PlaceRequest.RequestType.BOARD, this.board );

        try {
            logEntry( "Login successful for " + name );
            this.out.writeUnshared( loginSuccess );
            this.out.flush();
            this.out.writeUnshared( boardReq );
            this.out.flush();

            playGame();
            
        } catch ( InterruptedException | ClassNotFoundException | IOException ioe ) {
            System.out.println( ioe );
        } 
    }

    /**
     * Main game loop that continually reads in client messages.
     */
    private void playGame() throws IOException, ClassNotFoundException, InterruptedException {
        try {
            while ( go ) {
                PlaceRequest< ? > req = ( PlaceRequest< ? > ) this.in.readUnshared();

                if ( req.getType() == PlaceRequest.RequestType.CHANGE_TILE ) {
                    PlaceTile tile = (PlaceTile) req.getData();
                    if ( this.board.isValid( tile ) ) {
                        System.out.println( tile );
                        this.board.setTile( tile );
                        sendUpdates( tile, this.clients );
                    }
                    else {
                        sendError( "Invalid tile placement." );
                    }
                } 
                // cooldown between moves.
                sleep( 50 );
            }
            this.in.close();
            this.out.close();
            this.sock.close();
        } catch ( EOFException | SocketException se ) {
            System.out.println( "Player " + name + " disconnected." );
            removePlayer( this.name, this.clients );
        }
    }

    /**
     * Removes the specified player from the map of clients.
     * 
     * @param name client name to be deleted.
     * @param clients map of clients.
     */
    private synchronized static void removePlayer( String name, Map< String, ObjectOutputStream > clients ) {
        clients.remove( name );
    }

    /**
     * Sends updates to every client within the map.
     *
     * @param tile the tile to be updated.
     * @param clients the map of clients to be updated.
     */
    private synchronized void sendUpdates( PlaceTile tile, Map< String, ObjectOutputStream > clients ) throws IOException, InterruptedException {
        PlaceRequest<PlaceTile> tileChange = new PlaceRequest<>( PlaceRequest.RequestType.TILE_CHANGED, tile );
        logEntry( "Sending tiles to players: \n" + tile.toString() );

        // for every client, send them a changed tile.
        for ( String name : clients.keySet() ) {
            ObjectOutputStream out = clients.get( name );
            out.writeUnshared( tileChange );
            out.flush();
        }
    }

    /**
     * Sends an error message back to the client.
     * 
     * @param string error message.
     */
    private void sendError( String string ) throws IOException {
        logEntry( string );
        PlaceRequest< String > error = new PlaceRequest( PlaceRequest.RequestType.ERROR, string );
        this.out.writeUnshared( error );
        this.out.flush();
    }

    /**
     * Generates a log entry with timestamps.
     * 
     * @param msg the message to be added to the log.
     */
    public void logEntry( String msg ) {
        // generates timestamps.
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        this.pw.println( "[" + df.format( cal.getTime() ) + "]" );
        this.pw.println( msg );
        this.pw.println();
    }
}
