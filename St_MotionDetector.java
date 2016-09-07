import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;


public class St_MotionDetector {
	private int width = 480;
	private int height = 270;
	private int len = width * height * 3;
	private InputStream is;
	private int start_frame = 0;
	private int frame_num = 4500;
	private String video_name = "";
	private String output_mad_name = "";
	private String output_vector_name = "";
	//
	private final int FRAME_NUMBER = 4500;
	
	private boolean READ_MODE = false;
	private boolean WRITE_MODE = false;
	private boolean TEST_MODE = false;
	//
	private double[] mad_list = new double[4500];
	// the sum of all frame's sum mad
	private double sum = 0;
	// the list of quantized mad value
	private int[] q_mad_list;
	
	private int cut_level = 3;
	private int min_cut_level = 0;

	private ArrayList<ArrayList<Vector>> vector_frame_list = new ArrayList<ArrayList<Vector>>();
	private String log_vector_x = "";
	private String log_vector_y = "";
	
	public byte[] f_block = new byte[len];
	public byte[] r_block = new byte[len];
	public byte[] p_block_RGB = new byte[len];
	
	// Macroblock setting
	public int macro_width = 8;
	private int k = 8;
	private int level_num = 8;
	private int mac_sum = (width * height) / (macro_width * macro_width);
	// Traverse all macroblocks and generate predict frame
	// macroblock is a 5 * 5 square
	private long gen_predict_frame(int frame_num){
		int mac_count = 0;
		long mad_sum = 0;
		
		int x_temp = 0;
		int y_temp = 0;
		
		String log_temp_x = "frame: " + frame_num + "\n";
		String log_temp_y = "frame: " + frame_num + "\n";
		ArrayList<Vector> vector_list = new ArrayList<Vector>();
		for(int y = macro_width/2; y <= height - macro_width/2;){
			for(int x = macro_width/2; x <= width - macro_width/2;){
				
				x_temp = x;
				y_temp = y;
				
				Macroblock mac_b = new Macroblock(x, y, f_block, r_block, macro_width, k);
				
				//Vector v = new Vector(mac_b.vector_i, mac_b.vector_j);
				//vector_list.add(v);
				
				log_temp_x += mac_b.vector_i + "\n";
				log_temp_y += mac_b.vector_j + "\n";
				
				mad_sum += mac_b.mini_MAD;
				mac_count++;
				if(mac_count >= mac_sum) break;
				if(TEST_MODE){
					for(int n = y - macro_width/2; n <= y + macro_width/2; n++){
					if(n >= height) continue;
						for(int m = x - macro_width/2; m <= x + macro_width/2; m++){
							if(m >= width) continue;
							
							int ind_p = m + n * width;
							int ind_r = m + mac_b.vector_i + (n + mac_b.vector_j) * width;
							
							p_block_RGB[ind_p] = r_block[ind_r];
							
							p_block_RGB[ind_p + width * height] = r_block[ind_r + width * height];
							
							p_block_RGB[ind_p + width * height * 2] = r_block[ind_r + width * height * 2];
						}
					}
				}
				x += macro_width;
			}
			y += macro_width;
			//vector_frame_list.add(vector_list);
		}
		log_temp_x += "------------\n";
		log_temp_y += "------------\n";
		log_vector_x += log_temp_x;
		log_vector_y += log_temp_y;
		return mad_sum/mac_sum;
	}

