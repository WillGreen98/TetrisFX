package org.bgw.tetrisfx.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.bgw.tetrisfx.model.Tetromino;

public class PreviewRender {

    private static final Color BG_COLOR = Color.GREY;
    private static final double BORDER_WIDTH = 1.0;

    public void render(GraphicsContext gc, Tetromino piece, int canvasSize) {
        gc.clearRect(0, 0, canvasSize, canvasSize);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, canvasSize, canvasSize);

        if (piece == null) return;

        int[][] shape = piece.getShape();
        int cell = canvasSize / 4;
        int offsetX = (4 - shape[0].length) / 2;
        int offsetY = (4 - shape.length) / 2;

        gc.setFill(piece.getColor());
        for (int r = 0; r < shape.length; r++)
            for (int c = 0; c < shape[r].length; c++)
                if (shape[r][c] == 1)
                    gc.fillRect((offsetX + c) * cell, (offsetY + r) * cell, cell, cell);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(BORDER_WIDTH);
        gc.strokeRect(0, 0, canvasSize, canvasSize);
    }
}
