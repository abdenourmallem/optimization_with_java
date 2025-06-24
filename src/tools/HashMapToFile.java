package tools;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

public class HashMapToFile {
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

    public static void saveToFile(Map<String, double[]> map, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(map);
            System.out.println("Map saved to file " + fileName);
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
