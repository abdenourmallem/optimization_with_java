import java.util.Arrays;

import tools.*;

public class BinCoa {

    public static void main(String[] args) {
        final int nPop = 200;
        final int nIter = 10;
        final float xorProb = (float) 0.2;
        final int effBias = 2;
        final int ub = 1;
        final int lb = 0;

        MKP mkpInstance = new MKP("..\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat");
        mkpInstance.printMKPDetails();

        for (int i = 0; i < 20; i++) {
            Candidate cand = new Candidate(mkpInstance, effBias);
            System.out
                    .println("Candidate objective value: " + cand.objValue + " " + cand.checkConstraints(mkpInstance));
        }
        // System.out.println("Candidate position: " + Arrays.toString(cand.position));
    }

}
