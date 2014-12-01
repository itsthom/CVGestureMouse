import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;


public class CVGestureMouse {

	private static final int CAPTURE_WIDTH = 640;
	private static final int CAPTURE_HEIGHT = 480;
	private static final int MIN_CONTOUR_SIZE = 300;

	public static void main(String[] args) throws Exception {

		// Set up opservation frame
		JFrame frame = new JFrame("Webcam Capture");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700, 700);
		CameraViewPanel facePanel = new CameraViewPanel();
		frame.setContentPane(facePanel);

		// Load native library, set up camera
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture webCam = new VideoCapture(0);
		webCam.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, CAPTURE_WIDTH);
		webCam.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, CAPTURE_HEIGHT);
		if (!webCam.isOpened()) {
			System.out.println("Could not connect to camera.");
		} else {
			System.out.println("found webmcam: " + webCam.toString());
		}

		// Open and read from video stream
		if (webCam.isOpened()) {
			// Delay to allow camera initialization
			Thread.sleep(500);
			frame.setVisible(true);

			Mat img = new Mat();
			Mat imgGray = new Mat();
			Mat imgBinary = new Mat();
			MatToBufImg matToBufferedImageConverter = new MatToBufImg();
			GestureClass currentClass;
			GestureClass previousClass = new GestureClass();
			Point currentPos;
			Point previousPos = new Point(0,0);
			Mouse mouse = new Mouse(CAPTURE_WIDTH, CAPTURE_HEIGHT);

			while (true) {
				webCam.read(img);
				if (!img.empty()) {

					// threshold into a binary image
					Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
					Imgproc.threshold(imgGray, imgBinary, 75, 255, Imgproc.THRESH_BINARY);

					// findContours Params
					ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					Mat hierarchy = new Mat();

					// find contours, then first one that might be a hand
					Imgproc.findContours(imgBinary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
					if (contours.size() > 0) {
						int index = 0;
						for (int i=0; i < contours.size(); i++) {
							if (contours.get(i).toArray().length > MIN_CONTOUR_SIZE) {
								index = i;
								break;
							}
						}

						// Classify
						currentClass = GestureClass.classifyContour(contours.get(index).toArray(), img, previousClass.name, previousPos);

						if (previousClass.name == ClassName.MOVING && currentClass.name == ClassName.LEFT_CLICK_MOVING) {
							mouse.leftClick();
						}
						if (previousClass.name == ClassName.LEFT_CLICK_MOVING && currentClass.name == ClassName.MOVING) {
							mouse.leftButtonRelease();
						}

						previousClass = currentClass;

						// draw contour and output image to Frame
						Imgproc.drawContours(img, contours, index, new Scalar(72,0,255), 2);
						Core.putText(img, currentClass.name.name(), new Point(5.0,30.0), Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(72,0,255), 2);

						// move cursor for moving classes
						if (currentClass.name == ClassName.MOVING || currentClass.name == ClassName.LEFT_CLICK_MOVING) {
//							String pos = "X: " + currentClass.position.x + " Y: " + currentClass.position.y;
//							Core.putText(img, pos, new Point(5.0,42.0), Core.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(72,0,255), 1);
							currentPos = currentClass.position;
							mouse.moveMouse(currentPos.x, currentPos.y);
							previousPos = currentPos;
						}

					}

					matToBufferedImageConverter.setMatrix(img, ".jpg");
					BufferedImage bufImg = matToBufferedImageConverter.getBufferedImage();
					facePanel.setFace(bufImg);
					facePanel.repaint();

				} else {
					System.out.println("Nothing captured from cam.");
					break;
				}
			}

		}

		webCam.release();

	}

}
