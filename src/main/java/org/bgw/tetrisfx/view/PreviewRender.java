/* (C)2026 */
package org.bgw.tetrisfx.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.bgw.tetrisfx.model.Tetromino;

public class PreviewRender {

    public void render(GraphicsContext gc, Tetromino piece, int canvasSize) {

        gc.clearRect(0, 0, canvasSize, canvasSize);

        // Background
        gc.setFill(Color.web("#2b2b2b"));
        gc.fillRect(0, 0, canvasSize, canvasSize);

        if (piece == null) return;

        int[][] shape = piece.getShape();

        int cell = canvasSize / 4; // preview grid 4x4

        int shapeWidth = shape[0].length;
        int shapeHeight = shape.length;

        int offsetX = (4 - shapeWidth) / 2;
        int offsetY = (4 - shapeHeight) / 2;

        gc.setFill(piece.getColor());

        for (int r = 0; r < shapeHeight; r++) {
            for (int c = 0; c < shapeWidth; c++) {
                if (shape[r][c] == 1) {
                    gc.fillRect((offsetX + c) * cell, (offsetY + r) * cell, cell, cell);
                }
            }
        }

        // Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0, 0, canvasSize, canvasSize);
    }
}
