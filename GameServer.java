import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.util.*;


import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
/**
	Server that handles all the commands of Memory game
	@author modified from Gladys Monagan Example Lab 12 Server
*/
public class GameServer extends JFrame implements GameConstants
{
	//Default frame dimensions
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 800;
	
	//GUI objects of the window
	private JTextArea log;
	private JButton commandButton;
	
	private Player player;
	
	private ImageIcon[] cardValues;
	private static final int DEFAULT_NUMBER_CARDS = 6;
	
		// to format the date  12-Nov-2016 10:15 PM
	private static DateTimeFormatter FORMATTER = 
	 DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
	
	 /**
	* Starts the server which creates a log window, opens one socket per client
	* and starts an encryption / decryption (in its own thread). The Server
	* begins the converstation by giving a client a name (a client number).
	* @param args line arguments -- not used
	*/
	public static void main(String[] args) 
	{
		new GameServer();
	}
		
	public GameServer()
	{
		createPanel();
		initCardValues();
		
		try (ServerSocket serverSocket = new ServerSocket(PORT))
		{
			 String nowStr = reportStatsOnServer();        
			 report("The server, port " + serverSocket.getLocalPort() 
				+ ", started on " + nowStr);
			int sessionNo = 1;

			while (true) 
			{
				// listen for a new connection request
				Socket socket1 = serverSocket.accept();
				reportStatsOnClient(socket1, 1);
				report("Player1 joined Session");
				
				Socket socket2 = serverSocket.accept();
				reportStatsOnClient(socket2, 2);
				report("Player2 joined Session");

				// create a client thread for the connection
				Runnable service = new HandleASession(cardValues, socket1, socket2, log);
				
				new Thread(service).start();
				
			 } // end while true
			 
		 } // end try
		 catch(IOException e) 
		 {
			 report("problems in server " + e.toString());
			 e.printStackTrace(System.err);
		 } // catch IOException
	} // Server

	private void initCardValues()
	{
		ImageIcon card0 = new ImageIcon("images//0.jpg");
		ImageIcon card1 = new ImageIcon("images//0.jpg");
		ImageIcon card2 = new ImageIcon("images//1.jpg");
		ImageIcon card3 = new ImageIcon("images//1.jpg");
		ImageIcon card4 = new ImageIcon("images//2.jpg");
		ImageIcon card5 = new ImageIcon("images//2.jpg");
		cardValues = new ImageIcon[] {
			 card0, card1, 
			 card2, card3, 
			 card4, card5 };
			 
		Random random = new Random();
		 //algorithm from https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
		 for (int i = cardValues.length - 1; i > 0; i--)
		 {
			int index = random.nextInt(i + 1);
			ImageIcon temp = cardValues[index];
			cardValues[index] = cardValues[i];
			cardValues[i] = temp;
		 }
	}
	/**
	* Writes onto the textAreaLog a string and adds a carriage return.
	* Scroll down so that the bottom of the textArea is always shown,
	* i.e. the last line is always displayed
	* @param msg the message to display.
	*/
	private void report(String msg)
	{
		 log.append(msg + '\n');
		 log.setCaretPosition(log.getDocument().getLength());
	} // report
	
	/**
	* Reports the server's IP address. <br> 
	* It has nothing to do with the clients.
	* Gives current time.
	* @return gives the date and time nicely formatter
	* @throws UnknownHostException due to not a proper server
	*/
	private String reportStatsOnServer() throws UnknownHostException
	{
		 report("This server's computer name is " + 
			 InetAddress.getLocalHost().getHostName());
		 report("This server's IP address is " + 
			 InetAddress.getLocalHost().getHostAddress() + "\n");
		 return LocalDateTime.now().format(FORMATTER);
	} // reportStatsOnServer

	/**
	* Reports the clients's domain name and IP address. <br /> 
	* .... just for fun
	* @param socket an open socket
	* @param n the client's number (starting with 1)
	*/
	private void reportStatsOnClient(Socket socket, int n)
	{

		 InetAddress  addr = socket.getInetAddress();
		 report("client " + n + "'s host name is " + addr.getHostName());
		 report("client " + n + "'s IP Address is " + addr.getHostAddress());
		 report("starting thread for client " + n + " at " + 
			 LocalDateTime.now().format(FORMATTER));
	} // reportStatsOnClient
	
	 /**
	 * Creates the window with GUI objects
	 */
	private void createPanel()
	{
		setResizable(false);
		JPanel panel = new JPanel();
		log = new JTextArea(60, 60);
		log.setEditable(false);

		panel.add(log);
		panel.add(new JScrollPane(log), BorderLayout.CENTER);
		add(panel);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setTitle("Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}