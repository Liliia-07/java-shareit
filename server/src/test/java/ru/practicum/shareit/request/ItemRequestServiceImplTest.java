package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private UserService userService;

    @Test
    void create_shouldSaveRequest() {
        UserDto user = userService.create(UserDto.builder()
                .name("UserR").email("userr@test.com").build());

        ItemRequestDto created = requestService.create(user.getId(),
                ItemRequestCreateDto.builder().description("Ищу дрель").build());

        assertNotNull(created.getId());
        assertEquals("Ищу дрель", created.getDescription());
    }
}