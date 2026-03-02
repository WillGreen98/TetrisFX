package org.bgw.tetrisfx.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.bgw.tetrisfx.model.PieceType;
import org.bgw.tetrisfx.model.Tetromino;

public class BoardRender {

    private final int cellSize;
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color GRID_COLOR = Color.web("#222");
    private static final double GHOST_ALPHA = 0.3;

    public BoardRender(int cellSize) {
        this.cellSize = cellSize;
    }

    public void render(GraphicsContext gc, PieceType[][] grid, Tetromino current, Tetromino ghost) {
        int height = grid.length;
        int width = grid[0].length;

        int pixelWidth = width * cellSize;
        int pixelHeight = height * cellSize;

        gc.clearRect(0, 0, pixelWidth, pixelHeight);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, pixelWidth, pixelHeight);

        // Grid
        gc.setStroke(GRID_COLOR);
        for (int x = 0; x <= width; x++) gc.strokeLine(x * cellSize, 0, x * cellSize, pixelHeight);
        for (int y = 0; y <= height; y++) gc.strokeLine(0, y * cellSize, pixelWidth, y * cellSize);

        // Locked pieces
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                PieceType cell = grid[y][x];
                if (cell != null) {
                    gc.setFill(cell.getColor());
                    gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }

        if (ghost != null) {
            gc.save();
            gc.setGlobalAlpha(GHOST_ALPHA);
            drawPiece(gc, ghost);
            gc.restore();
        }

        if (current != null) {
            drawPiece(gc, current);
        }
    }

    private void drawPiece(GraphicsContext gc, Tetromino piece) {
        int[][] shape = piece.getShape();
        gc.setFill(piece.getColor());
        for (int r = 0; r < shape.length; r++)
            for (int c = 0; c < shape[r].length; c++)
                if (shape[r][c] == 1)
                    gc.fillRect(
                            (piece.getX() + c) * cellSize,
                            (piece.getY() + r) * cellSize,
                            cellSize,
                            cellSize);
    }
}
