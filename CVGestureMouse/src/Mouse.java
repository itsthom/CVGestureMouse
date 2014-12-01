import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.Random;

public class Mouse {

	private double scaleFactorX;
	private double scaleFactorY;
	private double previousX;
	private double previousY;
	private static final double DEAD_ZONE = 15.0;
	private Robot robot;

	public Mouse(int originalWidth, int originalHeight) throws AWTException {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		scaleFactorX = (screenSize.width - 1) / (originalWidth - 1);
		scaleFactorY = (screenSize.height - 1) / (originalHeight - 1);
		previousX = 0;
		previousY = 0;
		robot = new Robot();
	}

	public void moveMouse(double dstX, double dstY) {
		dstX = dstX * scaleFactorX + 1;
		dstY = dstY * scaleFactorY + 1;
		double dx = dstX - previousX;
		double dy = dstY - previousY;
		double distance = Math.sqrt(dy*dy + dx*dx); 
		if (distance > DEAD_ZONE) {
			robot.mouseMove((int) dstX, (int) dstY);
			previousX = dstX;
			previousY = dstY;
		}
	}

	public void leftClick () {
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	}

	public void leftButtonRelease () {
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

}
