package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void create_shouldSaveItem() {
        UserDto owner = userService.create(UserDto.builder()
                .name("Owner").email("owner@test.com").build());

        ItemDto created = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель").description("Мощная").available(true).build());

        assertNotNull(created.getId());
        assertEquals("Дрель", created.getName());
    }

    @Test
    void getByOwner_shouldReturnItems() {
        UserDto owner = userService.create(UserDto.builder()
                .name("Owner2").email("owner2@test.com").build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Молоток").description("Тяжёлый").available(true).build());

        List<ItemDto> items = itemService.getByOwner(owner.getId());

        assertEquals(1, items.size());
        assertEquals("Молоток", items.get(0).getName());
    }
}