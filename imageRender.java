

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by user on 4/23/16.
 */
public class imageRender {
    public static void main(String[] args) throws IOException {
        int index = SIFThread.findPicture(args[1], args[0]);
        System.out.println("Find index: " + index);
        InputStream is = new FileInputStream(new File(args[0]));
        byte[] bytes = new byte[3*480*270];
        while(is.read(bytes)>0&&index>0) {
            index--;
        }
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        GridBagConstraints c = new GridBagConstraints();
        JLabel lbIm1 = new JLabel(new ImageIcon(render(is, 480, 270)));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(lbIm1, c);
        frame.pack();
        frame.setVisible(true);
    }

    public static int findPic(String targetImage, String sourcesVideo, int interval) throws IOException {
        SIFT sift = new SIFT();
        System.out.println("Processing target image...");
        RenderImage target = new RenderImage(render(targetImage, 1280, 720));
        sift.detectFeatures(target.toPixelFloatArray(null));
        List<KDFeaturePoint> targetPoint = sift.getGlobalKDFeaturePoints();
        System.out.println("Processing target image...Done");
        InputStream source = new FileInputStream(new File(sourcesVideo));
        int width = 480;
        int height = 270;
        int count = 0;
        int index = 0;
        int max = 0;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = new byte[3*width*height];
        System.out.println("Processing source video...");
        while(source.read(bytes)>0) {
            count++;
            if(count%interval==1) {
                int ind = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        byte a = 0;
                        byte r = bytes[ind];
                        byte g = bytes[ind + height * width];
                        byte b = bytes[ind + height * width * 2];
                        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                        //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                        image.setRGB(x, y, pix);
                        ind++;
                    }
                }
                sift = new SIFT();
                RenderImage sample = new RenderImage(image);
                System.out.println("Creating feature point for frame " + count);
                sift.detectFeatures(sample.toPixelFloatArray(null));
                List<KDFeaturePoint> samplePoint = sift.getGlobalKDFeaturePoints();
                System.out.println("Matching...");
                int size = MatchKeys.filterMore(MatchKeys.findMatchesBBF(samplePoint, targetPoint)).size();
                if(size>max) {
                    index = count;
                    max = size;
                }
                System.out.println("Matched pair: " + size);
            }
        }
        return index;
    }

    public static BufferedImage render(String fileName, int width, int height) throws IOException {
        InputStream is = new FileInputStream(new File(fileName));
        BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        byte[] buffer = new byte[3*width*height];
        if(is.read(buffer)>0) {
            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = buffer[ind];
                    byte g = buffer[ind + height * width];
                    byte b = buffer[ind + height * width * 2];
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    image.setRGB(x, y, pix);
                    ind++;
                }
            }
        }
        return image;
    }

    public static BufferedImage render(InputStream is, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        byte[] buffer = new byte[3*width*height];
        if(is.read(buffer)>0) {
            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = buffer[ind];
                    byte g = buffer[ind + height * width];
                    byte b = buffer[ind + height * width * 2];
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    image.setRGB(x, y, pix);
                    ind++;
                }
            }
        }
        return image;
    }

    public static Interval getIntervalByIndex(int index){
        int half = 0, start = 0, end = 0;
        if(index-30<0) {
            half = index;
            end = 60;
        } else if (index+30>4499){
            end = 4499;
            start = 4499 - 60;
        } else {
            start = index - 30;
            end = index + 30;
        }
        return new Interval(start, end);
    }

}
