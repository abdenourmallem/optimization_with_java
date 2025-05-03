package tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultBatch {

    // A simple class to store one result
    public static class ResultRow {
        String instanceName;
        double avgDFO;
        double bestDFO;
        double worstDFO;
        double timeSeconds;

        public ResultRow(String instanceName, double avgDFO, double bestDFO, double worstDFO, double timeSeconds) {
            this.instanceName = instanceName;
            this.avgDFO = avgDFO;
            this.bestDFO = bestDFO;
            this.worstDFO = worstDFO;
            this.timeSeconds = timeSeconds;
        }
    }

    public static void saveAllResults(List<ResultRow> results, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write the header once
            writer.write("Instance,Average_DFO,Best_DFO,Worst_DFO,Time\n");

            int rowsCounter = 0;
            for (ResultRow row : results) {

                if (rowsCounter == 6) {
                    writer.write(row.instanceName + "\n");
                    rowsCounter = 0;
                }
                writer.write(row.instanceName + ","
                        + row.avgDFO + ","
                        + row.bestDFO + ","
                        + row.worstDFO + ","
                        + row.timeSeconds + "\n");
                rowsCounter += 1;
            }
            System.out.println("Saved all GGA local search results to " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example usage
    public static void main(String[] args) {
        List<ResultRow> allResults = new ArrayList<>();

        // After solving each instance, add the results like this:
        allResults.add(new ResultRow("0.25_01", 1.14, 0.39, 1.41, 0.82));
        allResults.add(new ResultRow("0.25_10", 0.31, 0.0, 0.6, 0.87));
        allResults.add(new ResultRow("0.50_01", 0.60, 0.12, 1.06, 1.06));
        allResults.add(new ResultRow("0.50_10", 0.36, 0.09, 1.0, 1.05));
        allResults.add(new ResultRow("0.75_01", 0.52, 0.04, 0.86, 1.08));

        // When everything is done, save all at once:
        saveAllResults(allResults, "gga_local_search_results.csv");
    }
}