/* (C)2026 */
package org.bgw.tetrisfx.model;

import java.util.*;

public class BagRandomizer {
    private final Queue<PieceType> bag = new ArrayDeque<>();

    public PieceType next() {
        if (bag.isEmpty()) {
            refill();
        }

        PieceType piece = bag.poll();

        if (piece == null) {
            throw new IllegalStateException("BagRandomizer failed to provide piece");
        }

        return piece;
    }

    public void clear() {
        if (bag.isEmpty()) return;

        bag.clear();
    }

    private void refill() {
        List<PieceType> pieces = new ArrayList<>(List.of(PieceType.values()));
        Collections.shuffle(pieces);
        bag.addAll(pieces);
    }
}
