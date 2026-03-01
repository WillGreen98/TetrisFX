/* (C)2026 */
package org.bgw.tetrisfx.view;

import javafx.scene.canvas.GraphicsContext;
import org.bgw.tetrisfx.model.Board;
import org.bgw.tetrisfx.model.PieceType;
import org.bgw.tetrisfx.model.Tetromino;

public class BoardRender {
    public void render(
            GraphicsContext gc, Board board, Tetromino current, Tetromino ghost, int cell) {

        int width = board.getWidth() * cell;
        int height = board.getHeight() * cell;

        gc.clearRect(0, 0, width, height);

        // Background
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, width, height);

        // Grid lines
        gc.setStroke(javafx.scene.paint.Color.web("#222"));
        for (int x = 0; x <= board.getWidth(); x++) gc.strokeLine(x * cell, 0, x * cell, height);

        for (int y = 0; y <= board.getHeight(); y++) gc.strokeLine(0, y * cell, width, y * cell);

        PieceType[][] grid = board.getGrid();

        // Locked pieces
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (grid[y][x] != null) {
                    gc.setFill(grid[y][x].getColor());
                    gc.fillRect(x * cell, y * cell, cell, cell);
                }
            }
        }

        // Ghost piece
        // This logic can be improved - Stateful operations should restore previous state
        // explicitly.
        if (ghost != null) {
            gc.setGlobalAlpha(0.3);
            drawPiece(gc, ghost, cell);
            gc.setGlobalAlpha(1.0);
        }

        // Current piece
        drawPiece(gc, current, cell);
    }

    private void drawPiece(GraphicsContext gc, Tetromino piece, int cell) {
        int[][] shape = piece.getShape();
        gc.setFill(piece.getColor());

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    gc.fillRect((piece.getX() + c) * cell, (piece.getY() + r) * cell, cell, cell);
                }
            }
        }
    }
}
