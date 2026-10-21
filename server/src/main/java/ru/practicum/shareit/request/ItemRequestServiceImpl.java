package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
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
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final Sort SORT_DESC_BY_CREATED = Sort.by(Sort.Direction.DESC, "created");
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        log.info("Создание запроса пользователем id = {}: {}", userId, dto);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequest saved = requestRepository.save(request);
        log.info("Запрос создан: id = {}", saved.getId());
        return ItemRequestMapper.toDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        log.info("Запрос собственных запросов пользователя id = {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return requestRepository.findByRequestorId(userId, SORT_DESC_BY_CREATED).stream()
                .map(r -> ItemRequestMapper.toDto(r, getItemsForRequest(r.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        log.info("Запрос чужих запросов пользователем id = {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return requestRepository.findByRequestorIdNot(userId, SORT_DESC_BY_CREATED).stream()
                .map(r -> ItemRequestMapper.toDto(r, getItemsForRequest(r.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Запрос информации о запросе id = {} пользователем id = {}", requestId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return ItemRequestMapper.toDto(request, getItemsForRequest(requestId));
    }

    private List<ItemShortDto> getItemsForRequest(Long requestId) {
        return itemRepository.findByRequestId(requestId).stream()
                .map(item -> ItemShortDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwner().getId())
                        .build())
                .collect(Collectors.toList());
    }
}