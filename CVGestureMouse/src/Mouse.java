import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Random;

public class Mouse {

	public int ORIG_MAX_X;
	public int ORIG_MAX_Y;
	
	public int MAX_Y;
    public int MAX_X;
    
	public int currentX;
	public int currentY;
	
	public int previousX;
	public int previousY;
	
	
	
	public Mouse(int originalWidth, int originalHeight) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		MAX_X = screenSize.width;
		MAX_Y = screenSize.height;
		ORIG_MAX_X = originalWidth;
		ORIG_MAX_Y = originalHeight;
		currentX = 0;
		currentY = 0;
		previousX = 0;
		previousY = 0;
	}
	
	public void moveMouse(int dstX, int dstY) {
		
	}

}
