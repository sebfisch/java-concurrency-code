package sebfisch.coloring.algorithms;

public interface GridColoring {
    void pickNewColors();
    void pickNewColor(int row, int col);
    void onChange(Runnable action);
}
