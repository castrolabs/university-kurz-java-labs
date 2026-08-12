public class Counter {

    // TODO-00: This field is documented as carrying ACC_PUBLIC, but nothing in
    // the declaration asks the compiler for that. Add the modifier that makes
    // it true.
    int value;

    // Declared `static`, so the compiler sets ACC_STATIC here: one shared slot
    // on the class itself, not one per Counter instance.
    static int valueInstance;

    Counter(int newValue) {
        value = newValue;
        valueInstance = newValue;
    }
}
