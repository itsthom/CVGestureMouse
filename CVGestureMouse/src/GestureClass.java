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
		
		for(int i = 0; i < contour.length / 2; i++){
			Point temp = contour[i];
			contour[i] = contour[contour.length - i - 1];
			contour[contour.length - i -1] = temp;
		}
		
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
			double ay = contour[i].y - contour[a].y , 
					ax = contour[i].x - contour[a].x,
					by = contour[b].y - contour[i].y, 
					bx = contour[b].x - contour[i].x;
			angles[i] = Math.atan2(ax*by - ay*bx, ax*bx + ay*by); //perp dot product
		}
		
		int K = 25;

		for (int i = 0; i < angles.length; i++) {
			if (angles[i] > peakThresh) {
				boolean isMaxPeak = true;
				int index = i - K;
				for (int j = 0;j<2*K;j++){
					if (index < 0){
						if (angles[i] < angles[angles.length + index]){
							isMaxPeak = false;
						}
					} else if (index > angles.length - 1){
						if (angles[i] < angles[index - angles.length]){
							isMaxPeak = false;
						}
					} else {
						if (angles[i] < angles[index]){
							isMaxPeak = false;
						}
					}
					index++;
				}
				if (isMaxPeak){
					Core.circle(img, contour[i], 15, new Scalar(255,0,0), 2);
					peaks++;
				}
			}
			
			if (angles[i] < valleyThresh) {
				boolean isMinValley = true;
				int index = i - K;
				for (int j = 0;j<2*K;j++){
					if (index < 0){
						if (angles[i] > angles[angles.length + index]){
							isMinValley = false;
						}
					} else if (index > angles.length - 1){
						if (angles[i] > angles[index - angles.length]){
							isMinValley = false;
						}
					} else {
						if (angles[i] > angles[index]){
							isMinValley = false;
						}
					}
					index++;
				}
				if (isMinValley){
					Core.circle(img, contour[i], 15, new Scalar(0,255,0), 2);
					valleys++;
				}
			}
		}
		
		if (frameNo % 20 == 0) {
			System.out.println("Perimeter length: " + contour.length + ";    Peaks: " + peaks + ";    Valleys: " + valleys);
			//System.out.println(Arrays.toString(angles));
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
