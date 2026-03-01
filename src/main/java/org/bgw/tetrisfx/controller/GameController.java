/* (C)2026 */
package org.bgw.tetrisfx.controller;

import java.util.HashSet;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import org.bgw.tetrisfx.model.Board;
import org.bgw.tetrisfx.model.Tetromino;
import org.bgw.tetrisfx.service.GameEngine;
import org.bgw.tetrisfx.service.GameListener;
import org.bgw.tetrisfx.view.BoardRender;
import org.bgw.tetrisfx.view.PreviewRender;

public class GameController implements GameListener {
    @FXML private Canvas gameCanvas;
    @FXML private Canvas hold;
    @FXML private Canvas next;
    @FXML private Label scoreLabel;
    @FXML private Label linesLabel;
    @FXML private Label levelLabel;
    @FXML private Label statusLabel;

    private GameEngine engine;
    private AnimationTimer gameLoop;

    private BoardRender renderer;
    private PreviewRender previewRenderer;

    // TODO: improve this to be an EnumSet
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    // TODO: use DAS the in the
    private static final long DAS = 150_000_000; // 150ms
    private static final long ARR = 40_000_000; // 40ms repeat

    private long lastLeftMove = 0;
    private long lastRightMove = 0;

    @FXML
    private void initialize() {
        engine = new GameEngine(10, 20);
        engine.setListener(this);

        renderer = new BoardRender();
        previewRenderer = new PreviewRender();

        // This feels so much more responsive than before.
        gameCanvas
                .sceneProperty()
                .addListener(
                        (obs, oldScene, scene) -> {
                            if (scene == null) return;

                            scene.addEventHandler(
                                    javafx.scene.input.KeyEvent.KEY_PRESSED,
                                    e -> {
                                        pressedKeys.add(e.getCode());
                                    });

                            scene.addEventHandler(
                                    javafx.scene.input.KeyEvent.KEY_RELEASED,
                                    e -> {
                                        pressedKeys.remove(e.getCode());
                                    });

                            gameCanvas.requestFocus();
                        });

        setupGameLoop();
    }

    @FXML
    private void onStart() {
        engine.reset();
        statusLabel.setText("READY");
        gameLoop.start();
    }

    private void setupGameLoop() {
        gameLoop =
                new AnimationTimer() {

                    private long lastTime = 0;
                    private double accumulator = 0;

                    @Override
                    public void handle(long now) {
                        // Initialise lastTime on first frame
                        if (lastTime == 0) {
                            lastTime = now;

                            return;
                        }

                        // Convert nanoseconds to seconds for easier math
                        double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
                        lastTime = now;

                        // Update input once per frame
                        handleInput(now);

                        // Fixed‑timestep loop (drop interval in seconds)
                        double dropInterval = engine.getDropInterval() / 1_000_000_000.0;
                        accumulator += deltaSeconds;

                        while (accumulator >= dropInterval) {
                            engine.tick(); // advance game logic
                            accumulator -= dropInterval;
                        }

                        renderer.render(
                                gameCanvas.getGraphicsContext2D(),
                                engine.getBoard(),
                                engine.getCurrent(),
                                engine.getGhost(),
                                30);
                    }
                };
    }

    private boolean consumeKey(KeyCode key) {
        if (pressedKeys.contains(key)) {
            pressedKeys.remove(key);
            return true;
        }
        return false;
    }

    // TODO: fix SHIFT + key to work...
    private void handleInput(long now) {
        // LEFT
        if (pressedKeys.contains(KeyCode.LEFT)) {
            if (now - lastLeftMove > ARR) {
                engine.moveLeft();
                lastLeftMove = now;
            }
        }

        // RIGHT
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            if (now - lastRightMove > ARR) {
                engine.moveRight();
                lastRightMove = now;
            }
        }

        // SOFT DROP (continuous)
        if (pressedKeys.contains(KeyCode.DOWN)) {
            engine.softDrop();
        }

        // Single press actions
        if (consumeKey(KeyCode.UP)) {
            engine.rotate();
        }

        if (consumeKey(KeyCode.ENTER)) {
            engine.hardDrop();
        }

        if (consumeKey(KeyCode.H)) {
            engine.hold();
        }
    }

    @Override
    public void onBoardUpdated(
            Board board, Tetromino current, Tetromino nextPiece, Tetromino holdPiece) {
        previewRenderer.render(next.getGraphicsContext2D(), nextPiece, (int) next.getWidth());

        previewRenderer.render(hold.getGraphicsContext2D(), holdPiece, (int) hold.getWidth());
    }

    @Override
    public void onScoreUpdated(int score, int lines, int level) {
        scoreLabel.setText(String.valueOf(score));
        linesLabel.setText(String.valueOf(lines));
        levelLabel.setText(String.valueOf(level));
    }

    @Override
    public void onGameOver() {
        // Set gameover state to be true
        statusLabel.setText("GAME OVER");
    }
}
