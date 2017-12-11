package place.client.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.network.PlaceRequest;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Map;

/**
 * The GUI_client class creates a graphical user interface for the user to view and change a board provided by a server.
 *
 * @author Ethan Cantor
 * @author Ket-Meng Cheng
 */
public class PlaceGUI extends Application {

    /**
     * The scale of the GUI
     */
    private static final int SCALE = 2;

    /**
     * An integer to hold which color is currently selected. Default is black
     */
    private int selected = 0;

    /**
     * A clientside board that is only changed with permission from the server
     */
    PlaceBoard board;

    /**
     * A 2D array of rectangles used to display the client side board in the GUI
     */
    private Rectangle[][] GUI_board;

    /**
     * The dimensions of the board (length * width)
     */
    private int dim;

    /**
     * The canvas that the board is drawn onto
     */
    private Canvas canvas;

    /**
     * Username of this client
     */
    private String username;

    /**
     * Buttons used to select the color
     */
    private Button[] colorButton;

    /**
     * The connection to the server on the provided hostname and port
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
     * A thread that handles input from the server
     */
    private GUIServerListener listener;

    /**
     * A tooltip that follows the mouse to let the user know information about the tile that they are hovering over
     */
    private Tooltip tooltip;

    /**
     * Where the command line parameters will be stored once the application
     * is launched.
     */
    private Map< String, String > params = null;

    /**
     * Create javafx GUI
     * @param mainStage The main stage to start
     */
    @Override
    public void start(Stage mainStage) throws Exception {
        mainStage.setOnCloseRequest(event -> close());
        BorderPane borderPane = new BorderPane();
        GridPane colorGrid = new GridPane();
        colorButton = new Button[PlaceColor.TOTAL_COLORS];

        canvas = new Canvas(400 * SCALE, 400 * SCALE);
        canvas.setOnMouseClicked(event -> canvasClicked(event.getScreenX(), event.getScreenY(), event.getX(), event.getY()));

        tooltip = new Tooltip("");
        tooltip.setHideOnEscape(true);
        canvas.setOnMouseMoved(event -> setTooltip(event.getScreenX(), event.getScreenY(), event.getX(), event.getY()));

        canvas.setOnMouseEntered(event -> tooltip.setOpacity(1));
        canvas.setOnMouseExited(event -> tooltip.setOpacity(0));

        GUI_board = new Rectangle[(int) Math.sqrt(dim)][(int) Math.sqrt(dim)];
        selected = 3;
        for (int r = 0; r < Math.sqrt(dim); r++) {
            for (int c = 0; c < Math.sqrt(dim); c++) {
                updateBoardColors(board.getTile(r, c));
            }
        }
        selected = 0;

        PlaceColor[] colors = PlaceColor.values();
        for (int i = 0; i < PlaceColor.TOTAL_COLORS; i++) {
            java.awt.Color convertColor = new java.awt.Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue());
            String hex = "#" + Integer.toHexString(convertColor.getRGB()).substring(2);
            colorButton[i] = new Button();
            colorButton[i].setStyle("-fx-base: " + hex);
            colorButton[i].setPrefSize(canvas.getWidth() / PlaceColor.TOTAL_COLORS, 20 * SCALE);
            colorButton[i].setText(colors[i].toString());
            final int curr_button = i;
            colorButton[i].setOnAction(event -> buttonClicked(curr_button));
            colorGrid.add(colorButton[i], i, 0);
        }

        borderPane.setCenter(canvas);
        borderPane.setBottom(colorGrid);

        mainStage.setTitle("r/place : " + username);
        mainStage.setScene(new Scene(borderPane));
        mainStage.setResizable(false);
        mainStage.show();

