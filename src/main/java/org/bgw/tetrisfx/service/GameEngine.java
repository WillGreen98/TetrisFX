package org.bgw.tetrisfx.service;

import org.bgw.tetrisfx.dto.GameSnapshot;
import org.bgw.tetrisfx.model.*;

public class GameEngine {

    /* Core state -----------------------------------------------------*/
    private final Board board;
    private final BagRandomizer bag = new BagRandomizer();

    private Tetromino current;
    private PieceType next;
    private PieceType hold;
    private boolean holdUsedThisTurn = false;

    /* State ----------------------------------------------------------*/
    private GameState state = GameState.RUNNING;

    /* Score / level --------------------------------------------------*/
    private int level = 1;
    private int score, lines, combo = -1;

    /* Ghost cache ---------------------------------------------------*/
    private Tetromino cachedGhost;
    private boolean ghostValid = false;

    public GameEngine(int width, int height) {
        this.board = new Board(width, height);
        spawn();
    }

    public int getLevel() {
        return level;
    }

    public void reset() {
        board.clear();
        score = 0;
        lines = 0;
        level = 1;
        combo = -1;

        hold = null;
        next = null;
        current = null;
        holdUsedThisTurn = false;

        state = GameState.RUNNING;

        bag.clear();
        spawn();
    }

    private void spawn() {
        if (next == null) next = bag.next();
        if (next == null) throw new IllegalStateException("Next piece is null");

        holdUsedThisTurn = false;

        current = new Tetromino(next);
        next = bag.next();

        int spawnX = (board.getWidth() - current.getShape()[0].length) / 2;
        current.setPosition(spawnX, 0);

        if (!board.isValidPosition(current)) state = GameState.GAME_OVER;
        else ghostValid = false;
    }

    private void updateLevel() {
        level = (lines / 10) + 1;
    }

    private int scoreForLines(int cleared) {
        return switch (cleared) {
            case 1 -> 100 * level;
            case 2 -> 300 * level;
            case 3 -> 500 * level;
            case 4 -> 800 * level; // Tetris
            default -> 0;
        };
    }

    public void tick() {
        if (state != GameState.RUNNING) return;

        current.move(0, 1);

        if (!board.isValidPosition(current)) {
            current.move(0, -1);
            lockPiece();
        } else {
            ghostValid = false;
        }
    }

    void move(int dx) {
        current.move(dx, 0);
        if (!board.isValidPosition(current)) current.move(-dx, -0);
        ghostValid = false;
    }

    public void moveLeft() {
        move(-1);
    }

    public void moveRight() {
        move(1);
    }

    private void lockPiece() {
        board.place(current);

        int cleared = board.clearLines();

        if (cleared > 0) {
            lines += cleared;
            combo = Math.max(combo + 1, 0);

            score += scoreForLines(cleared) + combo * 50;
            updateLevel();
        } else {
            combo = -1;
        }

        spawn();
    }

    public void rotate() {
        if (state == GameState.GAME_OVER) return;
        int oldRot = current.getRotationIndex();
        current.rotateClockwise();

        int[][] kicks = SRS.getKicks(current.getType(), oldRot);
        for (int[] k : kicks) {
            current.move(k[0], k[1]);
            if (board.isValidPosition(current)) {
                ghostValid = false;
                return;
            }
            current.move(-k[0], -k[1]);
        }

        // No valid kick
        current.rotateCounterClockwise();
    }

    public void softDrop() {
        if (state == GameState.GAME_OVER) return;

        current.move(0, 1);

        if (!board.isValidPosition(current)) {
            current.move(0, -1);
            lockPiece();
        } else {
            ghostValid = false;
            score += 1; // optional soft drop reward
        }
    }

    public void hardDrop() {
        if (state == GameState.GAME_OVER) return;

        int distance = 0;

        while (true) {
            current.move(0, 1);
            if (!board.isValidPosition(current)) {
                current.move(0, -1);
                break;
            }
            distance++;
        }

        score += distance * 2; // reward hard drop distance

        lockPiece();
        ghostValid = false;
    }

    public void hold() {
        if (state == GameState.GAME_OVER || holdUsedThisTurn) return;

        PieceType newCurrentType;

        if (hold == null) {
            hold = current.getType();
            newCurrentType = next;
            next = bag.next();
        } else {
            newCurrentType = hold;
            hold = current.getType();
        }

        Tetromino newCurrent = new Tetromino(newCurrentType);

        int spawnX = (board.getWidth() - newCurrent.getShape()[0].length) / 2;
        newCurrent.setPosition(spawnX, 0);

        if (!board.isValidPosition(newCurrent)) {
            state = GameState.GAME_OVER;
        } else {
            current = newCurrent;
            holdUsedThisTurn = true;
            ghostValid = false;
        }
    }

    public Tetromino getGhost() {
        if (ghostValid) return cachedGhost;

        ghostValid = true;

        Tetromino g =
                new Tetromino(
                        current.getType(),
                        current.getRotationIndex(),
                        current.getX(),
                        current.getY());

        while (board.isValidPosition(g)) {
            g.move(0, 1);
        }

        g.move(0, -1);
        cachedGhost = g;

        return g;
    }

    private PieceType[][] copyGrid() {
        PieceType[][] src = new PieceType[board.getHeight()][board.getWidth()];
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                src[y][x] = board.getCell(x, y);
            }
        }
        return src;
    }

    private Tetromino copy(Tetromino t) {
        if (t == null) return null;

        return new Tetromino(t.getType(), t.getRotationIndex(), t.getX(), t.getY());
    }

    public GameSnapshot snapshot() {
        return new GameSnapshot(
                copyGrid(),
                copy(current),
                copy(getGhost()),
                next == null ? null : new Tetromino(next),
                hold == null ? null : new Tetromino(hold),
                score,
                lines,
                level,
                state);
    }

    public int getScore() {
        return score;
    }

    public int getLines() {
        return lines;
    }

    public GameState getState() {
        return state;
    }

    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        }
    }

    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    public boolean isRunning() {
        return state == GameState.RUNNING;
    }

    public boolean isPaused() {
        return state == GameState.PAUSED;
    }
}
