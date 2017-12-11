package place.server;

import place.PlaceException;
import place.network.NetworkServer;
import java.io.IOException;

public class PlaceServer {

    /** 
     * @param args port number, dimensions
     */
    public static void main( String[] args ) {
        // Case in which there aren't enough arguments.
        if ( args.length != 2 ) {
            System.out.println( "Usage: java PlaceServer dimensions port_num" );
            System.exit( 0 ); }

        try {
            NetworkServer nc = new NetworkServer( Integer.parseInt( args[1] ), Integer.parseInt( args[0] ) );
            nc.run();
        } catch ( PlaceException pe ) {
            System.out.println( pe );
            System.exit( 1 );
        } catch ( IOException ioe ) {
            System.out.println( ioe );
            System.exit( 2 );
        }
    }
}
