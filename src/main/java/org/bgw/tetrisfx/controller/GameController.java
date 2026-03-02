package org.bgw.tetrisfx.controller;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.bgw.tetrisfx.config.Config;
import org.bgw.tetrisfx.dto.GameSnapshot;
import org.bgw.tetrisfx.model.GameState;
import org.bgw.tetrisfx.service.GameEngine;
import org.bgw.tetrisfx.view.BoardRender;
import org.bgw.tetrisfx.view.PreviewRender;

/**
 * UI controller – wires FXML nodes to the game engine.
 */
public class GameController {

    /* UI -------------------------------------------------------------*/
    @FXML private Canvas gameCanvas;
    @FXML private Canvas holdCanvas;
    @FXML private Canvas nextCanvas;
    @FXML private Label scoreLabel, linesLabel, levelLabel, statusLabel;

    /* Game engine & renderers ----------------------------------------*/
    private GameEngine engine;
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
        engine = new GameEngine(Config.BOARD_WIDTH, Config.BOARD_HEIGHT);
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
                                    KeyEvent.KEY_PRESSED,
                                    e -> {
                                        if (!pressed.contains(
                                                e.getCode())) { // prevent auto-repeat spam
                                            pressed.add(e.getCode());
                                            long now = System.nanoTime();
                                            lastPress.put(e.getCode(), now);
                                            lastMove.put(e.getCode(), now);

                                            // Immediate first move for left/right
                                            if (e.getCode() == KeyCode.LEFT) engine.moveLeft();
                                            if (e.getCode() == KeyCode.RIGHT) engine.moveRight();
                                        }
                                    });

                            scene.addEventHandler(
                                    KeyEvent.KEY_RELEASED,
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

                        int levelIndex =
                                Math.min(engine.getLevel() - 1, Config.DROP_INTERVALS.length - 1);

                        double dropInterval = Config.DROP_INTERVALS[levelIndex] / 1_000_000_000.0;
                        accumulator += deltaSec;

                        while (accumulator >= dropInterval) {
                            engine.tick();
                            accumulator -= dropInterval;
                        }

                        scoreLabel.setText(String.valueOf(engine.getScore()));
                        linesLabel.setText(String.valueOf(engine.getLines()));
                        levelLabel.setText(String.valueOf(engine.getLevel()));

                        if (engine.getState() == GameState.GAME_OVER) {
                            statusLabel.setText("GAME OVER");
                            gameLoop.stop();
                        }

                        GameSnapshot snap = engine.snapshot();
                        boardRenderer.render(
                                gameCanvas.getGraphicsContext2D(),
                                snap.board(),
                                snap.current(),
                                snap.ghost());

                        previewRenderer.render(
                                nextCanvas.getGraphicsContext2D(),
                                snap.next(),
                                (int) nextCanvas.getWidth());

                        previewRenderer.render(
                                holdCanvas.getGraphicsContext2D(),
                                snap.hold(),
                                (int) holdCanvas.getWidth());
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
        if (pressed.contains(KeyCode.LEFT)) moveWithRepeat(now, KeyCode.LEFT, engine::moveLeft);
        if (pressed.contains(KeyCode.RIGHT)) moveWithRepeat(now, KeyCode.RIGHT, engine::moveRight);

        // Soft drop (continuous)
        if (pressed.contains(KeyCode.DOWN)) engine.softDrop();

        // Single‑press actions
        consumeAndExecute(KeyCode.UP, engine::rotate);
        consumeAndExecute(KeyCode.ENTER, engine::hardDrop);
        consumeAndExecute(KeyCode.H, engine::hold);
    }

    private void moveWithRepeat(long now, KeyCode key, Runnable action) {
        Long pressTime = lastPress.get(key);
        Long lastMoveTime = lastMove.get(key);

        if (pressTime == null || lastMoveTime == null) return;

        long sincePress = now - pressTime;
        long sinceLast = now - lastMoveTime;

        if (sincePress >= Config.DAS && sinceLast >= Config.ARR) {
            action.run();
            lastMove.put(key, now);
        }
    }

    private void consumeAndExecute(KeyCode key, Runnable action) {
        if (pressed.remove(key)) action.run();
    }
}