	public void gen_frames() throws IOException{
		double mad = 0;
		Writer writer_mad = null;
		Writer writer_vector_x = null;
		Writer writer_vector_y = null;
		String log_mad = "";
		is.skip(start_frame * height * width * 3);
		if(is.read(r_block) > 0){
			for(int i = 0; i < frame_num; i++){
				if(is.read(f_block) > 0) {
					mad = (int) gen_predict_frame(i + 2);
					sum += mad;

					if ((i > 0 && mad < mad_list[i - 1] * 20) || i == 0 || mad_list[i - 1] == 0) {
						mad_list[i] = mad;
					} else {
						mad_list[i] = 0;
					}
					log_mad += mad_list[i] + "\n";

					System.arraycopy(f_block, 0, r_block, 0, len);

					//if (i % 100 == 0) System.out.println(i);
					System.out.println(i);
				}
			}
		}
		log_mad += "s:" + sum;
		writer_mad = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_mad_name)));
		writer_mad.write(log_mad);
		writer_mad.close();
		
		writer_vector_x = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_vector_name + "_x.txt")));
		writer_vector_x.write(log_vector_x);
		writer_vector_x.close();
		
		writer_vector_y = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_vector_name + "_y.txt")));
		writer_vector_y.write(log_vector_y);
		writer_vector_y.close();
		
		System.out.println("end");
	}
	
	private void quantize() throws IOException{
		Writer writer = null;
		String file_name = "output_q.txt";
		String log = "";
		
		int level_value = (int) ((sum/frame_num)/((level_num - 1)/2));
		for(int i = 0; i < mad_list.length; i++){
			for(int v = level_value; v <= (level_num - 1) * level_value; v += level_value){
				if(mad_list[i] <= v){
					if(mad_list[i] < v/2) q_mad_list[i] = (v - level_value)/level_value;
					else q_mad_list[i] = v/level_value;
					break;
				}
				if(q_mad_list[i] == 0 && mad_list[i] > (level_num - 1) * level_value) q_mad_list[i] = level_num;
			}
		}

		for(int i = 0; i < q_mad_list.length; i++){
			log += q_mad_list[i] + "\n";
		}
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_name)));
		writer.write(log);
		writer.close();	
	}
	//
	private ArrayList<Interval> gen_play_intervals(){
		ArrayList<Interval> intervals = new ArrayList<>();
		int start_frame = 0;
		int end_frame = 0;
		int count = 0;
		for(int i = 0; i < q_mad_list.length; i++){
			if(q_mad_list[i] < 5 && q_mad_list[i] > 0){
				count++;
			}
			else{
				if(count >= 15){
					end_frame = i - 1;
					intervals.add(new Interval(start_frame, end_frame));
				}
				start_frame = i;
				count = 0;
			}
		}
		return intervals;
	}
	
	public void reset_cut_level(int new_min_cut_level, int new_cut_level){
		min_cut_level = new_min_cut_level;
		cut_level = new_cut_level;
	}
	
	public ArrayList<Interval> divide_low_motion_intervals(){
		ArrayList<Interval> intervals = new ArrayList<>();
		int start_frame = 0;
		int end_frame = 0;
		int cut_level = 1;
		int count = 0;
		for(int i = 0; i < q_mad_list.length; i++){
			if(q_mad_list[i] <= cut_level){
				count++;
			}
			else{
				if(count >= 30){
					end_frame = i - 1;
					intervals.add(new Interval(start_frame, end_frame));
				}
				start_frame = i;
				count = 0;
			}
		}
		return intervals;
	}

	//
	public ArrayList<Interval> initializeWithIntervals() throws IOException{
		if(new File(output_mad_name).isFile()){
			BufferedReader reader = new BufferedReader(new FileReader(output_mad_name));
			String line;
			int i = 0;
			while((line = reader.readLine()) != null){
				if(line.charAt(line.length() -1) == 'E'){
					double num = Double.parseDouble(line.substring(0, line.length()-2));
					double pow = Double.parseDouble(line.substring(line.length()-1, line.length()));
					mad_list[i] = num * Math.pow(10, pow);
				}
				else if(line.charAt(0) == 's') sum = Double.parseDouble(line.substring(2));
				else{
					mad_list[i] = Double.parseDouble(line.substring(0, line.length()));
				}
				
				i++;
			}
			reader.close();
		}
		else{
			gen_frames();
		}
		System.out.println("Start quantize");
		quantize();
	
		System.out.println("Start gen_play_list");
		return gen_play_intervals();
	}

	public St_MotionDetector(String video_name, int start_frame) {
		this.video_name = video_name;
		this.output_mad_name = "mad_" + video_name.substring(video_name.length() - 7, video_name.length() - 4) + ".txt";
		this.output_vector_name = "vector_" + video_name.substring(video_name.length() - 7, video_name.length() - 4);
		this.start_frame = start_frame;
		try {
			File file = new File(video_name);
			this.is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.q_mad_list = new int[FRAME_NUMBER];
	}

}
