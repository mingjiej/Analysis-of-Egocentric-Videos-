

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;


public class MotionDetector {
	private int width = 480;
	private int height = 270;
	private int len = width * height * 3;
	private InputStream is;
	private int start_frame = 0;
	private int frame_num = 4500;
	//
	private final int FRAME_NUMBER = 4500;
	public enum Mode {
		READ_MODE, TEST_MODE, WRITE_MODE;
	}
	private Mode mode;
	//
	private double[] mad_list;
	// the sum of all frame's sum mad
	private double sum = 0;
	// the list of quantized mad value
	private int[] q_mad_list;

	public byte[] f_block = new byte[len];
	public byte[] r_block = new byte[len];
	public byte[] p_block_RGB = new byte[len];
	
	// Macroblock setting
	private int macro_width = 10;
	private int k = 16;
	private int level_num = 8;
	private int mac_sum = (width * height) / (macro_width * macro_width);
	// Traverse all macroblocks and generate predict frame
	// macroblock is a 5 * 5 square
	private double gen_predict_frame(){
		int mac_count = 0;
		double mad_sum = 0;
		for(int y = macro_width/2; y < height - macro_width/2;){
			for(int x = macro_width/2; x < width - macro_width/2;){
				Macroblock mac_b = new Macroblock(x, y, f_block, r_block, macro_width, k);
				mad_sum += mac_b.mini_MAD;
				mac_count++;
				if(mac_count >= mac_sum) break;
				if(mode== Mode.TEST_MODE){
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
		}
		//System.out.print(mac_count);
		return mad_sum/mac_sum;
	}

	public void gen_frames() throws IOException{
		double mad = 0;
		mad_list = new double[frame_num];
		Writer writer = null;
		String file_name = "output_mad.txt";
		String log = "";
		is.skip(start_frame * height * width * 3);
		if(is.read(r_block) > 0){
			for(int i = 0; i < frame_num; i++){
				if(is.read(f_block) > 0) {
					mad = (int) gen_predict_frame();
					sum += mad;

					if ((i > 0 && mad < mad_list[i - 1] * 20) || i == 0 || mad_list[i - 1] == 0) {
						mad_list[i] = mad;
					} else {
						mad_list[i] = 0;
					}
					log += mad_list[i] + "\n";

					System.arraycopy(f_block, 0, r_block, 0, len);

					if (i % 100 == 0) System.out.println(i);
				}
			}
		}
		if(mode == Mode.WRITE_MODE){
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_name)));
			writer.write(log);
			writer.close();
		}
		System.out.println("end");
	}
	
	private void quantize() throws IOException{
		Writer writer = null;
		String file_name = "output_q_mad.txt";
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
		if(mode == Mode.WRITE_MODE){
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_name)));
			writer.write(log);
			writer.close();	
		}
	}
	//
	private ArrayList<Interval> gen_play_intervals(){
		ArrayList<Interval> intervals = new ArrayList<>();
		int start_frame = 0;
		int end_frame = 0;
		int cut_level = level_num/2;
		int count = 0;
		for(int i = 0; i < q_mad_list.length; i++){
			if(q_mad_list[i] <= cut_level){
				count++;
			}
			else{
				if(count >= 45){
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
		if(mode == Mode.READ_MODE){
			try {
				BufferedReader reader = new BufferedReader(new FileReader("output_q_mad.txt"));
				String line;
				int i = 0;
				while((line = reader.readLine()) != null){
					q_mad_list[i] = Integer.parseInt(line.substring(0, line.length()));
					i++;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			gen_frames();
			System.out.println("Start quantize");
			quantize();
		}
		System.out.println("Start gen_play_list");
		return gen_play_intervals();
	}
	
	public void jumper(int n, InputStream is, Boolean run) throws IOException{
		if(run){
			is.skip(n * height * width * 3);
		}
		System.out.println("Jump: " + n);
	}


	public Interval jump_to_highlight(InputStream is, ArrayList<Interval> intervals, int frame_count, Interval currentInterval) throws IOException {
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

	public MotionDetector(InputStream is, int start_frame, Mode mode) {
		this.start_frame = start_frame;
		this.mode = mode;
		this.is = is;
		this.q_mad_list = new int[FRAME_NUMBER];
	}

}
