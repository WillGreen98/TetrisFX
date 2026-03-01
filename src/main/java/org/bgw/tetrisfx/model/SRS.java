/* (C)2026 */
package org.bgw.tetrisfx.model;

public class SRS {

    // JLSTZ kicks
    private static final int[][][] NORMAL_KICKS = {
        {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}}, // 0->R
        {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}}, // R->2
        {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}}, // 2->L
        {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}} // L->0
    };

    // I piece kicks
    private static final int[][][] I_KICKS = {
        {{0, 0}, {-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
        {{0, 0}, {-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
        {{0, 0}, {2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
        {{0, 0}, {1, 0}, {-2, 0}, {1, -2}, {-2, 1}}
    };

    public static int[][] getKicks(PieceType type, int rotationIndex) {
        if (type == PieceType.I) return I_KICKS[rotationIndex];
        return NORMAL_KICKS[rotationIndex];
    }
}
