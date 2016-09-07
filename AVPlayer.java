
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


public class AVPlayer {
	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	static BufferedImage img;
	LinkedList<BufferedImage> images;
	private static int width = 480;
	private static int height = 270;
	static InputStream is;
	byte[] bytes;
	static Clip clip;
	long tStart;
	long clipPosition = 0;
	int indexOfFrame = 0;
	static BufferedImage targetFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	public void initialize(String[] args) throws IOException {
		images = new LinkedList<BufferedImage>();
		try {
			File file = new File(args[0]);
			is = new FileInputStream(file);
			//long len = file.length();
			long len = width * height * 3;
			bytes = new byte[(int) len];
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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


//		lbIm1 = new JLabel(new ImageIcon(targetFrame));


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
		addButtion();
//		renderImage(bytes, is, images);
//		try {
//			findPic(args);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		frame.pack();
//		frame.repaint();
		frame.setVisible(true);
	}

	public static void prepareWAV(String filename) {
		try {
			// Open an audio input stream.
			File soundFile = new File(filename); //you could also get the sound file with an URL
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			// Get a sound clip resource.
			clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
		    return;
		}
		AVPlayer ren = new AVPlayer();
		try {
			ren.initialize(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		prepareWAV(args[1]);

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
		Timer timer = new Timer(64, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					indexOfFrame++;
					renderImage(bytes, is, images);
					lbIm1 = new JLabel(new ImageIcon(images.removeFirst()));
					if(indexOfFrame%3!=0) {
						TimeUnit.MICROSECONDS.sleep(1);
					}
					frame.repaint();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				tStart = System.nanoTime();
				timer.setRepeats(true);
				timer.start();
				clip.start();
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
				clip.stop();
				long deltaTime = System.nanoTime() - tStart;
				clipPosition += deltaTime;
				clip.setMicrosecondPosition(clipPosition/1000);
			}
		});
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}

	public static void renderImage(byte[] bytes, InputStream is, LinkedList<BufferedImage> images) throws IOException {
		if(images.size()<=100&&is.read(bytes)>0) {
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
			images.add(deepCopy(img));
		}
	}

//	public static void findPic(String[] args) throws IOException {
//		if(args.length<=2) {
//			return;
//		}
//		long len = 3 * width * height;
//		File target = new File(args[2]);
//		byte[] buffer = new byte[(int)len];
//		try {
//			InputStream im = new FileInputStream(target);
//			is.read(buffer);
//			System.out.println("buffer String is: " + buffer);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		byte[] bytes = new byte[3*width*height];
//		int counter = 1;
//		int ind = 0;
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				byte a = 0;
//				byte r = buffer[ind];
//				byte g = buffer[ind + height * width];
//				byte b = buffer[ind + height * width * 2];
//				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
//				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
//				targetFrame.setRGB(x, y, pix);
//				ind++;
//			}
//		}
//		InputStream input = new FileInputStream(new File(args[0]));
//		while(input.read(bytes)>0) {
//			System.out.println(bytes);
//			if(Arrays.equals(buffer, bytes)) break;
//			counter++;
//		}
//		if(counter>4500) {
//			System.out.println("Cannot find the frame");
//		} else {
//			System.out.println("the frame is number " + counter);
//		}
//	}
}