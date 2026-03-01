/* (C)2026 */
package org.bgw.tetrisfx.model;

import java.util.*;

public class BagRandomizer {
    private final Queue<PieceType> bag = new ArrayDeque<>();

    public PieceType next() {
        if (bag.isEmpty()) refill();

        return bag.poll();
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
