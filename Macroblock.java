
public class Macroblock {
	private int mac_width;
	private int k;
	private int width = 480;
	private int height = 270;
	private byte[] f_block;
	private byte[] r_block;
	private int center_x = 0;
	private int center_y = 0;
	public int vector_i = 0;
	public int vector_j = 0;
	public double mini_MAD = Double.POSITIVE_INFINITY;
	
	public double MAD(int center_y, int center_x, int j, int i){
		double sum = 0;
		double MAD = 0;
		int ind_r = 0;
		int ind_f = 0;
		
		for(int p = center_y - mac_width/2; p < center_y + mac_width/2; p++){
			if(p < 0) continue;
			if(p >= height) break;
			for(int q = center_x - mac_width/2; q < center_x + mac_width/2; q++){
				if(q < 0) continue;
				if(q >= width) break;
				if(p + j < 0 || q + i < 0 || p + j >= height || q + i >= width){
					continue;
				}
				
				ind_r = q + i + (p + j) * width;
				byte r1 = r_block[ind_r];
				byte g1 = r_block[ind_r + height * width];
				byte b1 = r_block[ind_r + height * width * 2];
				
				ind_f = q + p * width;
				byte r2 = f_block[ind_f];
				byte g2 = f_block[ind_f + height * width];
				byte b2 = f_block[ind_f + height * width * 2];
				
				double delta = (Math.pow((r1 & 0xff) - (r2 & 0xff), 2) + Math.pow((g1 & 0xff) - (g2 & 0xff), 2) + Math.pow((b1 & 0xff) - (b2 & 0xff), 2))/3;
				
				sum += Math.pow(delta, 2);
			}
		}
		MAD = sum/(Math.pow(mac_width, 2));
		
		return MAD;
	}

	private void logarithmic_search(){
		int step_size = k;
		int new_x = center_x;
		int new_y = center_y;
		double mad = 0;
		
		while(step_size != 1){
			for(int j = -(step_size/2); j <= step_size/2;){
				for(int i = -(step_size/2); i <= step_size/2;){
					if(center_x - mac_width + i < 0 || center_y - mac_width + j < 0 || center_x + mac_width + i > width || center_y + mac_width + j > height){
						i += step_size/2;
						continue;
					}
					mad = MAD(new_y, new_x, j, i);
					if (mini_MAD > mad){
						mini_MAD = mad;
						vector_i = i;
						vector_j = j;
					}
					i += step_size/2;
				}
				j += step_size/2;
			}
			new_x += vector_i;
			new_y += vector_j;
			step_size /= 2;
		}
		
	}
	
	public Macroblock(int x, int y, byte[] f_block, byte[] r_block, int macro_width, int k){
		this.f_block = f_block;
		this.r_block = r_block;
		this.mac_width = macro_width;
		this.k = k;
		center_x = x;
		center_y = y;
		
		//brute_force_search();
		logarithmic_search();
	}
}
