import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class GestureClass {

	public ClassName name;
	public Point position;

	private static final int MOVING_PEAKS_MIN_THRESHOLD = 5;
	private static final int MOVING_PEAKS_MAX_THRESHOLD = 5;
	private static final int LEFT_CLICK_MOVING_PEAKS_MIN_THRESHOLD = 4;
	private static final int LEFT_CLICK_MOVING_PEAKS_MAX_THRESHOLD = 4;
	private static final int MOVING_VALLEYS_MIN_THRESHOLD = 4;
	private static final int MOVING_VALLEYS_MAX_THRESHOLD = 4;
	private static final int LEFT_CLICK_MOVING_VALLEYS_MIN_THRESHOLD = 3;
	private static final int LEFT_CLICK_MOVING_VALLEYS_MAX_THRESHOLD = 3;

	private static final int X_MAX = 640;
	private static final int Y_MAX = 480;
	private static final int IGNORE_EDGE_WIDTH = 5;

	private static int frameNo = 0;

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
		int k = 25; // distance from center point for getting vectors
		if (contour.length <= k)
			return new GestureClass();

		double[] angles = new double[contour.length]; // angles between vectors
														// i-k,i and i,i+k
		double peakThresh = 1.6; // positive angle threshold for peaks
		double valleyThresh = -1.6; // negative angle threshold for valleys
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
			double ay = contour[i].y - contour[a].y, ax = contour[i].x
					- contour[a].x, by = contour[b].y - contour[i].y, bx = contour[b].x
					- contour[i].x;
			angles[i] = Math.atan2(ax * by - ay * bx, ax * bx + ay * by); // perp
																			// dot
																			// product
		}

		int K = 25;
		ArrayList<Point> peakList = new ArrayList<Point>();

		for (int i = 0; i < angles.length; i++) {
			if (angles[i] > peakThresh) {
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

			if (angles[i] < valleyThresh) {
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

			if (peakList.size() > 2) {
				ArrayList<Double> peakY = new ArrayList<Double>();
				for (Point p : peakList) {
					peakY.add(p.y);
				}
				Collections.sort(peakY);
				Double wantedY;
				if (peaks == MOVING_PEAKS_MIN_THRESHOLD) {
					wantedY = peakY.get(2);
					for (Point p : peakList) {
						if (p.y == wantedY)
							pointLocation = p;
					}
				} else if (peaks == LEFT_CLICK_MOVING_PEAKS_MIN_THRESHOLD) {
					wantedY = peakY.get(1);
					for (Point p : peakList) {
						if (p.y == wantedY)
							pointLocation = p;
					}
				}

			}

		}

		frameNo++;
		GestureClass result = new GestureClass(peaks, valleys, previousClass);
		if (result.name == ClassName.MOVING
				|| result.name == ClassName.LEFT_CLICK_MOVING) {
			if (pointLocation == null) {
				result.position = previousPos;
			} else {
				result.position = pointLocation;
			}
		}
		return result;
	}

	private ClassName getClass(int peaks, int valleys, ClassName previousClass) {
		if (peaks == MOVING_PEAKS_MIN_THRESHOLD)
			name = ClassName.MOVING;
		else if (peaks == LEFT_CLICK_MOVING_PEAKS_MIN_THRESHOLD)
			name = ClassName.LEFT_CLICK_MOVING;
		else
			name = previousClass;
		// if (peaks > POINT_PEAKS_MIN_THRESHOLD && peaks <
		// POINT_PEAKS_MAX_THRESHOLD)
		// name = ClassName.POINT;
		// else if (peaks > REACH_PEAKS_MIN_THRESHOLD || valleys >
		// REACH_VALLEYS_MIN_THRESHOLD)
		// name = ClassName.REACH;
		return name;
	}

}
