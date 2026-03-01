package org.bgw.tetrisfx.service;

import org.bgw.tetrisfx.model.BagRandomizer;
import org.bgw.tetrisfx.model.Board;
import org.bgw.tetrisfx.model.SRS;
import org.bgw.tetrisfx.model.Tetromino;

public class GameEngine {

    /* Core state -----------------------------------------------------*/
    private final Board board;
    private final BagRandomizer bag = new BagRandomizer();

    private Tetromino current, next, hold;
    private boolean holdUsedThisTurn = false;

    /* Listeners -----------------------------------------------------*/
    private GameListener listener;
    private boolean gameOver = false;

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

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getLevel() {
        return level;
    }

    private void notifyUpdate() {
        if (listener != null) listener.onBoardUpdated(board, current, next, hold);
    }

    private void notifyScore() {
        if (listener != null) listener.onScoreUpdated(score, lines, level);
    }

    private void notifyGameOver() {
        if (listener != null) listener.onGameOver();
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
        gameOver = false;

        bag.clear();
        spawn();
        notifyScore();
    }

    private void spawn() {
        holdUsedThisTurn = false;

        if (next == null) next = new Tetromino(bag.next());
        current = next;
        next = new Tetromino(bag.next());

        current.setPosition(board.getWidth() / 2, 0);
        if (!board.isValidPosition(current)) {
            gameOver = true;
            notifyGameOver();
        } else {
            ghostValid = false; // needs recompute
            notifyUpdate();
        }
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
        if (gameOver) return;

        current.move(0, 1);

        if (!board.isValidPosition(current)) {
            current.move(0, -1);
            lockPiece();
        } else {
            ghostValid = false;
        }

        notifyUpdate();
    }

    public void move(int dx, int dy) {
        current.move(dx, dy);
        if (!board.isValidPosition(current)) current.move(-dx, -dy);
        ghostValid = false;
        notifyUpdate();
    }

    private void lockPiece() {
        board.place(current);

        int cleared = board.clearLines();

        if (cleared > 0) {
            lines += cleared;
            combo = Math.max(combo + 1, 0);

            score += scoreForLines(cleared) + combo * 50;
            updateLevel();
            notifyScore();
        } else {
            combo = -1;
        }

        spawn();
    }

    public void rotate() {
        if (gameOver) return;
        int oldRot = current.getRotationIndex();
        current.rotateClockwise();

        int[][] kicks = SRS.getKicks(current.getType(), oldRot);
        for (int[] k : kicks) {
            current.move(k[0], k[1]);
            if (board.isValidPosition(current)) {
                ghostValid = false;
                notifyUpdate();
                return;
            }
            current.move(-k[0], -k[1]);
        }

        // No valid kick
        current.rotateCounterClockwise();
    }

    public void softDrop() {
        if (gameOver) return;

        current.move(0, 1);

        if (!board.isValidPosition(current)) {
            current.move(0, -1);
            lockPiece();
        } else {
            ghostValid = false;
            score += 1; // optional soft drop reward
            notifyScore();
        }

        notifyUpdate();
    }

    public void hardDrop() {
        if (gameOver) return;

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
        notifyScore();

        lockPiece();
        ghostValid = false;

        notifyUpdate();
    }

    public void hold() {
        if (gameOver || holdUsedThisTurn) return;

        Tetromino newCurrent;
        if (hold == null) {
            hold = new Tetromino(current.getType());
            newCurrent = new Tetromino(next.getType());
            next = new Tetromino(bag.next());
        } else {
            newCurrent = new Tetromino(hold.getType());
            hold = new Tetromino(current.getType());
        }

        newCurrent.setPosition(board.getWidth() / 2, 0);
        if (!board.isValidPosition(newCurrent)) {
            gameOver = true;
            notifyGameOver();
        } else {
            current = newCurrent;
            holdUsedThisTurn = true;
            ghostValid = false;
        }
        notifyUpdate();
    }

    public Tetromino getGhost() {
        if (ghostValid) return cachedGhost;

        ghostValid = true;

        Tetromino g = new Tetromino(current.getType());
        g.setPosition(current.getX(), current.getY());

        while (g.getRotationIndex() != current.getRotationIndex()) g.rotateClockwise();
        while (board.isValidPosition(g)) g.move(0, 1);

        g.move(0, -1);
        cachedGhost = g;

        return g;
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrent() {
        return current;
    }
}
