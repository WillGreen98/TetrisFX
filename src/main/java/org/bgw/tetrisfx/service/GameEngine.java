/* (C)2026 */
package org.bgw.tetrisfx.service;

import org.bgw.tetrisfx.model.BagRandomizer;
import org.bgw.tetrisfx.model.Board;
import org.bgw.tetrisfx.model.SRS;
import org.bgw.tetrisfx.model.Tetromino;

public class GameEngine {

    private final Board board;
    private final BagRandomizer bag = new BagRandomizer();

    private Tetromino current;
    private Tetromino next;
    private Tetromino hold;

    private boolean holdUsedThisTurn = false;

    private GameListener listener;
    private boolean gameOver = false;

    private int level = 1;
    private int score;
    private int lines;
    private int combo = -1;

    public void setListener(GameListener listener) {
        this.listener = listener;
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

    public GameEngine(int width, int height) {
        this.board = new Board(width, height);
        spawn();
    }

    public void reset() {
        board.clear();
        score = 0;
        lines = 0;
        level = 1;
        combo = -1;
        hold = null;
        next = null;
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
            return;
        }

        notifyUpdate();
    }

    private void updateLevel() {
        level = (lines / 10) + 1;
    }

    public long getDropInterval() {
        // Tetris guideline style gravity curve
        return (long) (500_000_000 / Math.sqrt(level));
    }

    private int calculateScore(int linesCleared) {
        return switch (linesCleared) {
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
            board.place(current);

            int cleared = board.clearLines();

            if (cleared > 0) {
                lines += cleared;
                combo++;

                int base = calculateScore(cleared);
                int comboBonus = combo * 50;

                score += base + comboBonus;

                updateLevel();
                notifyScore();

            } else {
                combo = -1;
            }
        }

        notifyUpdate();
    }

    public void moveLeft() {
        current.move(-1, 0);
        if (!board.isValidPosition(current)) current.move(1, 0);

        notifyUpdate();
    }

    public void moveRight() {
        current.move(1, 0);
        if (!board.isValidPosition(current)) current.move(-1, 0);

        notifyUpdate();
    }

    public void rotate() {
        if (gameOver) return;

        int oldRotation = current.getRotationIndex();
        current.rotateClockwise();

        int[][] kicks = SRS.getKicks(current.getType(), oldRotation);

        for (int[] kick : kicks) {
            current.move(kick[0], kick[1]);

            if (board.isValidPosition(current)) {
                notifyUpdate();
                return;
            }

            current.move(-kick[0], -kick[1]);
        }

        // revert if all kicks fail
        current.rotateCounterClockwise();
    }

    public void softDrop() {
        if (gameOver) return;

        current.move(0, 1);

        if (!board.isValidPosition(current)) {
            current.move(0, -1);
            board.place(current);
            spawn();
            return;
        }

        notifyUpdate();
    }

    public void hardDrop() {
        if (gameOver) return;

        while (true) {
            current.move(0, 1);
            if (!board.isValidPosition(current)) {
                current.move(0, -1);
                break;
            }
        }

        board.place(current);

        int cleared = board.clearLines();
        lines += cleared;

        int base = calculateScore(cleared);
        score += base;
        updateLevel();
        notifyScore();

        spawn();

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

        newCurrent.setPosition(3, 0);

        if (!board.isValidPosition(newCurrent)) {
            gameOver = true;
            notifyGameOver();
            return;
        }

        current = newCurrent;
        holdUsedThisTurn = true;

        notifyUpdate();
    }

    public Tetromino getGhost() {
        // TODO: improve this overhead
        //        Per-frame object allocation
        //        Avoidable
        //        Better:
        //        Maintain cached ghost inside engine
        //        Update only when piece moves
        //        Micro-optimization but good practice.
        Tetromino ghost = new Tetromino(current.getType());
        ghost.setPosition(current.getX(), current.getY());

        while (ghost.getRotationIndex() != current.getRotationIndex()) {
            ghost.rotateClockwise();
        }

        while (board.isValidPosition(ghost)) {
            ghost.move(0, 1);
        }

        ghost.move(0, -1);

        return ghost;
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrent() {
        return current;
    }
}
