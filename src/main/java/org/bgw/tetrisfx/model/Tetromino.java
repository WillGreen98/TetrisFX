/* (C)2026 */
package org.bgw.tetrisfx.model;

import javafx.scene.paint.Color;

public class Tetromino {

    private final PieceType type;
    private int rotationIndex = 0;
    private int x = 3;
    private int y = 0;

    public Tetromino(PieceType type) {
        this.type = type;
    }

    // Specifically for Ghosting improvement
    public Tetromino(PieceType type, int rotationIndex, int x, int y) {
        this.type = type;
        this.rotationIndex = rotationIndex;
        this.x = x;
        this.y = y;
    }

    public void rotateClockwise() {
        rotationIndex = (rotationIndex + 1) % 4;
    }

    public void rotateCounterClockwise() {
        rotationIndex = (rotationIndex + 3) % 4;
    }

    public int[][] getShape() {
        return type.getRotation(rotationIndex);
    }

    public Color getColor() {
        return type.getColor();
    }

    public PieceType getType() {
        return type;
    }

    public int getRotationIndex() {
        return rotationIndex;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
