package com.practice.shareitprojectalena.request.service;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.error.exceptions.ValidationException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.request.ItemRequestMapper;
import com.practice.shareitprojectalena.request.ItemRequestRepository;
import com.practice.shareitprojectalena.request.dto.ItemRequestCreateDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestFullDto;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;

    public ItemRequestDto create(Long userId, ItemRequestCreateDto itemRequestCreateDto) {
        User user = userService.findById(userId);
        ItemRequest itemRequest = new ItemRequest();
        if (itemRequestCreateDto.getDescription() == null || itemRequestCreateDto.getDescription().isEmpty()) {
            throw new ValidationException("Ошибка валидации: описание должно быть заполнено");
        }

        itemRequest.setDescription(itemRequestCreateDto.getDescription());
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }


    public List<ItemRequestFullDto> getAllRequestsByUserId(Long userId) {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequestFullDto> itemRequestFullDtoList = new ArrayList<>();

        for (ItemRequest itemRequest : itemRequests) {
            List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
            ItemRequestFullDto fullDto = itemRequestMapper.toItemRequestFullDto(itemRequest);
            fullDto.setItems(items);
            itemRequestFullDtoList.add(fullDto);
        }
        return itemRequestFullDtoList;
    }

    public List<ItemRequestDto> getAllRequests(int from, int size, Long userId) {
        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequesterId(userId, pageable).getContent();
        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestDto)
                .toList();
    }

    public ItemRequestFullDto  getAllByRequestId(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        List<Item> items = itemRepository.findByRequestId(requestId);
        ItemRequestFullDto itemRequestFullDto = itemRequestMapper.toItemRequestFullDto(itemRequest);
        itemRequestFullDto.setItems(items);
        return itemRequestFullDto;
    }
}