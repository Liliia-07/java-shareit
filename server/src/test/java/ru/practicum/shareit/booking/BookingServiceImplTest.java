package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    @Test
    void create_shouldSaveBooking() {
        UserDto owner = userService.create(UserDto.builder()
                .name("OwnerB").email("ownerb@test.com").build());
        UserDto booker = userService.create(UserDto.builder()
                .name("BookerB").email("bookerb@test.com").build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("ДрельB").description("МощнаяB").available(true).build());

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto booking = bookingService.create(booker.getId(), dto);

        assertNotNull(booking.getId());
        assertEquals("WAITING", booking.getStatus());
    }
}