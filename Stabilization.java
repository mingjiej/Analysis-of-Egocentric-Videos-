import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Stabilization {
	private String video_name = "";
	private int start = 0;
	private int N = 5;
	private int[] arr_x;
	private int[] arr_y;
	private ArrayList<Integer> comp_list_x = new ArrayList<Integer>();
	private ArrayList<Integer> comp_list_y = new ArrayList<Integer>();
	private int mac_count = 0;
	
	public ArrayList<Interval> low_motion_interval = new ArrayList<Interval>();
	
	public Stabilization(String video_name, int start) throws IOException{
		this.video_name = video_name;
		this.start = start;
	}
	
	private int gen_globle_vector(int frame_num, int[] arr){
		int max = 0;
		int[] temp_arr = new int[31];
		
		for(int j = frame_num * mac_count; j < (frame_num + 1) * mac_count; j++){
			int vector = arr[j];
			if(vector != 0){
				temp_arr[vector + 15]++;
			}
		}
		for(int m = 0; m < temp_arr.length; m++){
			max = Math.max(max, temp_arr[m]);
		}
		for(int m = 0; m < temp_arr.length; m++){
			if(max == temp_arr[m]) return m - 15;
		}
		return 0;
	}
	
	private int motion_smoother(int frame_num, int[] arr){
		int sum = 0;
		for(int i = 0; i < 5; i++){
			if(i + frame_num >= arr.length) return (int)(sum/i);
			sum += arr[i + frame_num];
		}
		return (int)(sum/N);
	}
	
	private ArrayList<Integer> motion_estimation(int start, int end, String v){
		int[] arr;
		int[] or_globle_v_list = new int[end - start + 1];
		ArrayList<Integer> sm_globle_v_list = new ArrayList<Integer>();
		if(v == "x") arr = arr_x;
		else arr = arr_y;
		
		for(int i = start; i <= end; i++){
			or_globle_v_list[i - start] = gen_globle_vector(i, arr);
		}
		
		for(int i = start; i <= end; i++){
			//sm_globle_v_list.add(motion_smoother(i - start, or_globle_v_list));
			sm_globle_v_list.add(or_globle_v_list[i - start]/2);
		}
		return sm_globle_v_list;
	}
	
	private void gen_new_comp_list(){
		for(int i = 0; i < comp_list_x.size(); i++){
			if(i > 0){
				comp_list_x.set(i, comp_list_x.get(i - 1) - comp_list_x.get(i));
			}
		}
		
		for(int i = 0; i < comp_list_y.size(); i++){
			if(i > 0){
				comp_list_y.set(i, comp_list_y.get(i - 1) - comp_list_y.get(i));
			}
		}
	}
	
	public void initialize() throws IOException{
		
		String output_name = "vector_" + video_name.substring(video_name.length() - 7, video_name.length() - 4);
		String output_x = output_name + "_x.txt";
		String output_y = output_name + "_y.txt";
		
		BufferedReader reader_x = new BufferedReader(new FileReader(output_x));
		BufferedReader reader_y = new BufferedReader(new FileReader(output_y));
		
		String line;

		St_MotionDetector detector = new St_MotionDetector(video_name, start);
		detector.reset_cut_level(0, 1);
		low_motion_interval = detector.initializeWithIntervals();
		
		int mac_width = detector.macro_width;
		mac_count = (int)(480/mac_width) * (int)(270/mac_width);
		int len = mac_count * 4500 + 1;
		arr_x = new int[len];
		arr_y = new int[len];
		
		int i = 0;
		
		// Read vector_x
		while((line = reader_x.readLine()) != null){
			if(line.charAt(0) == 'f' || (line.length() > 1 && line.charAt(1) == '-') || line.charAt(0) == '\n') continue;
			arr_x[i] = Integer.parseInt(line.substring(0, line.length()));
			i++;
		}
		reader_x.close();
		
		// Read vector_y
		i = 0;
		while((line = reader_y.readLine()) != null){
			if(line.charAt(0) == 'f' || (line.length() > 1 && line.charAt(1) == '-') || line.charAt(0) == '\n') continue;
			arr_y[i] = Integer.parseInt(line.substring(0, line.length()));
			i++;
		}
		reader_y.close();
		
		for(i = 0; i < low_motion_interval.size(); i++){
			Interval interval = low_motion_interval.get(i);
			comp_list_x.addAll(motion_estimation(interval.getStart(), interval.getEnd(), "x"));
			comp_list_y.addAll(motion_estimation(interval.getStart(), interval.getEnd(), "y"));
		}
		
		
//		Interval interval = new Interval(3666, 3730);
//		comp_list_x.addAll(motion_estimation(interval.getStart(), interval.getEnd(), "x"));
//		comp_list_y.addAll(motion_estimation(interval.getStart(), interval.getEnd(), "y"));
//		
//		//gen_new_comp_list();
//		
//		low_motion_interval.add(interval);
		
	}
	
	public ArrayList<Integer> get_comp_list_x(){
		return comp_list_x;
	}
	
	public ArrayList<Integer> get_comp_list_y(){
		return comp_list_y;
	}
	
	public ArrayList<Interval> get_interval(){
		return low_motion_interval;
	}
}
