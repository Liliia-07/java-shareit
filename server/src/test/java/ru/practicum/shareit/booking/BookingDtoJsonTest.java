package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testSerializeDates() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 10, 21, 10, 0, 0))
                .end(LocalDateTime.of(2026, 10, 22, 10, 0, 0))
                .status("WAITING")
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-10-21T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-10-22T10:00:00");
    }
}