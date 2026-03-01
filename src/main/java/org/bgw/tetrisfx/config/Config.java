package org.bgw.tetrisfx.config;

import java.util.stream.IntStream;

/**
 * All numeric constants that drive the game.
 */
public final class Config {
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    public static final int CELL_SIZE = 30;

    /* Input timing ----------------------------------------------------*/
    /** Delay before a held key starts repeating (DAS). */
    public static final long DAS = 150_000_000L; // 150ms

    /** Repeat interval after DAS expires (ARR). */
    public static final long ARR = 40_000_000L; // 40ms

    /* Gravity --------------------------------------------------------*/
    /** Pre‑computed drop interval per level (nanoseconds). */
    public static final long[] DROP_INTERVALS =
            IntStream.rangeClosed(1, 20)
                    .mapToLong(l -> (long) (500_000_000L / Math.sqrt(l)))
                    .toArray();

    private Config() {}
}
