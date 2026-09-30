package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем id = {}: {}", userId, itemDto);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности обязателен");
        }

        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();

        Item saved = itemRepository.save(item);
        log.info("Вещь создана: id = {}", saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи id = {} пользователем id = {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь id = {} не владелец вещи id = {}", userId, itemId);
            throw new NotFoundException("Редактировать может только владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item saved = itemRepository.save(item);
        log.info("Вещь id = {} обновлена", saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        log.info("Запрос вещи id = {} пользователем id = {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto dto = ItemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            dto.setLastBooking(getLastBooking(itemId, now));
            dto.setNextBooking(getNextBooking(itemId, now));
        }

        dto.setComments(getComments(itemId));
        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(Long userId) {
        log.info("Запрос вещей владельца id = {}", userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    dto.setLastBooking(getLastBooking(item.getId(), now));
                    dto.setNextBooking(getNextBooking(item.getId(), now));
                    dto.setComments(getComments(item.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto dto) {
        log.info("Добавление комментария к вещи id = {} пользователем id = {}", itemId, userId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (dto.getText() == null || dto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        List<Booking> pastBookings = bookingRepository
                .findByItemIdAndBookerIdAndEndIsBefore(itemId, userId, LocalDateTime.now());

        if (pastBookings.isEmpty()) {
            log.warn("Пользователь id = {} не брал вещь id = {} в аренду", userId, itemId);
            throw new ValidationException("Вы не брали эту вещь в аренду");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Комментарий создан: id = {}", saved.getId());
        return CommentMapper.toDto(saved);
    }

    private LocalDateTime getLastBooking(Long itemId, LocalDateTime now) {
        List<Booking> bookings = bookingRepository.findLastBookingForItem(itemId, now);
        return bookings.isEmpty() ? null : bookings.get(0).getEnd();
    }

    private LocalDateTime getNextBooking(Long itemId, LocalDateTime now) {
        List<Booking> bookings = bookingRepository.findNextBookingForItem(itemId, now);
        return bookings.isEmpty() ? null : bookings.get(0).getStart();
    }

    private List<CommentDto> getComments(Long itemId) {
        return commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }
}