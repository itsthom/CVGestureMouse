import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class GestureClass {

	public ClassName name;
	public Point position;

	private static final double PK_THRESH_RADS = 1.6;
	private static final double VL_THRESH_RADS = -1.6;
	
	private static final int LCLICK_MOV_PK_MIN_THRESH = 1;
	private static final int LCLICK_MOV_PK_MAX_THRESH = 1;
	private static final int LCLICK_MOV_VL_MIN_THRESH = 0;
	private static final int LCLICK_MOV_VL_MAX_THRESH = 1;
	private static final int MOV_PK_MIN_THRESH = 2;
	private static final int MOV_PK_MAX_THRESH = 2;
	private static final int MOV_VL_MIN_THRESH = 1;
	private static final int MOV_VL_MAX_THRESH = 2;
	private static final int STOP_PK_MIN_THRESH = 3;
	private static final int STOP_VL_MIN_THRESH = 3;

	private static final int X_MAX = 640;
	private static final int Y_MAX = 480;
	private static final int IGNORE_EDGE_WIDTH = 5;


	public GestureClass() {
		this.name = ClassName.UNCLASSIFIED;
		this.position = null;
	}

	public GestureClass(int peaks, int valleys, ClassName previousClass) {
		this.name = getClass(peaks, valleys, previousClass);
		this.position = null;
	}

	public static GestureClass classifyContour(Point[] contour, Mat img, ClassName previousClass, Point previousPos) {

		int peaks = 0, valleys = 0;
		int k = 50; // distance from center point for getting vectors
		int K = 75; // distance from center point for getting neighborhood
		
		if (contour.length <= k)
			return new GestureClass();
		
		// angles between vectors i-k,i and i,i+k
		double[] angles = new double[contour.length]; 
		
		Point pointLocation = null;

		for (int i = 0; i < contour.length / 2; i++) {
			Point temp = contour[i];
			contour[i] = contour[contour.length - i - 1];
			contour[contour.length - i - 1] = temp;
		}

		for (int i = 0; i < contour.length; i++) {
			int a, b;
			if (i < k)
				a = contour.length - (k - i);
			else
				a = i - k;

			if (i >= (contour.length - k))
				b = (i + k) % k;
			else
				b = i + k;
			double ay = contour[i].y - contour[a].y,
					ax = contour[i].x - contour[a].x,
					by = contour[b].y - contour[i].y,
					bx = contour[b].x - contour[i].x;
			// perp dot product
			angles[i] = Math.atan2(ax * by - ay * bx, ax * bx + ay * by);
		}

		
		ArrayList<Point> peakList = new ArrayList<Point>();

		for (int i = 0; i < angles.length; i++) {
			if (angles[i] > PK_THRESH_RADS) {
				boolean isMaxPeak = true;
				int index = i - K;
				for (int j = 0; j < 2 * K; j++) {
					if (index < 0) {
						if (angles[i] < angles[angles.length + index]) {
							isMaxPeak = false;
						}
					} else if (index > angles.length - 1) {
						if (angles[i] < angles[index - angles.length]) {
							isMaxPeak = false;
						}
					} else {
						if (angles[i] < angles[index]) {
							isMaxPeak = false;
						}
					}
					index++;
				}
				if (isMaxPeak && contour[i].x > IGNORE_EDGE_WIDTH
						&& contour[i].x < (X_MAX - IGNORE_EDGE_WIDTH)
						&& contour[i].y > IGNORE_EDGE_WIDTH
						&& contour[i].y < (Y_MAX - IGNORE_EDGE_WIDTH)) {
					Core.circle(img, contour[i], 16, new Scalar(255, 0, 0), 3);
					peaks++;
					peakList.add(contour[i]);
				}
			}

			if (angles[i] < VL_THRESH_RADS) {
				boolean isMinValley = true;
				int index = i - K;
				for (int j = 0; j < 2 * K; j++) {
					if (index < 0) {
						if (angles[i] > angles[angles.length + index]) {
							isMinValley = false;
						}
					} else if (index > angles.length - 1) {
						if (angles[i] > angles[index - angles.length]) {
							isMinValley = false;
						}
					} else {
						if (angles[i] > angles[index]) {
							isMinValley = false;
						}
					}
					index++;
				}
				if (isMinValley && contour[i].x > IGNORE_EDGE_WIDTH
						&& contour[i].x < (X_MAX - IGNORE_EDGE_WIDTH)
						&& contour[i].y > IGNORE_EDGE_WIDTH
						&& contour[i].y < (Y_MAX - IGNORE_EDGE_WIDTH)) {
					Core.circle(img, contour[i], 16, new Scalar(183, 255, 0), 2);
					valleys++;
				}

			}

			if (peakList.size() == 1) {
				pointLocation = peakList.get(0);
			} else if (peakList.size() == 2) {
				pointLocation = (peakList.get(0).y > peakList.get(1).y) ? peakList.get(1) : peakList.get(0);
			} else {
				pointLocation = previousPos;
			}

		}

		GestureClass result = new GestureClass(peaks, valleys, previousClass);
		if (result.name == ClassName.MOVING
				|| result.name == ClassName.LEFT_CLICK_MOVING) {
			result.position = (pointLocation == null) ? previousPos : pointLocation;
		}
		return result;
	}

	private ClassName getClass(int peaks, int valleys, ClassName previousClass) {
		if (peaks >= MOV_PK_MIN_THRESH
				&& peaks <= MOV_PK_MAX_THRESH
				&& valleys >= MOV_VL_MIN_THRESH
				&& valleys <= MOV_VL_MAX_THRESH)
			name = ClassName.MOVING;
		else if (peaks >= LCLICK_MOV_PK_MIN_THRESH
				&& peaks <= LCLICK_MOV_PK_MAX_THRESH
				&& valleys >= LCLICK_MOV_VL_MIN_THRESH
				&& valleys <= LCLICK_MOV_VL_MAX_THRESH)
			name = ClassName.LEFT_CLICK_MOVING;
		else if (peaks >= STOP_PK_MIN_THRESH
				&& valleys >= STOP_VL_MIN_THRESH)
			name = ClassName.STOP;
		else
			name = previousClass;
		return name;
	}

}
