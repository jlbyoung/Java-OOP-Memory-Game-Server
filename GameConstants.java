/**
 * Protocol for the server / client memory game
 * @author James Young based on G. Monagan lab 12
 * @version March 31, 2017
 */
 
 /**
 *  PROTOCOL:
	 * SET PLAYER : Sets the player to 0 or 1. 0 means the player should begin the game.
	 * PLAYING : Sets if it's the player's turn. 
	 * TRY : The user clicking the card. A player has maximum 2 cards to pick.
	 * SHOW : The server displays the specified card.
	 * COVER : Covers the specified card.
	 * PAIRS : Increases the amount of pairs the player has.
	 * DISABLE : Disables the specified cards that were matched.
	 * WINNER : Sets the player to winner.
	 * LOSE : Sets the player to loser.
	 * WAIT : Pauses the thread for a few seconds
	 * NEW : Start a new game
	 * QUIT : Forfeit the current game
 */
public interface GameConstants 
{
	/**
	* port
	*/
	int PORT = 2017;

	/**
	*/
	String HOST = "localhost";   
	
	public static final int SETPLAYER = 100;
	public static final int PLAYING = 101;
	public static final int SHOW = 102;
	public static final int COVER = 103;
	public static final int PAIRS = 104;
	public static final int DISABLE = 105;
	public static final int WINNER = 106;
	public static final int WAIT = 107;
	public static final int LOSE = 108;
	
	public static final int DONE = 666;

	public static final int TRY = 200;
	public static final int QUIT = 201;
	public static final int NEW = 202;
	/**
	* Converts an integer command cmd to its string representation. 
	* A command that is not suppored returns the string
	* "UNRECOGNIZABLE COMMAND".
	* @param cmd an integer corresponding to a command
	* @return String the textual representation of the command cmd
	*/ 
	default String cmdToString(int cmd)
	{
		String cmdString;
		switch (cmd)
		{
			case SETPLAYER:
				cmdString = "SETPLAYER";
				break;
			case PLAYING: 
				cmdString = "PLAYING";
				break;
			case TRY:
				cmdString = "TRY";
				break;
			case SHOW:
				cmdString = "SHOW";
				break;
			case COVER:
				cmdString = "COVER";
				break;
			case PAIRS: 
				cmdString = "PAIRS";
				break;
			case DISABLE:
				cmdString = "DISABLE";
				break;
			case WINNER:
				cmdString = "WINNER";
				break;
			case LOSE:
				 cmdString = "LOSE";
				break;
			case QUIT:
				cmdString = "QUIT";
				break;
			case NEW:
				cmdString = "NEW";
				break;
			case WAIT:
				cmdString = "WAIT";
				break;
			case DONE:
				cmdString = "DONE";
				break;
			default:
				cmdString = "UNRECOGNIZABLE COMMAND";
		}  // switch
		return cmdString;
	} // cmdToString
}
