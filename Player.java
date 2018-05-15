import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Client for the Encryption and Decryption program. <br />
 * Send commands to the Server in response to the GUI.  <br />
 * Written for CpSc 1181.  <br />
 * based on http://www.cs.armstrong.edu/liang/intro8e/book/Client.java
 * @author ******** your name ********
 * @version March 31, 2017
 */
public class Player extends JFrame implements Runnable, GameConstants
{   
	private static final int FRAME_WIDTH = 700;
	private static final int FRAME_HEIGHT = 375;
	
	private static final int CARD_WIDTH = 100;
	private static final int CARD_HEIGHT = 100;
	
	private final String DEFAULT_TEXT = "1181";
	
	//GUI objects of the window
	private JButton forfeitGameButton;
	private JButton newGameButton;
	private JLabel matchesField;
	private JLabel playerNumberField;
	private JTextArea gameStatusField;
	private JScrollPane scrollPane;
	
	private int currentPlayer;
	
	//The number the player is assigned to. 0 means the player should begin the game.
	private int playerNumber;
	
	//True if it's the player's turn, false otherwise.
	private boolean isYourTurn;
	
	private boolean gameEnd;
	
	//Number of pairs the player has matched
	private int matches;
	
	//Card buttons
	private Card[] cards;
	private Card card0;
	private Card card1;
	private Card card2;
	private Card card3;
	private Card card4;
	private Card card5;
	
	private ImageIcon cardBack;
	
	//Number of pickedCards the user has selected. If greater or equal to 2, the user cannot select any more cards.
	private int pickedCards;

	// the instance variables socket, toServer, and fromServer are 
	// needed because of the interface Runnable that has a method run 
	// that takes no arguments   
	private Socket socket;
	private DataOutputStream toServer;
	private DataInputStream fromServer;
	
	
	private int clientNumber;
	
	/**
	* Set up the GUI, connect to the server and decrypt and encrypt.
	* @param args the line commands -- not used
	*/
	public static void main(String[] args)
	{
		new Player(HOST);
	}

