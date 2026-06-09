package org.jaubs.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SoldItem(BookItem item, String buyer, LocalDate date) {
}
