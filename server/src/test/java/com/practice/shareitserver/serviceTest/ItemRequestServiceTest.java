package com.practice.shareitserver.serviceTest;

import com.practice.common.dto.ItemRequestFullDto;
import com.practice.common.dto.ItemRequestFullResponseDto;
import com.practice.common.dto.ItemShortDto;
import com.practice.common.mapper.RequestFullDtoMapper;
import com.practice.shareitserver.error.exceptions.NotFoundException;
import com.practice.shareitserver.error.exceptions.ValidationException;
import com.practice.shareitserver.item.Item;
import com.practice.shareitserver.item.ItemRepository;
import com.practice.shareitserver.request.ItemRequestMapper;
import com.practice.shareitserver.request.ItemRequestRepository;
import com.practice.shareitserver.request.ItemRequestService;
import com.practice.shareitserver.request.dto.ItemRequestCreateDto;
import com.practice.shareitserver.request.dto.ItemRequestDto;
import com.practice.shareitserver.request.entity.ItemRequest;
import com.practice.shareitserver.user.UserService;
import com.practice.shareitserver.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    ItemRequestService itemRequestService;

    private User user;
    private ItemRequestCreateDto itemRequestCreateDto;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private final Long userId = 1L;

    private RequestFullDtoMapper requestFullDtoMapper = new RequestFullDtoMapper();

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(userId);

        itemRequestCreateDto = new ItemRequestCreateDto("Возьму попользоваться электросамокат");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription(itemRequestCreateDto.getDescription());
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setRequesterId(user.getId());
        itemRequestDto.setCreated(itemRequest.getCreated());
    }

    @Test
    public void create_ThrowExceptionWhenDescriptionIsEmpty() {
        itemRequestCreateDto.setDescription("");

        when(userService.findById(1L))
                .thenReturn(user);
        Exception exception = assertThrows(ValidationException.class,
                () -> {
                    itemRequestService.create(1L, itemRequestCreateDto);
                });

        assertEquals("Описание должно быть заполнено", exception.getMessage());
    }


    @Test
    public void create_ReturnItemRequestDtoWhenUserExists() {
        when(userService.findById(1L))
                .thenReturn(user);
        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);
        when(itemRequestMapper.toItemRequestDto(itemRequest))
                .thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.create(1L, itemRequestCreateDto);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
    }

    @Test
    public void create_ThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userService.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> {
                    itemRequestService.create(1L, itemRequestCreateDto);
                });

        assertEquals("Объект не найден", exception.getMessage());
    }

    @Test
    void getAllRequestsByUserId_ThrowUserNotFoundExceptionIfUserDoesNotExist() {
        when(userService.findById(1L))
                .thenReturn(null);

        Exception exception = assertThrows(NotFoundException.class,
                () -> {
                    itemRequestService.getAllRequestsByUserId(1L);
                });

        assertEquals("Объект не найден", exception.getMessage());
    }

    @Test
    void getAllRequestsByUserId_WhenUserExists() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request1");
        request1.setCreated(LocalDateTime.now().minusDays(1));
        request1.setRequester(user);

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request2");
        request2.setCreated(LocalDateTime.now());
        request2.setRequester(user);

        List<ItemRequest> requests = List.of(request2, request1);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item1-Request1");
        item1.setRequest(request1);
        List<Item> itemsForRequest1 = List.of(item1);


        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item2-Reques2");
        item2.setRequest(request2);
        List<Item> itemsForRequest2 = List.of(item2);

        List<ItemShortDto> itemShortDtosForRequest1 = itemsForRequest1.stream()
                .map(item -> new ItemShortDto(item.getId(), item.getName(), item.getDescription()))
                .toList();

        List<ItemShortDto> itemShortDtosForRequest2 = itemsForRequest2.stream()
                .map(item -> new ItemShortDto(item.getId(), item.getName(), item.getDescription()))
                .toList();

        when(userService.findById(userId)).thenReturn(user);
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(eq(userId), any(Sort.class)))
                .thenReturn(requests);
        when(itemRepository.findByRequestId(request1.getId())).thenReturn(itemsForRequest1);
        when(itemRepository.findByRequestId(request2.getId())).thenReturn(itemsForRequest2);

        ItemRequestFullDto dto1 = new ItemRequestFullDto();
        dto1.setId(request1.getId());
        dto1.setDescription(request1.getDescription());
        dto1.setCreated(request1.getCreated());
        dto1.setItems(itemShortDtosForRequest1);

        ItemRequestFullDto dto2 = new ItemRequestFullDto();
        dto2.setId(request2.getId());
        dto2.setDescription(request2.getDescription());
        dto2.setCreated(request2.getCreated());
        dto2.setItems(itemShortDtosForRequest2);

        when(itemRequestMapper.toItemRequestFullDto(request1)).thenReturn(dto1);
        when(itemRequestMapper.toItemRequestFullDto(request2)).thenReturn(dto2);

        ItemRequestFullResponseDto responseDto1 = new ItemRequestFullResponseDto(
                dto1.getId(), dto1.getDescription(), dto1.getRequesterId(), dto1.getCreated(), dto1.getItems()
        );

        ItemRequestFullResponseDto responseDto2 = new ItemRequestFullResponseDto(
                dto2.getId(), dto2.getDescription(), dto2.getRequesterId(), dto2.getCreated(), dto2.getItems()
        );

        when(requestFullDtoMapper.mapToResponseDto(dto1)).thenReturn(responseDto1);
        when(requestFullDtoMapper.mapToResponseDto(dto2)).thenReturn(responseDto2);


        List<ItemRequestFullResponseDto> result = itemRequestService.getAllRequestsByUserId(userId);

        assertEquals(2, result.size());
   }


    @Test
    void getAllRequestsByUserId_noRequestsFoundReturnsEmptyList() {
        when(userService.findById(userId)).thenReturn(user);
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(eq(userId), any(Sort.class)))
                .thenReturn(Collections.emptyList());
        List<ItemRequestFullResponseDto> result = itemRequestService.getAllRequestsByUserId(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequestsByUserId_returnsRequestsWithEmptyItems() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request1");
        request1.setCreated(LocalDateTime.now());
        request1.setRequester(user);

        List<ItemRequest> requests = List.of(request1);

        when(userService.findById(userId)).thenReturn(user);
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(eq(userId), any(Sort.class)))
                .thenReturn(requests);
        when(itemRepository.findByRequestId(request1.getId())).thenReturn(Collections.emptyList());

        ItemRequestFullDto dto1 = new ItemRequestFullDto();
        dto1.setId(request1.getId());
        dto1.setDescription(request1.getDescription());
        dto1.setCreated(request1.getCreated());
        dto1.setItems(Collections.emptyList());
        when(itemRequestMapper.toItemRequestFullDto(request1)).thenReturn(dto1);

        List<ItemRequestFullResponseDto> result = itemRequestService.getAllRequestsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(dto1, result.get(0));
        assertTrue(result.get(0).getItems().isEmpty());
    }

    @Test
    void getAllByRequestId_ThrowNotFoundExceptionIfItemRequestDoesNotExist() {
        when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class,
                () -> {
                    itemRequestService.getAllByRequestId(1L);
                });

        assertEquals("Запрос не найден", exception.getMessage());
    }

    @Test
    void getAllRequests_ReturnsListOfRequestsWithPagination() {
        int from = 0;
        int size = 10;

        User requester = new User();
        requester.setId(2L);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request1");
        request1.setCreated(LocalDateTime.now().minusDays(1));
        request1.setRequester(requester);

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request2");
        request2.setCreated(LocalDateTime.now());
        request2.setRequester(requester);


        List<ItemRequest> requests = List.of(request2, request1);
        Page<ItemRequest> page = new PageImpl<>(requests);

        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));

        when(itemRequestRepository.findByRequesterId(userId, pageable)).thenReturn(page);

        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setId(request1.getId());
        dto1.setDescription(request1.getDescription());
        dto1.setCreated(request1.getCreated());

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setId(request2.getId());
        dto2.setDescription(request2.getDescription());
        dto2.setCreated(request2.getCreated());

        when(itemRequestMapper.toItemRequestDto(request1)).thenReturn(dto1);
        when(itemRequestMapper.toItemRequestDto(request2)).thenReturn(dto2);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(from, size, userId);

        assertEquals(2, result.size());
        assertEquals(dto2, result.get(0));
        assertEquals(dto1, result.get(1));
    }

    @Test
    void getAllRequests_returnsEmptyList() {
        int from = 0;
        int size = 10;
        Page<ItemRequest> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
        when(itemRequestRepository.findByRequesterId(userId, pageable)).thenReturn(emptyPage);
        List<ItemRequestDto> result = itemRequestService.getAllRequests(from, size, userId);
        assertTrue(result.isEmpty());
    }
}
