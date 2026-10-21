package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void create_shouldSaveUser() {
        UserDto dto = UserDto.builder()
                .name("Test")
                .email("test@test.com")
                .build();

        UserDto created = userService.create(dto);

        assertNotNull(created.getId());
        assertEquals("Test", created.getName());
        assertEquals("test@test.com", created.getEmail());
    }

    @Test
    void getById_shouldReturnUser() {
        UserDto created = userService.create(UserDto.builder()
                .name("Test").email("test@test.com").build());

        UserDto found = userService.getById(created.getId());

        assertEquals(created.getId(), found.getId());
    }
}