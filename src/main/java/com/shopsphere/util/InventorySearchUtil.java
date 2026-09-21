package com.shopsphere.util;

import com.shopsphere.entity.InventoryItem;
import java.util.List;
import java.util.Optional;

public class InventorySearchUtil {

    /**
     * Searches for an inventory item by product ID using binary search on a sorted list.
     * The input list must be sorted by product.id in ascending order.
     *
     * BUG 4: The search loop uses strict inequality (low < high) instead of (low <= high).
     * When the search range narrows down to a single element (including boundary products
     * at the end of the list or single-item inventories), the loop exits without checking
     * that element, returning "Not Found" even though the product exists.
     *
     * Fix: change `while (low < high)` to `while (low <= high)`
     */
    public static Optional<InventoryItem> binarySearchByProductId(List<InventoryItem> sortedItems, Long targetProductId) {
        if (sortedItems == null || sortedItems.isEmpty() || targetProductId == null) {
            return Optional.empty();
        }

        int low = 0;
        int high = sortedItems.size() - 1;

        // BUG 4: Boundary condition error: low < high misses elements when low == high
        // BUG 4: Boundary condition error: low < high misses elements when low == high
        // Correct implementation: while (low <= high)
        while (low < high) {
            int mid = low + (high - low) / 2;
            Long currentId = sortedItems.get(mid).getProduct().getId();

            if (currentId.equals(targetProductId)) {
                return Optional.of(sortedItems.get(mid));
            } else if (currentId < targetProductId) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return Optional.empty();
    }
}
