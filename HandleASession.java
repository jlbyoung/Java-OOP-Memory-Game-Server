import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

/** 
 * Defines the thread class for handling a new connection... with its run command
 * process the commands sent by the client 
 * 
 * @author James Young based on Gladys Monagan's code loosely based on code by Daniel Liang
 * @version March 31, 2017
 */

public class HandleASession implements Runnable, GameConstants
{
	// already opened socket
	private Socket socket1; 
	private Socket socket2; 
	// place where the messages of the log are reported
	private JTextArea textAreaLog;
	
	private DataInputStream fromClient2;
	private DataOutputStream toClient2;
	private DataInputStream fromClient1;
	private DataOutputStream toClient1;
	
	private ImageIcon[] cardValues;
	private int pickedCards;
	private int indexOfFirstCard;
	private int indexOfSecondCard;
	
	private int currentPlayer;

	// Masks on the console the secret message sent to the Client
	// so that it is not "reported" on the textAreaLog
	private static final boolean MASKING_OUTPUT_SECRET_TEXT = true;
	
	/**
	* Receives the open socket so that input/output streams can be attached
	* and it receives the JTextArea where the messages will be logged.
	* It keeps track of which client it's interacting with.
	* @param cV The array containing card's values 
	* @param s1 player 1 socket already opened
	* @param s2 player 2 socket already opened
	* @param tA a JTextArea that receives messages
	*/
	public HandleASession(ImageIcon[] cV, Socket s1, Socket s2, JTextArea tA) 
	{
		pickedCards = 0;
		cardValues = cV;
		socket1 = s1;
		socket2 = s2;
		textAreaLog = tA;
	}
	
	/**
	* Helper function to report messages to the textAreaLog
	* which is an instance variable. It keeps the last appended line
	* showing at the bottom.
	*/
	private void report(String direction, String msg)
	{
		textAreaLog.append(msg + '\n');
		textAreaLog.setCaretPosition(textAreaLog.getDocument().getLength());
	}
	
  /**
	* Runs a thread:
	*  - set up the DataInputStream and the DataOutputStream
	*  - call for the commands from the client to be executed
	*  - clean up
	*/
	public void run() 
	{
		try 
		{
			try
			{
				// create the data input and output streams
				// we put them here instead of in the constructor so that
				// we do not have to put a try catch in the constructor
				fromClient1 = new DataInputStream(socket1.getInputStream());
				toClient1 = new DataOutputStream(socket1.getOutputStream());
				fromClient2 = new DataInputStream(socket2.getInputStream());
				toClient2 = new DataOutputStream(socket2.getOutputStream());
				executeCmds();
			}
			finally  // close may throw an Exception
			{
				socket1.close();
				socket2.close();
			}
		}
		catch (Exception e)
		{
			// could be an IOException but also a NullPointerException
			// for clients' (students in the lab at Langara) to have a shorter message
			report("ERROR ", e.getMessage());
			// report("in run of HandleClient", e.toString() + "\n");
			// e.printStackTrace(System.err);
		}
	} // run
	
	/**
	* Execute all commands until the QUIT command is received from the client, 
	* i.e. continuously serve the client.
	* If there is an unknown command, then stop, do not continue.
	*/
	private void executeCmds() throws IOException
	{
		// send the client its name (number)
		report("to    ", cmdToString(SETPLAYER) + " " + 1);
		toClient1.writeInt(SETPLAYER);
		toClient1.writeInt(1);
		toClient1.flush();
		toClient2.writeInt(SETPLAYER);
		toClient2.writeInt(2);
		toClient2.flush();
		setCurrentPlayer(1);

		doPlaying();
		resetTryCards();
		// start listening to client's requests and respond to them  
		boolean done = false;
		while (!done) 
		{
			int cmd;
			if(currentPlayer == 1)
			{
				cmd = fromClient1.readInt();
			}
			else
			{
				cmd = fromClient2.readInt();
			}
			switch (cmd)
			{
				case QUIT:
					doQuit();
					done = true;
					break;
				case TRY:
				{
					doTry();
					if(pickedCards > 1)
					{
						coverCards(indexOfFirstCard, indexOfSecondCard);
					}
					endTurn();
					break;
				}
				case NEW:
					doNewGame();
					break;
				default:
					report("from", "unknown command " + 
						String.valueOf(cmd) + " received");
			
			done = true;
			} // switch
		} // while
	} // executeCmds
	
	/**
	 * Server gets the current player
	* @return playerNumber
	*/
	public int getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	/**
	 * Server sets the current player to either 1 or 2. 
	* @param n , 1 or 2
	*/
	public void setCurrentPlayer(int n)
	{
		currentPlayer = n;
	} // setCurrentPlayer()
	
