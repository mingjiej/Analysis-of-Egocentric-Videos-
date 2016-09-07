
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class St_Player {
	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	// Frame counter
	JLabel lbText3;
	private static int frame_count = 0;
	private static int frame_count_local = 0;
	//
	static BufferedImage img;
	LinkedList<BufferedImage> images;
	private static int width = 480;
	private static int height = 270;
	InputStream is;
	byte[] pre_frame;
	//static Clip clip;
    private int indexOfFrame = 0;
    private File file;
	private St_MotionDetector detector;
	private Interval currentIntervals;
	private ArrayList<Interval> intervals;
    private ArrayList<Interval> buffer;
    private ArrayList<Integer> comp_list_x;
    private ArrayList<Integer> comp_list_y;
    
    
	public void initialize(String[] args) throws IOException, UnsupportedAudioFileException {
		images = new LinkedList<>();
		try {
            file = new File(args[0]);
			is = new FileInputStream(file);
			long len = width * height * 3;
			pre_frame = new byte[(int) len];
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//            getAllVector(pre_frame, is);
//            is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		JLabel lbText1 = new JLabel("Video: " + args[0]);
		lbText1.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel lbText2 = new JLabel("Audio: " + args[1]);
		lbText2.setHorizontalAlignment(SwingConstants.LEFT);
		lbIm1 = new JLabel(new ImageIcon(img));
		
		// 4.19
		// Show frame number
		lbText3 = new JLabel("Frame: " + frame_count);
		lbText3.setHorizontalAlignment(SwingConstants.LEFT);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		frame.getContentPane().add(lbIm1, c);
		
		// 4.19
		// Frame counter
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 3;
		frame.getContentPane().add(lbText3, c);
		
		// Motion Detector
		//detector = new MotionDetector(args[0], 0);
		//intervals = Interval.mergeInterval(detector.initializeWithIntervals(), AudioAmplitude.calculateVoice(args[1]));
		//
		
		Stabilization stabilizer = new Stabilization(args[0], 0);
		stabilizer.initialize();
		intervals = stabilizer.get_interval();
		comp_list_x = stabilizer.get_comp_list_x();
		comp_list_y = stabilizer.get_comp_list_y();
		
        //Interval.reverse(intervals);
        Interval.filter(intervals);
        Interval.removeIntervalsUnderThrehold(intervals, 45);
        Interval.print(intervals);
        Interval.report(intervals);
        buffer = new ArrayList<>(intervals);
		currentIntervals = Video_Control.jump_to_highlight(is, intervals, frame_count, currentIntervals);
		frame_count = currentIntervals.getStart();
        //clip.setMicrosecondPosition(frame_count*66667);
		addButtion();
		frame.pack();
		frame.setVisible(true);
	}

//	public static void prepareWAV(String filename) {
//		try {
//			// Open an audio input stream.
//			File soundFile = new File(filename); //you could also get the sound file with an URL
//			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
//			// Get a sound clip resource.
//			clip = AudioSystem.getClip();
//			// Open audio clip and load samples from the audio input stream.
//			clip.open(audioIn);
//		} catch (UnsupportedAudioFileException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (LineUnavailableException e) {
//			e.printStackTrace();
//		}
//	}

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		if (args.length < 2) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
		    return;
		}
		St_Player ren = new St_Player();
		//prepareWAV(args[1]);
		ren.initialize(args);
	}

	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public void addButtion() {
		JButton startButton = new JButton("Start");
		frame.add(startButton);
		JButton stopButton = new JButton("Pause");
		frame.add(stopButton);
		JButton quitButton = new JButton("Stop");
		frame.add(quitButton);
		stopButton.setEnabled(false);
		
		Timer timer = new Timer(63, new ActionListener() {
		//Timer timer = new Timer(63, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// 4.19
					// Frame counter
					lbText3.setText("Frame: " + frame_count);
                    if(indexOfFrame%3!=0) {
                        TimeUnit.MICROSECONDS.sleep(1);
                    }
					//MotionDetector
					if(frame_count>=currentIntervals.getEnd()) {
						System.out.println("Frame: " + frame_count);
						currentIntervals = Video_Control.jump_to_highlight(is, intervals, frame_count, currentIntervals);
						if(currentIntervals==null) {
							//clip.stop();
                            stopButton.setText("Restart");
						}
						else frame_count = currentIntervals.getStart();
                        //clip.setMicrosecondPosition(frame_count*66667);
					}
					//
					pre_frame = renderImage_st(pre_frame, is, images, currentIntervals.getStart());
					lbIm1 = new JLabel(new ImageIcon(images.removeFirst()));
					frame.repaint();
					frame_count++;
					frame_count_local++;
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
		});
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				timer.setRepeats(true);
				timer.start();
                //clip.setMicrosecondPosition(frame_count*66667);
                //clip.start();
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
			}
		});
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                timer.stop();
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                //clip.stop();
                if(currentIntervals==null) {
                    stopButton.setText("Pause");
                    try {
                        is = new FileInputStream(file);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    intervals = new ArrayList<>(buffer);
                    try {
                        currentIntervals = Video_Control.jump_to_highlight(is, intervals, frame_count, currentIntervals);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    frame_count = currentIntervals.getStart();
                    //clip.setMicrosecondPosition(frame_count*66667);
                }
			}
		});
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}

	private void set_img(int ind, BufferedImage img, int m, int n, byte[] pre_frame){
		ind = m + n * width;
		byte r_c1 = pre_frame[ind];
		byte g_c1 = pre_frame[ind + height * width];
		byte b_c1 = pre_frame[ind + height * width * 2];
		int pix_c1 = 0xff000000 | ((r_c1 & 0xff) << 16) | ((g_c1 & 0xff) << 8) | (b_c1 & 0xff);
		img.setRGB(m, n, pix_c1);
	}
	
	public byte[] renderImage_st(byte[] pre_frame, InputStream is, LinkedList<BufferedImage> images, int start) throws IOException {

		int x_offset = comp_list_x.get(frame_count_local);
		int y_offset = comp_list_y.get(frame_count_local);

//		x_offset *= 10;
//		y_offset *= 10;
		
		
		System.out.println(x_offset + ", " + y_offset);
		
		byte[] cur_frame = new byte[480 * 270 * 3];
		
		if(frame_count == start){
			if(images.size()<=100&&is.read(cur_frame)>0) {
				int ind = 0;
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte r = cur_frame[ind];
						byte g = cur_frame[ind + height * width];
						byte b = cur_frame[ind + height * width * 2];
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x, y, pix);
						ind++;
					}
				}
				images.add(deepCopy(img));
			}
		}
		else{
			if(images.size()<=100&&is.read(cur_frame)>0) {
				int ind = 0;
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte a = 0;
						byte r = cur_frame[ind];
						byte g = cur_frame[ind + height * width];
						byte b = cur_frame[ind + height * width * 2];
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						if(y + y_offset >= 0 && y + y_offset < height && x + x_offset >= 0 && x + x_offset < width){
							img.setRGB(x + x_offset, y + y_offset, pix);
						}
						ind++;
					}
				}
				if(x_offset > 0){
					ind = 0;
					for(int n = 0; n < height; n++){
						for(int m = 0; m < x_offset; m++){
							set_img(ind, img, m, n, pre_frame);
						}
					}
					ind = 0;
					if(y_offset < 0){
						for(int n = height + y_offset; n < height; n++){
							for(int m = x_offset; m < width; m++){
								set_img(ind, img, m, n, pre_frame);
							}
						}
					}
					else{
						for(int n = 0; n < y_offset; n++){
							for(int m = x_offset; m < width; m++){
								set_img(ind, img, m, n, pre_frame);
							}
						}
					}
				}
				else{
					ind = 0;
					for(int n = 0; n < height; n++){
						for(int m = width + x_offset; m < width; m++){
							set_img(ind, img, m, n, pre_frame);
						}
					}
					ind = 0;
					if(y_offset < 0){
						for(int n = height + y_offset; n < height; n++){
							for(int m = 0; m < width + x_offset; m++){
								set_img(ind, img, m, n, pre_frame);
							}
						}
					}
					else{
						for(int n = 0; n < y_offset; n++){
							for(int m = 0; m < width + x_offset; m++){
								set_img(ind, img, m, n, pre_frame);
							}
						}
					}
					
				}
				images.add(deepCopy(img));
			}
		}	
		return cur_frame;
	}

    public static void getAllVector(byte[] bytes, InputStream is) throws IOException {
        if(is.read(bytes)>0) {
            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
//            vlist.add(ImageTransform.getCharacterVectors(img));
//            System.out.println("the length of vlist: " + vlist.size());
        }
    }
}