        listener = new GUIServerListener(inputStream, this);
        listener.start();
    }

    /**
     * Creates and manipulates the tooltip. The information of the tooltip is:
     * The row of the tile
     * The column of the tile
     * The owner of the tile
     * The color of the tile
     * The time the tile was place
     *
     * @param screenX x-coordinate of the mouse on the screen
     * @param screenY y-coordinate of the mouse on the screen
     * @param x x-coordinate of the mouse on the canvas
     * @param y y-coordinate of the mouse on the canvas
     */
    private void setTooltip(double screenX, double screenY, double x, double y){
        int r = (int) (x / (canvas.getWidth() / Math.sqrt(dim)));
        int c = (int) (y / (canvas.getWidth() / Math.sqrt(dim)));

        String tileName = board.getTile(r, c).getOwner();
        String tileColor = board.getTile(r, c).getColor().toString();
        long tileTimeMili = board.getTile(r, c).getTime();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tileTimeMili);

        String tileTime = cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH) + "." + cal.get(Calendar.YEAR) +
                ": " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
        String tooltipMsg = "row: " + r + "\n" +
                "column: " + c + "\n" +
                "owner: " + tileName + "\n" +
                "color: " + tileColor + "\n" +
                "time: " + tileTime;
        tooltip.setText(tooltipMsg);
        tooltip.show(canvas, screenX + 25, screenY + 25);
    }

    /**
     * As the client window is closed. Stop the connection to the server and stop listening for server input.
     * Then end the program
     */
    private void close(){
        listener.kill();
        try {
            serverConn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Setup network connection, login to the server, and recieve the board
     */    public void init(){
        String port = "";
        String host = "";
        try {
            port = getParamNamed("port");
            host = getParamNamed("host");
            username = getParamNamed("username");
        } catch(PlaceException PE){
            PE.printStackTrace();
        }
        try {
            serverConn = new Socket(host, Integer.parseInt(port));
            outputStream = new ObjectOutputStream(serverConn.getOutputStream());
            outputStream.flush();
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
                    dim = board.getBoard().length * board.getBoard().length;
                }
            } else if(confirmConnect.getType() == PlaceRequest.RequestType.ERROR){
                System.err.println("Login unsuccessful : " + confirmConnect.getData());
                JOptionPane.showMessageDialog(null,"Login unsuccessful : " + confirmConnect.getData());
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method called when the user clicks on a button
     * @param button which button was clicked
     */
    private void buttonClicked(int button) {
        PlaceColor[] colors = PlaceColor.values();
        colorButton[selected].setText("" + colors[selected].toString());
        selected = button;
        colorButton[selected].setText("[" + colors[selected].toString() + "]");
    }

    /**
     * A method called when the user clicks on the canvas
     * @param x x-coordinate of the click
     * @param y y-coordinate of the click
     */
    private void canvasClicked(double screenX, double screenY, double x, double y){
        int r = (int) (x / (canvas.getWidth() / Math.sqrt(dim)));
        int c = (int) (y / (canvas.getWidth() / Math.sqrt(dim)));
        PlaceColor[] colors = PlaceColor.values();
        PlaceColor currColor = colors[selected];
        long time = System.currentTimeMillis();
        PlaceTile tile = new PlaceTile(r, c,username, currColor, time);

        PlaceRequest<PlaceTile> tileRequest = new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, tile);
        try {
            outputStream.writeUnshared(tileRequest);
            outputStream.flush();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        setTooltip(screenX, screenY, x, y);
    }

    /**
     * Updates a single tile on the GUI
     * @param tile which tile to update
     */
    void updateBoardColors(PlaceTile tile){
        PlaceColor currColor = tile.getColor();
        Color color = new Color(currColor.getRed() / 255., currColor.getGreen() / 255., currColor.getBlue() / 255., 1);
        GUI_board[tile.getRow()][tile.getCol()] = new Rectangle(canvas.getWidth() / Math.sqrt(dim) * tile.getRow(),canvas.getWidth() / Math.sqrt(dim) * tile.getCol(),
                Math.floor(canvas.getWidth() / Math.sqrt(dim)), Math.floor(canvas.getHeight() / Math.sqrt(dim)));
        GraphicsContext gfx = canvas.getGraphicsContext2D();
        gfx.setFill(color);
        gfx.fillRect(GUI_board[tile.getRow()][tile.getCol()].getX(), GUI_board[tile.getRow()][tile.getCol()].getY(),
                GUI_board[tile.getRow()][tile.getCol()].getWidth(), GUI_board[tile.getRow()][tile.getCol()].getHeight());
    }

    /**
     * Look up a named command line parameter (format "--name=value")
     * @param name the string after the "--"
     * @return the value after the "="
     * @throws PlaceException if name not found on command line
     */
    private String getParamNamed( String name ) throws PlaceException{
        if ( params == null ) {
            params = super.getParameters().getNamed();
        }
        if ( !params.containsKey( name ) ) {
            throw new PlaceException(
                    "Parameter '--" + name + "=xxx' missing."
            );
        }
        else {
            return params.get( name );
        }
    }

    /**
     * Start the GUI clinet
     * @param args host, port, username
     */
    public static void main(String[] args){
        Application.launch(args);
    }

}