	/**
	* Sets up the Graphical User Interface s and it sets ups the I/O streams 
	* from a socket.
	* It then starts a thread if the socket to the server worked. 
	* @param serverHost IPAddress of server that is running already
	*/   
	public Player(String serverHost)
	{
		gameEnd = false;
		cardBack = new ImageIcon(getClass().getResource("images//cardback.jpg"));
		createButton();
		createFields();
		buildGUI();
		openConnection(serverHost);
		if(socket != null && !socket.isClosed())
		{
			(new Thread(this)).start();
		}
	
	} // Client

	
	public void openConnection(String serverHost)
	{
		try
		{
			this.socket = new Socket(serverHost, PORT);
			this.fromServer = new DataInputStream(socket.getInputStream());     
			this.toServer = new DataOutputStream(socket.getOutputStream());         
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();  
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	/** 
	* Continues sending and receiving data as long as the server does 
	* not send a DONE
	*/  
	@Override
	public void run()
	{
		boolean done = false;
		try
		{
			while(!done)
			{
				int msg = fromServer.readInt();
				if(msg == SETPLAYER)
				{
					this.setPlayer(fromServer.readInt());
					System.out.println("server -> player" + this.getPlayer() + " -> SETPLAYER " + this.getPlayer());
					this.setTitle("PLAYER " + this.getPlayer());
					gameStatusField.append("You are player " + this.getPlayer() + "\n");
				}
				else if(msg == PLAYING)
				{
					 if(this.getPlayer() == fromServer.readInt())
					 {
						setTurn(true);
					 }
					 else if(this.getPlayer() != fromServer.readInt())
					{
						setTurn(false);
					}
				}
				else if(msg == SHOW)
				{
					int numChars = fromServer.readInt();
					int index = fromServer.readInt();
					String cardValue = getCardValue(numChars);
					this.showCard(index, cardValue);
				}
				else if(msg == COVER)
				{
					threadWait();
					 int firstCard = fromServer.readInt();
					 int secondCard = fromServer.readInt();
					 this.coverCard(firstCard);
					 this.coverCard(secondCard);
				}
				else if(msg == PAIRS)
				{
					 if(getTurn())
					 {
						this.increaseMatches();
					}
				}
				else if(msg == DISABLE)
				{
					 int firstCard = fromServer.readInt();
					 int secondCard = fromServer.readInt();
					 this.disableCard(firstCard);
					 this.disableCard(secondCard);
				}
				else if(msg == WINNER)
				{
					win();
				}
				else if(msg == LOSE)
				{
					lose();
				}
				else if(msg == WAIT)
				 {
					threadWait();
				 }
				 else if(msg == DONE)
				 {
					 done = true;
				 }
				 else if(msg == NEW)
				 {
					 newGame();
				 }
				else
				{
					throw new IOException("Unknown message");
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace(System.err);
		}
		catch(Exception e)
		{
			
			e.printStackTrace(System.err);
		}
		finally
		{
			closeConnection();
		}
	} //run()
	
		/**
	 * Server gets the playerNumber
	* @return playerNumber
	*/
	public int getPlayer()
	{
		return playerNumber;
	}
	
	/**
	 * Server gets the current player
	* @return playerNumber
	*/
	public int getCurrentPlayer()
	{
		return playerNumber;
	}
	
	/**
	 * Server sets the player to either 0 or 1 with the *SETPLAYER* command. If player is set to 0, then that player will begin the game.
	* @param n , 0 or 1
	*/
	public void setPlayer(int n)
	{
		if(n == 0)
		{
			playerNumber = 0;
		}
		else if(n == 1)
		{
			playerNumber = 1;
		}
	} // setPlayer()
	
	/**
	 * Server sets the current player to either 0 or 1. If the playerNumber equals the current player, then it will be that player's turn.
	* @param n , 0 or 1
	*/
	public void setCurrentPlayer(int n)
	{
		if(n == 0)
		{
			currentPlayer = 0;
		}
		else if(n == 1)
		{
			currentPlayer = 1;
		}
	} // setCurrentPlayer()
	
	/**
	 * The server requests whether or not it is the player's turn. 
	* @return isYourTurn returns true if it's the players turn, false otherwise.
	*/
	public boolean getTurn()
	{
		return isYourTurn;
	}
	
	/**
	 * The server sets if it's the player's turn with the *PLAYING* command. When this command is executed, then the number of picked cards is also reset.
	* @param b true if it's the player's turn, false otherwise
	*/
	public void setTurn(boolean b)
	{
		isYourTurn = b;
		if(isYourTurn)
		{
			gameStatusField.append("Your turn. \n");
			pickedCards = 0;
		}
		else
		{
			gameStatusField.append("It's not your turn. \n");
		}
	}
	
	/**
	*@author Gladys Monagan Server example
	*/
	public String getCardValue(int numChars) throws IOException
	{
		String str = "";
		for (int i=0; i < numChars; i++)
		{
			str += fromServer.readChar();
		}
		return str;
	}
	
	/**
	 * The server displays the chosen card with the *SHOW* command.
	* @param leftNumber the index of the card specified
	* @param string the rightField's text
	*/
	public void showCard(int index, String cardValue) 
	{
		ImageIcon value = new ImageIcon(cardValue);
		cards[index].setIcon(value);
	}
	
	/**
	 * The server covers the two cards that have been picked with the *COVER* command.
	* @param index The index of the card to cover
	*/
	public void coverCard(int index)
	{
		cards[index].setIcon(cardBack);
	}
	
	/**
	 * The server increases the number of matches the player has with the *PAIRS* command.
	*/
	public void increaseMatches()
	{
		matches++;
		matchesField.setText("Number of Matches : " + matches);
	}
	
	/**
	 * The server sets the disabled flag to true when the player has matched a pair.
	* @param index The index of the specified card to disable
	*/
	public void disableCard(int index)
	{
		cards[index].disable();
		cards[index].setMatched(true);
	}
	
	/**
	 * The player forfeits the game during their turn, ends the game and disables buttons.
	*/
	private void forfeitGame() 
	{
		if(isYourTurn)
		{
			lose();
			try
			{
				toServer.writeInt(QUIT);
				closeConnection();
				gameStatusField.append("Game Over");
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * The player requests a new game and sets all of the GUI objects and parameters to original state.
	*/
	private void newGame()
	{
		if(isYourTurn)
		{
			matches = 0;
			for(int i = 0; i < cards.length; i++)
			{
				cards[i].setDisable(false);
			}
			gameStatusField.setText("");
			playerNumberField.setText("You are Player");
			try
			{
				toServer.writeInt(NEW);
				closeConnection();
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * The server sets the player to lose state. Cards are disabled.
	*/
	public void lose()
	{
		gameStatusField.append("You lost. \n");
		for(int i = 0; i < cards.length; i++)
		{
			cards[i].setDisable(true);
		}
	}
	
	/**
	 * The server sets the player to win state. Cards are disabled.
	*/
	public void win()
	{
		gameStatusField.append("You won! \n");
		for(int i = 0; i < cards.length; i++)
		{
			cards[i].setDisable(true);
		}
	}
	
	/**
	 * Displays an error message
	* @param string An error message.
	*/
	public void showError(String string)
	{
		gameStatusField.append("**ERROR : " + string + "** \n");
	}
	
	public void closeConnection()
	{
		try
		{
			if(socket != null && !socket.isClosed())
			{
				socket.close();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		socket = null;
	}

	/**
	 * Pauses the thread whenever the server sends the WAIT command
	*/
	public void threadWait() throws Exception
	{
		Thread.sleep(1000);
	}
	/**
	 * Creates the window with GUI objects
	*/
	private void createFields()
	{
		gameStatusField = new JTextArea("Welcome to Memory \n", 7, 30);
		gameStatusField.setEditable(false);
		matchesField = new JLabel("Number of Matches : ");
		playerNumberField = new JLabel("You are Player ");
	}
	
	/**
	 * Creates the newGame and quitGame buttons
	*/
	private void createButton()
	{
		forfeitGameButton = new JButton();
		newGameButton = new JButton();
		
		newGameButton.addActionListener(e -> newGame());
		forfeitGameButton.addActionListener(e -> forfeitGame());

	}
	
	/**
	* 
	*/
	public void buildGUI()
	{
		ActionListener listener = new CardListener();
		card0 = new Card(cardBack);
		card0.addActionListener(listener);
		card1 = new Card(cardBack);
		card1.addActionListener(listener);
		card2 = new Card(cardBack);
		card2.addActionListener(listener);
		card3 = new Card(cardBack);
		card3.addActionListener(listener);
		card4 = new Card(cardBack);
		card4.addActionListener(listener);
		card5 = new Card(cardBack);
		card5.addActionListener(listener);
		
		cards = new Card[] {card0, card1, card2, card3, card4, card5};
		
		scrollPane = new JScrollPane();
		newGameButton = new JButton();
		forfeitGameButton = new JButton();

		card0.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

		card1.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

		card2.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

		card3.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

		card4.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

		card5.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

		scrollPane.setViewportView(gameStatusField);
		newGameButton.setText("New Game");
		forfeitGameButton.setText("Forfeit Game");

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			 layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			 .addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
				 .addGroup(layout.createSequentialGroup()
				.addComponent(forfeitGameButton)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(newGameButton))
				 .addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					 .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					 .addComponent(playerNumberField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					 .addComponent(matchesField, GroupLayout.Alignment.LEADING))
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					 .addGroup(layout.createSequentialGroup()
					.addComponent(card3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(card4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(card5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					 .addGroup(layout.createSequentialGroup()
					.addComponent(card0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(card1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(card2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		layout.setVerticalGroup(
			 layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			 .addGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				 .addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					 .addComponent(card0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					 .addComponent(card1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					 .addComponent(card2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					 .addComponent(card3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					 .addComponent(card4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					 .addComponent(card5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				 .addGroup(layout.createSequentialGroup()
				.addComponent(playerNumberField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(matchesField)))
			.addGap(33, 33, 33)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				 .addComponent(newGameButton)
				 .addComponent(forfeitGameButton))
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setVisible(true);
	}  //buildGUI
	
	/**
	 * Listens to mouse clicks on the cards
	*/
	private class CardListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(isYourTurn && pickedCards < 2)
			{
				//Gets the button that was clicked on
				Object obj = e.getSource();
				Card card = (Card)obj;
				String string = card.getText();
				for(int i = 0; i < cards.length; i++)
				{
					if(cards[i].equals(card) && !cards[i].getDisabledStatus())
					{
						gameStatusField.append("Picked card in slot " + i + "\n");
						System.out.println("player" + playerNumber + " -> server -> TRY card " + i);
						try
						{
							pickedCards++;
							toServer.writeInt(TRY);
							toServer.writeInt(i);
						}
						catch(IOException ex)
						{
							showError("Server not connected");
						}
					}
					else if(cards[i].getMatchedStatus())
					{
						gameStatusField.append("This card has already been matched");
					}
				}
			}
			else if(pickedCards >= 2)
			{
				showError("You already picked 2 cards.");
			}
			else
			{
				showError("It's not your turn.");
			}
		}
	}
} // Client 