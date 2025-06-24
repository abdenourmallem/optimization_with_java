package tools;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.HashSet;

public class TopIdx {
    public static int[] topIdx(double[] arr, int k) {
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

    public static void main(String[] args) {
        double[] arr = { 5.0, 1.2, 9.8, 3.3, 7.7, 0.4, 6.1, 8.6 };
        int k = 3;

        int[] topKIndices = topIdx(arr, k);

        System.out.println("Original Array: " + Arrays.toString(arr));
        System.out.println("Top " + k + " Indices: " + Arrays.toString(topKIndices));

        System.out.print("Top " + k + " Values: ");
        for (int i : topKIndices) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }

}