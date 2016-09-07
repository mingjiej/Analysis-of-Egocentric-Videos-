

import java.util.*;

/**
 * Created by user on 4/23/16.
 */
public class Interval {
    private int start;
    private int end;
    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }
    public static ArrayList<Interval> mergeInterval(ArrayList<Interval> interval1, ArrayList<Interval> interval2) {
        int index1 = 0, index2 = 0;
        ArrayList<Interval> intervals = new ArrayList<>();
        while(index1<interval1.size()&&index2<interval2.size()) {
            Interval i = interval1.get(index1);
            Interval j = interval2.get(index2);
            if(!(i.getEnd()<j.getStart()||j.getEnd()<i.getStart())) {
                if(i.getEnd()>j.getEnd()) {
                    index2++;
                    intervals.add(new Interval(Math.max(i.getStart(), j.getStart()), j.getEnd()));
                    i.start = j.getEnd();
                } else {
                    index1++;
                    intervals.add(new Interval(Math.max(i.getStart(), j.getStart()), i.getEnd()));
                    j.start = i.getEnd();
                }
            } else {
                if(i.getStart()<j.getStart()) index1++;
                else index2++;
            }
        }
        return intervals;

    }
    public static ArrayList<Interval> createIntervalList(List<Integer> list) {
        ArrayList<Interval> res = new ArrayList<Interval>();
        Iterator<Integer> iter = list.iterator();
        while(iter.hasNext()) {
            res.add(new Interval(iter.next(), iter.next()));
        }
        return res;
    }
    public static void removeIntervalsUnderThrehold(ArrayList<Interval> list, int threhold) {
        Iterator<Interval> iter = list.iterator();
        while(iter.hasNext()) {
            Interval in = iter.next();
            if(in.end - in.start + 1<threhold) {
                iter.remove();
            }
        }
    }
    public static void sortIntervalWithLength(ArrayList<Interval> interval) {
        Collections.sort(interval, new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                return -1 * Integer.compare(o1.getLength(), o2.getLength());
            }
        });
    }
    public int getStart() {
        return start;
    }
    public int getEnd() {
        return end;
    }
    public int getLength() {
        return end - start + 1;
    }
    public static void print(List<Interval> list) {
        for(Interval i: list) {
            System.out.println(i.start + " " + i.end);
        }
    }
    public static void filter(ArrayList<Interval> interval) {
        Interval.sortIntervalWithLength(interval);
        int length = 0;
        int i=0;
        for(;i<interval.size();i++) {
            if(length>1350) break;
            length += interval.get(i).getLength();
        }
        int temp = i;
        for(; i<interval.size();i++) {
            interval.remove(temp);
        }
        Collections.sort(interval, new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                return o1.getStart() - o2.getStart();
            }
        });
    }
    public static void report(ArrayList<Interval> interval) {
        int count = 0;
        int length = 0;
        for(Interval i: interval) {
            count++;
            length += i.getLength();
        }
        System.out.println("The total length is: " + length);
        System.out.println("The number of intervals is: " + count);
        System.out.println("Average length: " + length/count);

    }
    public static void reverse(ArrayList<Interval> interval) {
        int end = 4500;
        int last = 0;
        int size = interval.size();
        for(int i = 0;i<size;i++) {
            Interval inter = interval.remove(0);
            interval.add(new Interval(last, inter.getStart()));
            last = inter.getEnd();
        }
        interval.add(new Interval(last, end));
    }

    public static Interval generateIntervalForTargetImage(int targetFrame, int start, int end) {
        int half = 0, iStart = 0, iEnd = 0;
        if(targetFrame - start < 30) iStart = start;
        if(end-targetFrame<30) iEnd = end;
        if(iStart!=0) {
            half = 60 - (targetFrame - iStart);
            iEnd = targetFrame + half;
        } else if(iEnd!=0) {
            half = 60 + (targetFrame - iStart);
            iStart = targetFrame - half;
        }
        return new Interval(iStart, iEnd);
    }
}
