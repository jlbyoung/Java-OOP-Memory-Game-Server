import javax.swing.*;
import java.awt.*;

/**
 * A card button that user clicks on to match pairs
*/
public class Card extends JButton
{
	//True if the button is disabled, false otherwise
	private boolean isDisabled;
	private boolean isMatched;
	
	public Card(Icon icon)
	{
		super(icon);
		isDisabled = false;
		isMatched = false;
	}
	
	public void setMatched(boolean b)
	{
		isMatched = b;
	}
	public boolean getMatchedStatus()
	{
		return isMatched;
	}
	
	public void setDisable(boolean b)
	{
		isDisabled = b;
	}
	
	public boolean getDisabledStatus()
	{
		return isDisabled;
	}
}