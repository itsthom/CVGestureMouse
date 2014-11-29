import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;


public class GestureClass {
	
	public ClassName name;
	
	private static final int POINT_PEAKS_MIN_THRESHOLD = 0;
	private static final int POINT_PEAKS_MAX_THRESHOLD = 3;
	private static final int REACH_PEAKS_MIN_THRESHOLD = 4;
	private static final int REACH_VALLEYS_MIN_THRESHOLD = 4;
	
	private static int frameNo = 0;
	
	public GestureClass() {
		this.name = ClassName.UNCLASSIFIED;
	}
	
	public GestureClass(int peaks, int valleys) {
		this.name = getClass(peaks, valleys);
	}
	
	public static GestureClass classifyContour(Point[] contour, Mat img) {
		// TODO classification doesn't work - may need to rewrite and adjust thresholds
		int peaks = 0, valleys = 0;
		int k = 25; // distance from center point for getting vectors
		if (contour.length <= k)
			return new GestureClass();
		
		double[] angles = new double[contour.length]; // angles between vectors i-k,i and i,i+k
		double peakThresh = 1.6; // positive angle threshold for peaks
		double valleyThresh = -1.6; // negative angle threshold for valleys
		
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
			
//			Core.circle(img, contour[a], 20, new Scalar(255,0,0));
//			Core.circle(img, contour[b], 20, new Scalar(255,0,0));
			
			double ay = contour[a].y - contour[i].y, 
					ax = contour[a].x - contour[i].x,
					by = contour[b].y - contour[i].y, 
					bx = contour[b].x - contour[i].x;
			angles[i] = Math.atan2(ax*by - ay*bx, ax*bx + ay*by); //perp dot product
		}
		
		int K = 25;
		double[] neighbors = new double[2 *K];
		
		for (int i = 0; i < angles.length; i++) {
			if ((angles[i] < peakThresh && angles[i] > valleyThresh)) {
				if (angles[i] > 0 ) {
					int index;
					
//					for (int j = 0; j < K; j++) {
//						if (i < K)
//							index = angles.length - (K - j);
//						else
//							index = i - j;
//						
//						if (angles[index] > 0 && angles[index] < peakThresh)
//							neighbors[j] = angles[index];
//					}
					
					for (int j = 0; j < K; j++) {
						if (i >= (angles.length - K))
							index = i;//(i + j) % K;
						else
							index = i + j;
						if (angles[index] > 0 && angles[index] < peakThresh)
							neighbors[j + K] = angles[index];
					}
					
					boolean isPeak = true;
					for (int j=0; j < neighbors.length; j++) {
						if (neighbors[j] != 0 && angles[i] > neighbors[j]) {
							isPeak = false;
							break;
						}
					}
					
					if (isPeak){
						Core.circle(img, contour[i], 15, new Scalar(255,0,0), 3);
						peaks++;
					}
					
					Core.circle(img, contour[i], 8, new Scalar(0,255,0),2);
						valleys++;
					
				}
//					
//				if (angles[i] < 0 ) {
//					Core.circle(img, contour[i], 20, new Scalar(0,255,0));
//					valleys++;
//				}
			}
//			
//			int K = 30;
//			if (angles.length > K && (angles[i] > peakThresh && angles[i] < valleyThresh)) {
//				// get neighborhood [i-k, i+k]
//				double[] neighborAngles = new double[2 * K];
//				int index;
//				
//				for (int j = -K; j < 0; j++) {
//					if (i < K)
//						index = angles.length - (K-i);
//					else
//						index = i - K;
//					neighborAngles[j + K] = angles[index];
//				}
//				
//				for (int j = K; j < neighborAngles.length; j++) {
//					if (i >= (angles.length - K))
//						index = (i + K) % K;
//					else
//						index = i + K;
//					neighborAngles[j] = angles[index];
//				}
//				
//				boolean isBestPeak = true, isBestValley = true;
//				if (angles[i] < 0) {
//					isBestValley = false;
//					for (int j=0; j < neighborAngles.length; j++) {
//						if (angles[i] < neighborAngles[j]) {
//							isBestPeak = false;
//							break;
//						}
//					}
//				} else {
//					isBestPeak = false;
//					for (int j=0; j < neighborAngles.length; j++) {
//						if (angles[i] > neighborAngles[j]) {
//							isBestValley = false;
//							break;
//						}
//					}
//				}
//				
//				if (isBestPeak) {
//					peaks++;
////					Core.circle(img, contour[i], 20, new Scalar(255,0,0));
//				}
//				if (isBestValley) {
//					valleys++;
//					Core.circle(img, contour[i], 20, new Scalar(0,255,0));
//					
//				}
//			}
		}
		
		if (frameNo % 20 == 0) {
			System.out.println("Perimeter length: " + contour.length + ";    Peaks: " + peaks + ";    Valleys: " + valleys);
			System.out.println(Arrays.toString(neighbors));
		}
		frameNo++;
		return new GestureClass(peaks, valleys);
	}
	
	private ClassName getClass(int peaks, int valleys) {
		ClassName name = ClassName.GROUND;
		if (peaks > POINT_PEAKS_MIN_THRESHOLD && peaks < POINT_PEAKS_MAX_THRESHOLD)
			name = ClassName.POINT;
		else if (peaks > REACH_PEAKS_MIN_THRESHOLD || valleys > REACH_VALLEYS_MIN_THRESHOLD)
			name = ClassName.REACH;
		return name;
	}
	
}
