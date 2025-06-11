package tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultBatchSac {

    // A simple class to store one result
    public static class RRow {
        String instanceName;
        double successRate;
        double stDev;

        public RRow(String instanceName, double successRate, double stDev) {
            this.instanceName = instanceName;
            this.successRate = successRate;
            this.stDev = stDev;
        }
    }

    public static void saveAllResults(List<RRow> results, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write the header once
            writer.write("Instance,SR,STD\n");

            int rowsCounter = 0;
            for (RRow row : results) {

                if (rowsCounter == 3) {
                    writer.write(row.instanceName + "\n");
                    rowsCounter = 0;
                }
                writer.write(row.instanceName + ","
                        + row.successRate + ","
                        + row.stDev + "\n");
                rowsCounter += 1;
            }
            System.out.println("Saved all Bincoa results to " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example usage
    // public static void main(String[] args) {
    // List<ResultRow> allResults = new ArrayList<>();

    // // After solving each instance, add the results like this:
    // allResults.add(new ResultRow("0.25_01", 1.14, 0.39, 1.41, 0.82));
    // allResults.add(new ResultRow("0.25_10", 0.31, 0.0, 0.6, 0.87));
    // allResults.add(new ResultRow("0.50_01", 0.60, 0.12, 1.06, 1.06));
    // allResults.add(new ResultRow("0.50_10", 0.36, 0.09, 1.0, 1.05));
    // allResults.add(new ResultRow("0.75_01", 0.52, 0.04, 0.86, 1.08));

    // // When everything is done, save all at once:
    // saveAllResults(allResults, "gga_local_search_results.csv");
    // }
}