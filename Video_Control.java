import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Video_Control {
	private static int width = 480;
	private static int height = 270;
	
	public static void jumper(int n, InputStream is, Boolean run) throws IOException{
		if(run){
			is.skip(n * height * width * 3);
		}
		System.out.println("Jump: " + n);
	}
	
	public static Interval jump_to_highlight(InputStream is, ArrayList<Interval> intervals, int frame_count, Interval currentInterval) throws IOException {
		if(intervals.size()==0) {
			return null;
		} else {
			Interval inter = intervals.remove(0);
			if(currentInterval==null) jumper(inter.getStart() - 0, is, true);
			else jumper(inter.getStart() - currentInterval.getEnd(), is, true);
			frame_count = inter.getStart();
			System.out.println("[JUMP] Start: " + inter.getStart() + ", End: "+ inter.getEnd() + ", Frame: " + frame_count);
			return inter;
		}
	}
}
