package com.practice.shareitserver.request;

import com.practice.common.dto.ItemRequestFullDto;
import com.practice.common.dto.ItemRequestFullResponseDto;
import com.practice.common.dto.ItemShortDto;
import com.practice.common.mapper.RequestFullDtoMapper;
import com.practice.shareitserver.error.exceptions.NotFoundException;
import com.practice.shareitserver.error.exceptions.ValidationException;
import com.practice.shareitserver.item.ItemRepository;
import com.practice.shareitserver.request.dto.ItemRequestCreateDto;
import com.practice.shareitserver.request.dto.ItemRequestDto;
import com.practice.shareitserver.request.entity.ItemRequest;
import com.practice.shareitserver.user.UserService;
import com.practice.shareitserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;

    private final RequestFullDtoMapper requestFullDtoMapper = new RequestFullDtoMapper();

    public ItemRequestDto create(Long userId, ItemRequestCreateDto itemRequestCreateDto) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new NotFoundException("Объект не найден");
        }
        if (itemRequestCreateDto.getDescription().isBlank()) {
            throw new ValidationException("Описание должно быть заполнено");
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestCreateDto.getDescription());
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }


    public List<ItemRequestFullResponseDto> getAllRequestsByUserId(Long userId) {

        User user = userService.findById(userId);
        if (user == null) {
            throw new NotFoundException("Объект не найден");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId,sort).stream()
                .map(itemRequest -> {
                    List<ItemShortDto> items = itemRepository.findByRequestId(itemRequest.getId())
                            .stream()
                            .map(item -> new ItemShortDto(item.getId(), item.getName(), item.getDescription()))
                            .collect(Collectors.toList());

                 ItemRequestFullDto fullDto = new ItemRequestFullDto(
                            itemRequest.getId(),
                            itemRequest.getDescription(),
                            itemRequest.getRequester().getId(),
                            itemRequest.getCreated(),
                            items
                    );
                    return requestFullDtoMapper.mapToResponseDto(fullDto);

                })
                .collect(Collectors.toList()); }

    public List<ItemRequestDto> getAllRequests(int from, int size, Long userId) {
        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequesterId(userId, pageable).getContent();

        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    public ItemRequestFullResponseDto getAllByRequestId(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<ItemShortDto> items = itemRepository.findByRequestId(requestId)
                .stream()
                .map(item -> new ItemShortDto(item.getId(), item.getName(), item.getDescription()))
                .collect(Collectors.toList());

        ItemRequestFullDto fullDto = new ItemRequestFullDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequester().getId(),
                itemRequest.getCreated(),
                items
        );

        return requestFullDtoMapper.mapToResponseDto(fullDto);
    }


}
