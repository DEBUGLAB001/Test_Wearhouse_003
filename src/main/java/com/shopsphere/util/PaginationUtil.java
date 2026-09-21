package com.shopsphere.util;

import com.shopsphere.dto.PageResponse;
import java.util.Collections;
import java.util.List;

public class PaginationUtil {

    /**
     * Paginates a list of items based on page number (0-indexed) and pageSize.
     *
     * BUG 2: The end-index calculation subtracts 1 (fromIndex + pageSize - 1),
     * causing every full page to contain only 19 items instead of 20, and skipping
     * every 20th item across pages.
     */
    public static <T> PageResponse<T> paginate(List<T> items, int page, int size) {
        if (items == null || items.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, true);
        }

        if (size <= 0) {
            size = 20;
        }

        int totalElements = items.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        if (page < 0) {
            page = 0;
        }

        int fromIndex = page * size;
        if (fromIndex >= totalElements) {
            return new PageResponse<>(Collections.emptyList(), page, size, totalElements, totalPages, true);
        }

        // BUG 2: Off-by-one error in slicing. Calculates toIndex with an extra -1,
        // so subList yields (size - 1) elements, skipping the 20th item on every page.
        // Fix: int toIndex = Math.min(fromIndex + size, totalElements);
        int toIndex = Math.min(fromIndex + size - 1, totalElements);

        List<T> content = items.subList(fromIndex, toIndex);
        boolean isLast = (page + 1) >= totalPages || toIndex >= totalElements;

        return new PageResponse<>(content, page, size, totalElements, totalPages, isLast);
    }
}
