import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.Random;

public class Mouse {

	private double ORIG_MAX_X;
	private double ORIG_MAX_Y;
	private double MAX_Y;
	private double MAX_X;
	private double previousX;
	private double previousY;
	private static final double DEAD_ZONE = 25.0;
	private Robot robot;

	public Mouse(int originalWidth, int originalHeight) throws AWTException {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		MAX_X = screenSize.width;
		MAX_Y = screenSize.height;
		ORIG_MAX_X = originalWidth;
		ORIG_MAX_Y = originalHeight;
		previousX = 0;
		previousY = 0;
		robot = new Robot();
	}

	public void moveMouse(double dstX, double dstY) {
		double scaledDstX = Math.floor(dstX * MAX_X / (ORIG_MAX_X));
		double scaledDstY = Math.floor(dstY * MAX_Y / (ORIG_MAX_Y));
		double dx = scaledDstX - previousX;
		double dy = scaledDstY - previousY;
		double distance = Math.sqrt(dy*dy + dx*dx); 
		if (distance > DEAD_ZONE) {
			// TODO Smooth mouse motion (add steps)?
			robot.mouseMove((int) dstX, (int) dstY);
			previousX = scaledDstX;
			previousY = scaledDstY;
		}
	}

	public void leftClick () {
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	}

	public void leftButtonRelease () {
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

}
