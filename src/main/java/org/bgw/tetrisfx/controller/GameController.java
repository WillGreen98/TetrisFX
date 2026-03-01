package org.bgw.tetrisfx.controller;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import org.bgw.tetrisfx.config.Config;
import org.bgw.tetrisfx.model.Board;
import org.bgw.tetrisfx.model.Tetromino;
import org.bgw.tetrisfx.service.GameEngine;
import org.bgw.tetrisfx.service.GameListener;
import org.bgw.tetrisfx.view.BoardRender;
import org.bgw.tetrisfx.view.PreviewRender;

/**
 * UI controller – wires FXML nodes to the game engine.
 */
public class GameController implements GameListener {

    /* UI -------------------------------------------------------------*/
    @FXML private Canvas gameCanvas;
    @FXML private Canvas holdCanvas;
    @FXML private Canvas nextCanvas;
    @FXML private Label scoreLabel, linesLabel, levelLabel, statusLabel;

    /* Game engine & renderers ----------------------------------------*/
    private final GameEngine engine = new GameEngine(Config.BOARD_WIDTH, Config.BOARD_HEIGHT);
    private final BoardRender boardRenderer = new BoardRender(Config.CELL_SIZE);
    private final PreviewRender previewRenderer = new PreviewRender();

    /* Animation loop -----------------------------------------------*/
    private AnimationTimer gameLoop;

    /* Input state ----------------------------------------------------*/
    private final EnumSet<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);

    /* Per‑key timers for DAS/ARR -------------------------------------*/
    private final Map<KeyCode, Long> lastPress = new EnumMap<>(KeyCode.class);
    private final Map<KeyCode, Long> lastMove = new EnumMap<>(KeyCode.class);

    @FXML
    private void initialize() {
        engine.setListener(this);
        setupInput();
        setupGameLoop();
    }

    private void setupInput() {
        gameCanvas
                .sceneProperty()
                .addListener(
                        (obs, oldScene, scene) -> {
                            if (scene == null) return;

                            scene.addEventHandler(
                                    javafx.scene.input.KeyEvent.KEY_PRESSED,
                                    e -> {
                                        pressed.add(e.getCode());
                                        lastPress.put(e.getCode(), System.nanoTime());
                                        lastMove.put(e.getCode(), 0L);
                                    });

                            scene.addEventHandler(
                                    javafx.scene.input.KeyEvent.KEY_RELEASED,
                                    e -> {
                                        pressed.remove(e.getCode());
                                        lastPress.remove(e.getCode());
                                        lastMove.remove(e.getCode());
                                    });

                            gameCanvas.requestFocus();
                        });
    }

    private void setupGameLoop() {
        gameLoop =
                new AnimationTimer() {

                    private long lastTime = 0;
                    private double accumulator = 0.0;

                    @Override
                    public void handle(long now) {
                        if (lastTime == 0) {
                            lastTime = now;
                            return;
                        }

                        double deltaSec = (now - lastTime) / 1_000_000_000.0;
                        lastTime = now;

                        handleInput(now);

                        int levelIndex = Math.min(
                                engine.getLevel() - 1,
                                Config.DROP_INTERVALS.length - 1
                        );

                        double dropInterval =
                                Config.DROP_INTERVALS[levelIndex] / 1_000_000_000.0;
                        accumulator += deltaSec;

                        while (accumulator >= dropInterval) {
                            engine.tick();
                            accumulator -= dropInterval;
                        }

                        boardRenderer.render(
                                gameCanvas.getGraphicsContext2D(),
                                engine.getBoard(),
                                engine.getCurrent(),
                                engine.getGhost());
                    }
                };
    }

    @FXML
    private void onStart() {
        engine.reset();
        statusLabel.setText("READY");
        gameLoop.start();
    }

    private void handleInput(long now) {
        // Left / Right – DAS + ARR
        if (pressed.contains(KeyCode.LEFT)) moveWithRepeat(now, KeyCode.LEFT, -1);
        if (pressed.contains(KeyCode.RIGHT)) moveWithRepeat(now, KeyCode.RIGHT, 1);

        // Soft drop (continuous)
        if (pressed.contains(KeyCode.DOWN)) engine.softDrop();

        // Single‑press actions
        consumeAndExecute(KeyCode.UP, engine::rotate);
        consumeAndExecute(KeyCode.ENTER, engine::hardDrop);
        consumeAndExecute(KeyCode.H, engine::hold);

        // Stop the loop when game over
        if (engine.isGameOver()) {
            gameLoop.stop();
            statusLabel.setText("GAME OVER");
        }
    }

    private void moveWithRepeat(long now, KeyCode key, int dx) {
        long press = lastPress.getOrDefault(key, 0L);
        if (press == 0) return; // key not pressed

        long last = lastMove.getOrDefault(key, 0L);

        if (last == 0) engine.move(dx, 0);
        else if (now - press > Config.DAS && now - last > Config.ARR) engine.move(dx, 0);

        lastMove.put(key, now);
    }

    private void consumeAndExecute(KeyCode key, Runnable action) {
        if (pressed.remove(key)) action.run();
    }

    @Override
    public void onBoardUpdated(Board board, Tetromino current, Tetromino next, Tetromino hold) {
        previewRenderer.render(
                nextCanvas.getGraphicsContext2D(), next, (int) nextCanvas.getWidth());
        previewRenderer.render(
                holdCanvas.getGraphicsContext2D(), hold, (int) holdCanvas.getWidth());
    }

    @Override
    public void onScoreUpdated(int score, int lines, int level) {
        scoreLabel.setText(String.valueOf(score));
        linesLabel.setText(String.valueOf(lines));
        levelLabel.setText(String.valueOf(level));
    }

    @Override
    public void onGameOver() {
        statusLabel.setText("GAME OVER");
    }
}
