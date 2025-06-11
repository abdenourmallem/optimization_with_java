package tools;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import tools.ResultBatchSac.RRow;
import tools.qAgent.Action;

public class saveQtable {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("apple", 3);
        map.put("banana", 5);

        // Serialize
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("map.ser"))) {
            oos.writeObject(map);
            System.out.println("Map saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToFile(Map<String, Map<Action, Double>> Q, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
        // Optional: write a header
        writer.write("State,Actions\n");

        for (Map.Entry<String, Map<Action, Double>> entry : Q.entrySet()) {
            String state = entry.getKey();
            Map<Action, Double> actions = entry.getValue();

            // Convert actions to desired string format: (add, 20.0), (remove, 15.0), ...
            String actionsString = actions.entrySet().stream()
                .map(e -> "(" + e.getKey().operation + ":" + e.getKey().index + "," + e.getValue() + ")")
                .collect(Collectors.joining(","));

            writer.write(state + "," + actionsString + "\n");
        }

        System.out.println("Q-table saved to " + fileName);

    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public static Map<String, double[]> readFromFile(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            @SuppressWarnings("unchecked")
            Map<String, double[]> map = (Map<String, double[]>) ois.readObject();
            System.out.println("Map loaded from file: " + fileName);
            return map;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
