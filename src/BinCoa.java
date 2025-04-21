import java.util.Arrays;

import tools.MKP;

public class BinCoa {
    public static void main(String[] args) {
        MKP mkpInstance = new MKP("..\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.50_1.dat");
        System.out.println(mkpInstance.details[0]);
        System.out.println(mkpInstance.details[1]);
        System.out.println(Arrays.toString(mkpInstance.profits));

        for (int[] row : mkpInstance.weights) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println(Arrays.toString(mkpInstance.capacities));
        System.out.println("added new thing");
    }
}