	public void doPlaying() throws IOException
	{
		if(currentPlayer == 1)
		{
			toClient1.writeInt(PLAYING);
			toClient1.writeInt(1);
			toClient1.flush();
			toClient2.writeInt(PLAYING);
			toClient2.writeInt(1);
			toClient2.flush();
		}
		else if(currentPlayer == 2)
		{
			toClient1.writeInt(PLAYING);
			toClient1.writeInt(2);
			toClient1.flush();
			toClient2.writeInt(PLAYING);
			toClient2.writeInt(2);
			toClient2.flush();
		}
	}
	
	/**
	* Current player can click on a card to try and match. Maximum 2 cards per turn.
	*/
	private void doTry() throws IOException
	{
		if(currentPlayer == 1)
		{
			if(indexOfFirstCard == -1)
			{
				indexOfFirstCard = fromClient1.readInt();
				//Show the card to both players
				showCard(indexOfFirstCard, toClient2);
				showCard(indexOfFirstCard, toClient1);
				pickedCards++;
			}
			else if(indexOfFirstCard != -1 && indexOfSecondCard == -1)
			{
				indexOfSecondCard = fromClient1.readInt();
				showCard(indexOfSecondCard, toClient2);
				showCard(indexOfSecondCard, toClient1);
				pickedCards++;
				//If the two cards match
				if(doPairs(indexOfFirstCard, indexOfSecondCard))
				{
					toClient1.writeInt(PAIRS);
					toClient1.flush();
					disableCards(indexOfFirstCard, indexOfSecondCard);
				}
			}
		}
		else if(currentPlayer == 2)
		{
			if(indexOfFirstCard == -1)
			{
				indexOfFirstCard = fromClient2.readInt();
				showCard(indexOfFirstCard, toClient1);
				showCard(indexOfFirstCard, toClient2);
				pickedCards++;
			}
			else if(indexOfFirstCard != -1 && indexOfSecondCard == -1)
			{
				indexOfSecondCard = fromClient2.readInt();
				showCard(indexOfSecondCard, toClient1);
				showCard(indexOfSecondCard, toClient2);
				pickedCards++;
				if(doPairs(indexOfFirstCard, indexOfSecondCard))
				{
					toClient2.writeInt(PAIRS);
					toClient2.flush();
					disableCards(indexOfFirstCard, indexOfSecondCard);
				}
			}
		}
	}
	
	/**
	 * The server displays the chosen card 
	* @param number The index passed of the specified card to show
	* @param toClient The Client receiving which card to show
	*/
	public void showCard(int number, DataOutputStream toClient) throws IOException
	{
		String string = cardValues[number].toString();
		int numChars = string.length();
		int index = number;
		toClient.writeInt(SHOW);
		toClient.writeInt(numChars);
		toClient.writeInt(index);
		toClient.flush();
		for(int i = 0; i < numChars; i++)
		{
			toClient.writeChar(string.charAt(i));
		}
	}
	private void coverCards(int index1, int index2) throws IOException
	{
		toClient1.writeInt(COVER);
		toClient1.writeInt(index1);
		toClient1.writeInt(index2);
		toClient1.flush();
		toClient2.writeInt(COVER);
		toClient2.writeInt(index1);
		toClient2.writeInt(index2);
		toClient2.flush();
		resetTryCards();
	}
	
	private boolean doPairs(int index1, int index2) throws IOException
	{
		String firstStr = cardValues[index1].toString();
		String secondStr = cardValues[index2].toString();
		if(firstStr.equals(secondStr))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * The server disables specified cards when the player has matched a pair.
	* @param index1 index of the first card
	* @param index2 index of the second card
	*/
	public void disableCards(int index1, int index2) throws IOException
	{
		toClient1.writeInt(DISABLE);
		toClient1.writeInt(index1);
		toClient1.writeInt(index2);
		toClient1.flush();
		toClient2.writeInt(DISABLE);
		toClient2.writeInt(index1);
		toClient2.writeInt(index2);
		toClient2.flush();
		resetTryCards();
	}
	
	public void endTurn() throws IOException
	{
		if(currentPlayer == 1)
		{
			currentPlayer = 2;
			doPlaying();
			resetTryCards();
		}
		else
		{
			currentPlayer = 1;
			doPlaying();
			resetTryCards();
		}
	}
	
	/**
	* Resets the currentPlayer's cards that were pressed
	*/
	private void resetTryCards()
	{
		pickedCards = 0;
		indexOfFirstCard = -1;
		indexOfSecondCard = -1;
	}
	
	private void doNewGame() throws IOException
	{
		if(currentPlayer == 1)
		{
			toClient1.writeInt(NEW);
			toClient1.flush();
		}
		else if(currentPlayer == 2)
		{
			toClient2.writeInt(NEW);
			toClient2.flush();
		}
	}

	/**
	* Processes a server quit by notifying the client with a DONE.
	*/
	private void doQuit() throws IOException
	{
		if(currentPlayer == 1)
		{
			report("from", cmdToString(QUIT) + "  received");
		}

		if(currentPlayer == 2)
		{
			report("from", cmdToString(QUIT) + "  received");
		}
	} // doQuit

	
} // end class HandleClient