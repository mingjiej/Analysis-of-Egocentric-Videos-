

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * Created by user on 4/29/16.
 */
public class SIFThread {
    private int[] answer = null;
    private List<KDFeaturePoint> targetPoint;
    private int[] ans1;
    private int[] ans2;
    private int[] ans3;
    private int[] ans4;

    public SIFThread(List<KDFeaturePoint> targetPoint) {
        this.targetPoint = targetPoint;
        this.answer = new int[2];
        this.ans1 = new int[2];
        this.ans2 = new int[2];
        this.ans3 = new int[2];
        this.ans4 = new int[2];
    }

    public void work(int start, int end, InputStream source, int index, int intervals) throws IOException {
        int count = start;
        int max = 0;
        int frame = 0;
        BufferedImage image = new BufferedImage(480, 270, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = new byte[3*480*270];
        while(count<=end&&source.read(bytes)>0) {
            count++;
            if(count%intervals==1) {
                int ind = 0;
                for (int y = 0; y < 270; y++) {
                    for (int x = 0; x < 480; x++) {
                        byte a = 0;
                        byte r = bytes[ind];
                        byte g = bytes[ind + 270 * 480];
                        byte b = bytes[ind + 270 * 480 * 2];
                        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                        //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                        image.setRGB(x, y, pix);
                        ind++;
                    }
                }
                SIFT sift = new SIFT();
                RenderImage sample = new RenderImage(image);
                System.out.println("Creating feature point for frame " + count);
                sift.detectFeatures(sample.toPixelFloatArray(null));
                List<KDFeaturePoint> samplePoint = sift.getGlobalKDFeaturePoints();
                System.out.println("Matching...");
                int size = MatchKeys.filterMore(MatchKeys.findMatchesBBF(samplePoint, targetPoint)).size();
                System.out.println("Matched pair: " + size);
                if(max<size) {
                    max = size;
                    frame = count;
                }
            }
        }
        if(index==1) {
            ans1[0] = frame;
            ans1[1] = max;
        } else if(index==2) {
            ans2[0] = frame;
            ans2[1] = max;
        } else if(index==3) {
            ans3[0] = frame;
            ans3[1] = max;
        } else {
            ans4[0] = frame;
            ans4[1] = max;
        }
    }
    public void findMaxAmountThread() {
       if(ans1[1]>=ans2[1]&&ans1[1]>=ans3[1]&&ans1[1]>=ans4[1]) {
           answer[0] = ans1[0];
       } else if(ans2[1]>=ans1[1]&&ans2[1]>=ans3[1]&&ans2[1]>=ans4[1]){
           answer[0] = ans2[0];
       } else if(ans3[1]>=ans2[1]&&ans3[1]>ans1[1]&&ans3[1]>=ans4[1]){
           answer[0] = ans3[0];
       } else {
           answer[0] = ans4[0];
       }
    }

    public int[] getAnswer() {
        return answer;
    }


    public static int findPicture(String targetImage, String videoSource) throws IOException {
        long startTime = System.currentTimeMillis();
        SIFT sift = new SIFT();
        System.out.println("Processing target image...");
        RenderImage target = new RenderImage(imageRender.render(targetImage, 1280, 720));
        sift.detectFeatures(target.toPixelFloatArray(null));
        List<KDFeaturePoint> targetPoint = sift.getGlobalKDFeaturePoints();
        System.out.println("Processing target image...Done");
        InputStream source = new FileInputStream(videoSource);
        SIFThread sf = new SIFThread(targetPoint);
        int[] ans = null;
        try {
            ans = sf.find(videoSource);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sf.findMaxAmountThread();
        System.out.println("Find index: " + sf.answer[0]);
        long stopTime = System.currentTimeMillis();
        System.out.println("Time comsumed: " + (stopTime-startTime));
        return sf.answer[0];
    }

    public int[] find(String fileName) throws FileNotFoundException, InterruptedException {
        InputStream s1 = new FileInputStream(new File(fileName));
        InputStream s2 = new FileInputStream(new File(fileName));
        InputStream s3 = new FileInputStream(new File(fileName));
        InputStream s4 = new FileInputStream(new File(fileName));
        Thread tr1 = new Thread(new Runnable() {
            public void run() {
                try {
                    work(0, 1124, s1, 1, 30);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread tr2 = new Thread(new Runnable() {
            public void run() {
                byte[] bytes = new byte[3*480*270];
                try {
                    int count = 0;
                    while(s2.read(bytes)>0&&count<1125) {
                        count++;
                    }
                    work(1125, 2249, s2, 2, 30);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread tr3 = new Thread(new Runnable() {
            public void run() {
                byte[] bytes = new byte[3*480*270];
                try {
                    int count = 0;
                    while(s3.read(bytes)>0&&count<2250) {
                        count++;
                    }
                    work(2250, 3374, s3, 3, 30);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread tr4 = new Thread(new Runnable() {
            public void run() {
                try {
                    int count = 0;
                    byte[] bytes = new byte[3*480*270];
                    while(s4.read(bytes)>0&&count<3375) {
                        count++;
                    }
                    work(3375, 4500, s4, 4, 30);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        tr1.start();
        tr2.start();
        tr3.start();
        tr4.start();
        try {
            tr1.join();
            tr2.join();
            tr3.join();
            tr4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getAnswer();
    }
}
