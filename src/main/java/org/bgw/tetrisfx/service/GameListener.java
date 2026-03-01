/* (C)2026 */
package org.bgw.tetrisfx.service;

import org.bgw.tetrisfx.model.Board;
import org.bgw.tetrisfx.model.Tetromino;

public interface GameListener {

    void onBoardUpdated(Board board, Tetromino current, Tetromino next, Tetromino hold);

    void onScoreUpdated(int score, int lines, int level);

    void onGameOver();
}
