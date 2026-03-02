package org.bgw.tetrisfx.dto;

import org.bgw.tetrisfx.model.GameState;
import org.bgw.tetrisfx.model.PieceType;
import org.bgw.tetrisfx.model.Tetromino;

public record GameSnapshot(
        PieceType[][] board,
        Tetromino current,
        Tetromino ghost,
        Tetromino next,
        Tetromino hold,
        int score,
        int lines,
        int level,
        GameState state) {}
