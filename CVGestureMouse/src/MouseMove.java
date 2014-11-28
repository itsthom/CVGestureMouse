import java.awt.AWTException;
import java.awt.Robot;
import java.util.Random;

public class MouseMove {

	public static final int FIVE_SECONDS = 5000;
    public static final int MAX_Y = 600;
    public static final int MAX_X = 600;
    
	public static void main(String[] args) throws AWTException, InterruptedException {
		
		Robot robot = new Robot();
        Random random = new Random();
        while (true) {
            robot.mouseMove(random.nextInt(MAX_X), random.nextInt(MAX_Y));
            Thread.sleep(FIVE_SECONDS);
        }
	    
	    

	}

}
