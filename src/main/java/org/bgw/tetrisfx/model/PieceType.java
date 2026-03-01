/* (C)2026 */
package org.bgw.tetrisfx.model;

import javafx.scene.paint.Color;

public enum PieceType {

    // Standard list of pieces
    I(new int[][] {{1, 1, 1, 1}}, Color.CYAN),
    J(new int[][] {{0, 1}, {0, 1}, {1, 1}}, Color.BLUE),
    L(new int[][] {{1, 0}, {1, 0}, {1, 1}}, Color.ORANGE),
    O(new int[][] {{1, 1}, {1, 1}}, Color.YELLOW),
    S(new int[][] {{0, 1, 1}, {1, 1, 0}}, Color.LIMEGREEN),
    T(new int[][] {{0, 1, 0}, {1, 1, 1}}, Color.PURPLE),
    Z(new int[][] {{1, 1, 0}, {0, 1, 1}}, Color.RED);

    private final int[][][] rotations;
    private final Color color;

    PieceType(int[][] baseShape, Color color) {
        this.color = color;
        this.rotations = generateRotations(baseShape);
    }

    private int[][][] generateRotations(int[][] base) {
        int[][][] result = new int[4][][];
        result[0] = copy(base);

        for (int i = 1; i < 4; i++) {
            result[i] = rotateClockwise(result[i - 1]);
        }

        return result;
    }

    private int[][] rotateClockwise(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) rotated[c][rows - 1 - r] = matrix[r][c];

        return rotated;
    }

    private int[][] copy(int[][] original) {
        int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) result[i] = original[i].clone();
        return result;
    }

    public int[][] getRotation(int index) {
        return rotations[index];
    }

    public Color getColor() {
        return color;
    }
}
