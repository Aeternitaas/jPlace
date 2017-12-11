package place.client.gui;

import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * This class creates a separate thread from the GUI to wait for input from the server
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class GUIServerListener extends Thread {

    /**
     * Input stream from the server
     */
    private ObjectInputStream inputStream;

    /**
     * The client that is connected to the server
     */
    private PlaceGUI gui;

    /**
     * Should it be listening for input from the server
     */
    private boolean running;

    /**
     * Create a thread to listen for input from the server
     * @param inputStream Inputstream from the server
     * @param gui The client that is connected to the server
     */
    GUIServerListener(ObjectInputStream inputStream, PlaceGUI gui){
        this.inputStream = inputStream;
        this.gui = gui;
        this.running = true;
    }

    /**
     * This is the core loop of the GUIServerListener class. While running, it will listen for tile changes or errors
     * from the server and update its specific client
     */
    public void run(){
        while(running){
            try {
                PlaceRequest<?> serverUpdate = (PlaceRequest<?>) inputStream.readUnshared();
                if(serverUpdate.getType() == PlaceRequest.RequestType.TILE_CHANGED){
                    PlaceTile updatedTile = (PlaceTile) serverUpdate.getData();
                    System.out.println(updatedTile);
                    gui.board.setTile(updatedTile);
                    gui.updateBoardColors(updatedTile);
                } else if(serverUpdate.getType() == PlaceRequest.RequestType.ERROR){
                    System.out.println("Error in game loop : " + serverUpdate.getData());
                }
            } catch(IOException | ClassNotFoundException IOE) {
            }
        }
    }

    /**
     * Stop looking for input from the server
     */
    public void kill(){
        running = false;
    }

}
