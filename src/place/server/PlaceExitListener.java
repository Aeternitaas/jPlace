package place.server;

import java.util.Scanner;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ObjectInputStream;

/**
 * Threaded listener for an "Enter" key; shuts down the server. 
 * Additionally, closes the log.txt print stream.
 * 
 * @author Ket-Meng Cheng
 * @author Ethan Cantor
 */
public class PlaceExitListener extends Thread {

    /**
     * Scanner for keyboard input.
     */
    private Scanner s;

    /**
     * PrintWriter stream to be closed upon exiting.
     */
    private PrintWriter pw;

    /**
     * The variable to determine whether or not critical
     * while loops run or not (for both checking System.in
     * and the ClientServerThread generator).
     */
    private boolean go;

    /**
     * Map of all input streams to be closed upon receiving
     * the enter key.
     */
    private Map< String, ObjectInputStream > clientsIn;

    public PlaceExitListener( PrintWriter pw, boolean go, Map< String, ObjectInputStream > clientsIn ) {
        this.s = new Scanner( System.in );
        this.pw = pw;
        this.go = go;
        this.clientsIn = clientsIn;
    }

    public boolean getGo() {
        return this.go;
    }

    /**
     * Continually checks for a blank enter key press. 
     * Variable `go` will also shutdown the main thread-generation loop
     * in NetworkServer.
     */
    public void run() {
        while ( this.go ) {
            String st = s.nextLine();
            if ( st.equals( "" ) ) {
                this.go = false;
            }
        }
        this.pw.close();
        closeIn();
        System.out.println( "Shutting down..." );
    }

    /**
     * Iterates through all entries and closes input streams.
     */
    private void closeIn() {
        try {
            for ( String name : this.clientsIn.keySet() ) { 
                clientsIn.get( name ).close();
            } 
        } catch ( IOException ioe ) {
            System.out.println( ioe );
        }
    }

}
