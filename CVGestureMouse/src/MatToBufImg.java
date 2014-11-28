import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;


public class MatToBufImg {
	Mat matrix;
	MatOfByte mob;
	String fileExten;
	
	public MatToBufImg() {}
	
	public MatToBufImg(Mat mat, String fileExt) {
		this.matrix = mat;
		this.fileExten = fileExt;
	}
	
	public void setMatrix(Mat mat, String fileExt) {
		this.matrix = mat;
		this.fileExten = fileExt;
		this.mob = new MatOfByte();
	}
	
	public BufferedImage getBufferedImage() {
		// convert the matrix into a matrix of bytes appropriate for this file extension
		Highgui.imencode(fileExten, matrix, mob);
		
		// convert the matrix of bytes into a byte array
		byte[] byteArray = mob.toArray();
		BufferedImage bufImg = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImg = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bufImg;
	}
}
