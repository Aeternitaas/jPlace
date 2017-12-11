package place.network;

import place.PlaceException;
import place.PlaceBoard;
import place.server.PlaceExitListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Continually spawns ClientServerThreads for every new socket connection. 
 * Creates, but does not maintain Map of clients and PlaceBoard.
 * 
 * @author Ket-Meng Cheng
 * @author Ethan Cantor
 */
public class NetworkServer {

    /**
     * Server-side socket, opens specified port for incoming connections.
     */
    private ServerSocket server;

    /**
     * Server-side PlaceBoard used to store a copy to deliver to clients,
     * and ensure valid moves.
     */
    private PlaceBoard board;

    /**
     * Map of connected clients and their ObjectOutputStreams; used to update
     * all player boards. Additionally used to check for unique names.
     */
    private Map< String, ObjectOutputStream > clients;

    /**
     * Map for maintaining client input streams; used to gracefully close all streams
     * when exiting.
     */
    private Map< String, ObjectInputStream > clientsIn;

    /**
     * PrintWriter for writing to server logs.
     */
    private PrintWriter pw;

    public NetworkServer( int port, int dim ) throws PlaceException {
        try {
            this.server = new ServerSocket( port );
            // timeout added as to refresh, looking for a changed while(go).
            this.server.setSoTimeout( 1000 );
            this.clients = new HashMap<>();
            this.clientsIn = new HashMap<>();
            this.board = new PlaceBoard( dim );
            // stores log information in log.txt.
            this.pw = new PrintWriter( new FileWriter( "log.txt" ) );
        } catch ( IOException ioe ) {
            throw new PlaceException( ioe );
        }
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

    /**
     * Main routine for generating ClientServerThreads that listen to
     * client connetions.
     */
    public void run() throws PlaceException, IOException {
        try {
            logEntry( "Server started." );

            // starts the exit listener that polls for Enter.
            PlaceExitListener listener = new PlaceExitListener( this.pw, true, clientsIn );
            listener.start();

            // will stop looping upon pressing the Enter key.
            while( listener.getGo() ) {
                System.out.println( "Listening for new players..." );
                try {
                    // .accept() will stop when .close() is called in the PlaceExitListener, 
                    // exiting out of the lock.
                    Socket sock = this.server.accept();
                    ObjectInputStream in = new ObjectInputStream( sock.getInputStream() );
                    ObjectOutputStream out = new ObjectOutputStream( sock.getOutputStream() );

                    PlaceRequest< ? > req = ( PlaceRequest< ? > ) in.readUnshared();
                    String name = (String) req.getData();

                    logEntry( name + " has connected." );

                    // if there is no person with that name, continue as planned.
                    if ( !this.clients.containsKey( name ) ) {
                        ( new ClientServerThread( name, sock, in, out, pw, board, clients, listener.getGo() ) ).start();
                        // associates client names with both their output streams (for sending)
                        // and input streams (for closing when done).
                        clients.put( name, out );
                        clientsIn.put( name, in );
                    }
                    // if name exists, reject connection.
                    else {
                        String errorMsg = "Someone has already logged onto the server with the username " + name;
                        PlaceRequest<String> usernameErr = new PlaceRequest<>(PlaceRequest.RequestType.ERROR, errorMsg);
                        out.writeUnshared(usernameErr);
                        out.flush();
                        in.close();
                        out.close();
                        sock.close();
                    }
                } catch ( SocketTimeoutException ste ) {
                }
            }
        } catch ( IOException | ClassNotFoundException e ) {
            System.out.println( e + "ERROR!" );
        }
    }
}
