package com.ruchij.api.web.responses;

import java.util.Collection;

public record PaginatedResponse<T>(Collection<T> data, int offset, int limit) {
}
