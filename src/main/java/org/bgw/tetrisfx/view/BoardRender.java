package org.bgw.tetrisfx.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.bgw.tetrisfx.model.Board;
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

    public void render(GraphicsContext gc, Board board, Tetromino current, Tetromino ghost) {

        int width = board.getWidth() * this.cellSize;
        int height = board.getHeight() * this.cellSize;

        gc.clearRect(0, 0, width, height);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, width, height);

        /* grid */
        gc.setStroke(GRID_COLOR);
        for (int x = 0; x <= board.getWidth(); x++)
            gc.strokeLine(x * this.cellSize, 0, x * this.cellSize, height);
        for (int y = 0; y <= board.getHeight(); y++)
            gc.strokeLine(0, y * this.cellSize, width, y * this.cellSize);

        /* locked pieces */
        PieceType[][] grid = board.getGrid();
        for (int y = 0; y < board.getHeight(); y++)
            for (int x = 0; x < board.getWidth(); x++)
                if (grid[y][x] != null) {
                    gc.setFill(grid[y][x].getColor());
                    gc.fillRect(x * this.cellSize, y * this.cellSize, this.cellSize, this.cellSize);
                }

        /* ghost */
        if (ghost != null) {
            gc.save();
            gc.setGlobalAlpha(GHOST_ALPHA);
            drawPiece(gc, ghost);
            gc.restore();
        }

        /* current */
        drawPiece(gc, current);
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
