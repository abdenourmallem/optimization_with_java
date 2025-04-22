package tools;

public class Pairs implements Comparable<Pairs> {
    int id;
    double value;

    public Pairs(int id, double value) {
        this.id = id;
        this.value = value;
    }

    // needed for the the Arrays.sort() method
    @Override
    public int compareTo(Pairs other) {
        // Sort by value (ascending)
        return Double.compare(other.value, this.value);
    }

    public int getId() {
        return this.id;
    }

    public double getValue() {
        return this.value;
    }

}
