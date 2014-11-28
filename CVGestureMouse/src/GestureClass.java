import org.opencv.core.Point;


public class GestureClass {
	
	public ClassName name;
	
	private static final int POINT_PEAKS_MIN_THRESHOLD = 0;
	private static final int POINT_PEAKS_MAX_THRESHOLD = 3;
	private static final int REACH_PEAKS_MIN_THRESHOLD = 4;
	private static final int REACH_VALLEYS_MIN_THRESHOLD = 4;
	
	public GestureClass() {
		this.name = ClassName.UNCLASSIFIED;
	}
	
	public GestureClass(int peaks, int valleys) {
		this.name = getClass(peaks, valleys);
	}
	
	public static GestureClass classifyContour(Point[] contour) {
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
	
	private ClassName getClass(int peaks, int valleys) {
		ClassName name = ClassName.GROUND;
		if (peaks > POINT_PEAKS_MIN_THRESHOLD && peaks < POINT_PEAKS_MAX_THRESHOLD)
			name = ClassName.POINT;
		else if (peaks > REACH_PEAKS_MIN_THRESHOLD || valleys > REACH_VALLEYS_MIN_THRESHOLD)
			name = ClassName.REACH;
		return name;
	}
	
}
