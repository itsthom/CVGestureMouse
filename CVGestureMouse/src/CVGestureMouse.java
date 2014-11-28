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

	private static final int CAPTURE_WIDTH = 320;
	private static final int CAPTURE_HEIGHT = 240;
	
	public static void main(String[] args) throws Exception {
		
		// Set up opservation frame
		JFrame frame = new JFrame("Webcam Capture");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(360, 280);
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
			Thread.sleep(500); // Delay to allow camera initialization
			int frameNo = 0;
			frame.setVisible(true);
			
			Mat img = new Mat();
			Mat imgGray = new Mat();
			Mat imgBinary = new Mat();
			MatToBufImg matToBufferedImageConverter = new MatToBufImg(); // converts Mat to Java BufferedImage for display
			GestureClass currentClass;
			Mouse mouse = new Mouse(CAPTURE_WIDTH, CAPTURE_HEIGHT);
			
			while (true) {
				webCam.read(img);
				if (!img.empty()) {
					frameNo++;
					
					// threshold into a binary image
					Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
					Imgproc.threshold(imgGray, imgBinary, 75, 255, Imgproc.THRESH_BINARY);
					
					// findContours Params
					ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					Mat hierarchy = new Mat();
					
					// find contours, then find max contour
					// TODO - instead of finding the largest region, find the one based on perimeter length that's in "hand range"
					Imgproc.findContours(imgBinary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
					if (contours.size() > 0) {
						double maxArea = 0.0;
						int maxIndex = 0;
						for (int i=0; i < contours.size(); i++) {
							double area = Imgproc.contourArea(contours.get(i));
							if (area > maxArea) {
								maxArea = area;
								maxIndex = i;
							}
						}
												
						// Classify
						currentClass = classifyContour(contours.get(maxIndex).toArray());
						
						// draw contour and output image to Frame
						Imgproc.drawContours(img, contours, maxIndex, new Scalar(0,0,255), 2);
						Core.putText(img, currentClass.name.name(), new Point(5.0,30.0), Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0,0,255), 2);
						
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
	
	private static GestureClass classifyContour(Point[] contour) {
		// TODO classification doesn't work - may need to rewrite and adjust thresholds
		int peaks = 0, valleys = 0;
		int k = 50; // distance from center point for getting vectors
		if (contour.length <= k)
			return new GestureClass();
		
		double[] angles = new double[contour.length]; // angles between vectors i-k,i and i,i+k
		double peakThresh = 3.10; // positive angle threshold for peaks
		double valleyThresh = -3.10; // negative angle threshold for valleys
		
		for (int i = 0; i < contour.length; i++) {
			int a, b;
			if (i < k)
				a = contour.length - (k-i);
			else
				a = i-k;
			
			if (i >= (contour.length - k))
				b = (i + k) % k;
			else
				b = i+k;
			
			double ay = contour[a].y - contour[i].y, 
					ax = contour[a].x - contour[i].x,
					by = contour[b].y - contour[i].y, 
					bx = contour[b].x - contour[i].x;
			angles[i] = Math.atan2(ax*by - ay*bx, ax*bx + ay*by); //perp dot product
		}
		
		for (int i = 0; i < angles.length; i++) {
			
			int K = 200;
			if (angles.length > K && (angles[i] > peakThresh || angles[i] < valleyThresh)) {
				// get neighborhood [i-k, i+k]
				double[] neighborAngles = new double[2 * K];
				int index;
				
				for (int j = -5; j < 0; j++) {
					if (i < K)
						index = angles.length - (K-i);
					else
						index = i - K;
					double y = angles[index];
					neighborAngles[j + 5] = y;
				}
				
				for (int j = 5; j < neighborAngles.length; j++) {
					if (i >= (angles.length - K))
						index = (i + K) % K;
					else
						index = i + K;
					neighborAngles[j] = angles[index];
				}
				
				boolean isBiggest = true, isSmallest = true;
				for (int j = 0; j < neighborAngles.length; j++) {
					if (isBiggest || isSmallest) {
						if (angles[i] < neighborAngles[j])
							isBiggest = false;
						if (angles[i] > neighborAngles[j])
							isSmallest = false;
					} else {
						break;
					}
				}
				
				if (isBiggest)
					peaks++;
				if (isSmallest)
					valleys++;
			
			}
		}
		
		return new GestureClass(peaks, valleys);
	}
	
}
