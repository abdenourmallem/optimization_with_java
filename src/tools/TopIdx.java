package tools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class TopIdx {
    public static  int[] topIdx(double[] arr, int k) {
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingDouble(a -> arr[a[0]]));

        for (int i = 0; i < arr.length; i++) {
            if (minHeap.size() < k) {
                minHeap.offer(new int[] { i });
            } else if (arr[i] > arr[minHeap.peek()[0]]) {
                minHeap.poll();
                minHeap.offer(new int[] { i });
            }
        }

        // Extract and sort indices in descending order of their values
        Integer[] tempResult = new Integer[minHeap.size()];
        int i = 0;
        while (!minHeap.isEmpty()) {
            tempResult[i++] = minHeap.poll()[0];
        }

        Arrays.sort(tempResult, (a, b) -> Double.compare(arr[b], arr[a])); // Sort by value descending

        // Convert Integer[] to int[]
        
        int[] result = Arrays.stream(tempResult).mapToInt(Integer::intValue).toArray();
        return result;
    }

}
