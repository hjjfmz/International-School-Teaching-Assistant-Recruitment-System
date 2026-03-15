package ebu6304.storage;

public final class Tsv {
    private Tsv() {}

    @Deprecated
    public static String[] splitLine(String line, int expectedFields) {
        throw new UnsupportedOperationException("TSV storage has been removed. Use Csv/MiniJson/XML stores instead.");
    }

    @Deprecated
    public static String join(String... fields) {
        throw new UnsupportedOperationException("TSV storage has been removed. Use Csv/MiniJson/XML stores instead.");
    }
}
