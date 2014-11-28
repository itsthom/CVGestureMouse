
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
	
	private ClassName getClass(int peaks, int valleys) {
		ClassName name = ClassName.GROUND;
		if (peaks > POINT_PEAKS_MIN_THRESHOLD && peaks < POINT_PEAKS_MAX_THRESHOLD)
			name = ClassName.POINT;
		else if (peaks > REACH_PEAKS_MIN_THRESHOLD || valleys > REACH_VALLEYS_MIN_THRESHOLD)
			name = ClassName.REACH;
		return name;
	}
	
}
