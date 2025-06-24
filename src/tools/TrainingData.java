package tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingData {
    public static class DataRow {
        String instanceName;
        double ir;
        double pc;
        double pm;
        double timeSeconds;
        double result;
        int ng;
        int ps;
        double r1;

        public DataRow(String instanceName, double ir, int ng, int ps, double pc, double pm, double r1,
                double timeSeconds, double accuracy) {
            this.instanceName = instanceName;
            this.ir = ir;
            this.ng = ng;
            this.ps = ps;
            this.timeSeconds = timeSeconds;
            this.pc = pc;
            this.pm = pm;
            this.r1 = r1;
            this.result = accuracy;
        }
    }

    public static void saveTrainingData(List<DataRow> results, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write the header
            // oncewriter.write("Instance,IR,NG,PS,PC,PM,NMP,Time,Accuracy\n");
            writer.write("Instance,IR,NG,PS,PC,PM,NMP,Time,Accuracy\n");
            for (DataRow row : results) {
                writer.write(row.instanceName + "," +
                        row.ir + ","
                        + row.ng + ","
                        + row.ps + ","
                        + row.pc + ","
                        + row.pm + ","
                        + row.r1 + ","
                        + row.timeSeconds + ","
                        + row.result + "\n");
            }
            System.out.println("Saved all GGA local search results to " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}