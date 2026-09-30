package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_DESC_BY_START = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingRequestDto dto) {
        log.info("Создание бронирования пользователем id = {}: {}", userId, dto);
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (dto.getEnd() == null || dto.getStart() == null || !dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Некорректные даты бронирования");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование создано: id = {}", saved.getId());
        return BookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение бронирования id = {} пользователем id = {}, approved = {}",
                bookingId, userId, approved);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь id = {} не владелец вещи id = {}", userId, booking.getItem().getId());
            throw new ValidationException("Подтвердить может только владелец вещи");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование id = {} переведено в статус {}", saved.getId(), saved.getStatus());
        return BookingMapper.toDto(saved);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        log.info("Запрос бронирования id = {} пользователем id = {}", bookingId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступа");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, BookingStatus state) {
        log.info("Запрос бронирований пользователя id = {}, state = {}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings = findByStateAndBooker(userId, state);
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, BookingStatus state) {
        log.info("Запрос бронирований владельца id = {}, state = {}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings = findByStateAndOwner(userId, state);
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    private List<Booking> findByStateAndBooker(Long userId, BookingStatus state) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                        userId, now, now, SORT_DESC_BY_START);
            case PAST:
                return bookingRepository.findByBookerIdAndEndIsBefore(userId, now, SORT_DESC_BY_START);
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartIsAfter(userId, now, SORT_DESC_BY_START);
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, SORT_DESC_BY_START);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, SORT_DESC_BY_START);
            default:
                return bookingRepository.findByBookerId(userId, SORT_DESC_BY_START);
        }
    }

    private List<Booking> findByStateAndOwner(Long userId, BookingStatus state) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                        userId, now, now, SORT_DESC_BY_START);
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, now, SORT_DESC_BY_START);
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, now, SORT_DESC_BY_START);
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, SORT_DESC_BY_START);
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, SORT_DESC_BY_START);
            default:
                return bookingRepository.findByItemOwnerId(userId, SORT_DESC_BY_START);
        }
    }
}