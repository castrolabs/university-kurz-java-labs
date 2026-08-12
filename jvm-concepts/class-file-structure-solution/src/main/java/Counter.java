public class Counter {

    public int value;

    static int valueInstance;

    Counter(int newValue) {
        value = newValue;
        valueInstance = newValue;
    }
}
