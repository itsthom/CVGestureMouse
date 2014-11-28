import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class CameraViewPanel extends JPanel {
	
	private BufferedImage image;
	int count = 0;
	
	public CameraViewPanel() {
		super();
	}
	
	public void setFace(BufferedImage img) {
		image = img;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (this.image == null) {
			System.out.println("JPanel Image is null.");
			return;
		}
		
		g.drawImage(this.image, 10, 10, this.image.getWidth(), this.image.getHeight(), null);
//		g.setFont(new Font("arial", 2, 20));
//		g.setColor(Color.WHITE);
//		g.drawString("Frame: " + (count++), 20, 40);
	}
}
