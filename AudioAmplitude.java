

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 4/23/16.
 */
public class AudioAmplitude {

    private final static int PIVOT = 25;
    public static int[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels)  {
        int[][] toReturn = new int[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
        int index = 0;
        for (int audioByte = 0; audioByte < eightBitByteArray.length;)
        {
            for (int channel = 0; channel < nbChannels; channel++)
            {
                // Do the byte to sample conversion.
                int low = (int) eightBitByteArray[audioByte];
                audioByte++;
                int high = (int) eightBitByteArray[audioByte];
                audioByte++;
                int sample = (high << 8) + (low & 0x00ff);

                toReturn[channel][index] = sample;
            }
            index++;
        }
        return toReturn;

    }

    public static ArrayList<Interval> returnIntervals(int[] rangeSum) {
        ArrayList<Interval> interval = new ArrayList<>();
        int first = 0;
        int end = 1;
        while(end<rangeSum.length) {
            if(rangeSum[first]!=rangeSum[end]) {
                interval.add(new Interval(first, end-1));
                first = end;
            }
            end++;
        }
        interval.add(new Interval(first, end-1));
        return interval;
    }


    public  static ArrayList<Interval> calculateVoice(String file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
        byte[] bytes = new byte[(int) (audioInputStream.getFrameLength()) * (audioInputStream.getFormat().getFrameSize())];
        audioInputStream.read(bytes);
        int[][] graphData = getUnscaledAmplitude(bytes, 1);
        int[] ans = new int[4500];
        int sum = 0;
        for(int i = 0;i<graphData[0].length;i++) {
            if(i!=0&&i%1600==0) {
                ans[i/1600] = sum/1600;
                sum = 0;
            }
            sum += graphData[0][i];
        }
        int[] rangeSum = new int[4500];
        int max = 0;
        for(int i=0;i<45;i++) {
            max = Math.max(ans[i], max);
        }
        rangeSum[0] = max;
        for(int i=1;i<rangeSum.length-44;i++) {
            max = Math.max(max, ans[i+44]);
            rangeSum[i] = max;
        }
        for(int i = rangeSum.length-44;i<rangeSum.length;i++){
            rangeSum[i] = max;
        }

        for(int i=0;i<rangeSum.length;i++) {
            rangeSum[i] = rangeSum[i]/PIVOT;
        }
        return returnIntervals(rangeSum);
    }
}
