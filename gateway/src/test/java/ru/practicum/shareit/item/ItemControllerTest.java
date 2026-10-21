package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_shouldReturn200() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .build();

        when(itemClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная")
                .available(false)
                .build();

        when(itemClient.update(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Дрель").build();

        when(itemClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/items/1").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getByOwner_shouldReturn200() throws Exception {
        when(itemClient.getByOwner(anyLong())).thenReturn(ResponseEntity.ok(new Object[]{}));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void search_shouldReturn200() throws Exception {
        when(itemClient.search("дрель")).thenReturn(ResponseEntity.ok(new Object[]{}));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldReturn200() throws Exception {
        CommentRequestDto dto = CommentRequestDto.builder().text("Отлично").build();

        when(itemClient.addComment(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}