package place.client.ptui;

import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * This class is connected to a specific PlacePTUI and listens for messages from the server
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class PTUIServerListener extends Thread {

    /**
     * Input stream from the server
     */
    private ObjectInputStream inputStream;

    /**
     * The connected PTUI
     */
    private PlacePTUI ptui;

    /**
     * A boolean to control if it should be looking for server input
     */
    private boolean running;

    /**
     * A separate thread to listen for server input for a PTUI
     * @param inputStream input stream from the server
     * @param ptui the connected PTUI
     */
    PTUIServerListener(ObjectInputStream inputStream, PlacePTUI ptui){
        this.inputStream = inputStream;
        this.ptui = ptui;
        this.running = true;
    }

    /**
     * The core loop of the PTUIServerListener class. This looks for tile changes and error messages from the server
     */
    public void run(){
        while(running){
            try {
                PlaceRequest<?> serverUpdate = (PlaceRequest<?>) inputStream.readUnshared();
                if(serverUpdate.getType() == PlaceRequest.RequestType.TILE_CHANGED){
                    PlaceTile updatedTile = (PlaceTile) serverUpdate.getData();
                    ptui.board.setTile(updatedTile);
                    System.out.println(ptui.board);
                } else if(serverUpdate.getType() == PlaceRequest.RequestType.ERROR){
                    System.out.println("Error in game loop : " + serverUpdate.getData());
                }
            } catch(IOException | ClassNotFoundException IOE) {
            }
        }

    }

    /**
     * A method to stop the thread from looking for server input
     */
    public void kill(){
        running = false;
    }

}
