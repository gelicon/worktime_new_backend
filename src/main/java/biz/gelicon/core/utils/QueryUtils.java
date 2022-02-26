package biz.gelicon.core.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class QueryUtils {

    public static final int DEFAULT_PAGE_SIZE = 10; // Рзамер страницы в строках по умолчанию
    public static final int UNLIMIT_PAGE_SIZE = Integer.MAX_VALUE;

    public static Pageable allPagesAndSort(List<Sort.Order> orders) {
        return PageRequest.of(0, UNLIMIT_PAGE_SIZE,Sort.by(orders));
    }

    public static Pageable allPagesAndSort(String field) {
        return PageRequest.of(0, UNLIMIT_PAGE_SIZE,Sort.by(field));
    }

    public static Pageable allPagesAndSort(String field,Sort.Direction direction) {
        return PageRequest.of(0, UNLIMIT_PAGE_SIZE,Sort.by(direction, field));
    }

    public static Pageable defaultPageAndSort(String field) {
        return PageRequest.of(0, DEFAULT_PAGE_SIZE,Sort.by(field));
    }

    public static Pageable defaultAndSort(String field,Sort.Direction direction) {
        return PageRequest.of(0, UNLIMIT_PAGE_SIZE,Sort.by(direction, field));
    }
}
