/* (C)2026 */
package org.bgw.tetrisfx.model;

public class Board {

    private final int width;
    private final int height;
    private final PieceType[][] grid;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new PieceType[height][width];
    }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = null;
            }
        }
    }

    public boolean isValidPosition(Tetromino piece) {
        int[][] shape = piece.getShape();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {

                if (shape[r][c] == 0) continue;

                int boardX = piece.getX() + c;
                int boardY = piece.getY() + r;

                if (boardX < 0 || boardX >= width || boardY < 0 || boardY >= height) {
                    return false;
                }

                if (grid[boardY][boardX] != null) {
                    return false;
                }
            }
        }

        return true;
    }

    public void place(Tetromino piece) {
        int[][] shape = piece.getShape();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    grid[piece.getY() + r][piece.getX() + c] = piece.getType();
                }
            }
        }
    }

    public int clearLines() {
        int cleared = 0;

        for (int y = height - 1; y >= 0; y--) {
            boolean full = true;

            for (int x = 0; x < width; x++) {
                if (grid[y][x] == null) {
                    full = false;
                    break;
                }
            }

            if (full) {
                removeLine(y);
                cleared++;
                y++;
            }
        }

        return cleared;
    }

    private void removeLine(int row) {
        if (row >= height || row < 0) {
            throw new IllegalArgumentException("Invalid row index: " + row);
        }

        for (int y = row; y > 0; y--) {
            System.arraycopy(grid[y - 1], 0, grid[y], 0, width);
        }

        grid[0] = new PieceType[width];
    }

    public PieceType getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException();
        }

        return grid[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
