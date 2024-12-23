package com.practice.shareitprojectalena.serviceTest;

import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.error.exceptions.ValidationException;
import com.practice.shareitprojectalena.request.ItemRequestMapper;
import com.practice.shareitprojectalena.request.ItemRequestRepository;
import com.practice.shareitprojectalena.request.dto.ItemRequestCreateDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.request.service.ItemRequestService;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Spy
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    ItemRequestService itemRequestService;

    private User user;
    private ItemRequestCreateDto itemRequestCreateDto;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);

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

        Mockito.when(userService.findById(1L))
                .thenReturn(user);
        Exception exception = assertThrows(ValidationException.class,
                () -> {
                    itemRequestService.create(1L, itemRequestCreateDto);
                });

        assertEquals("Описание должно быть заполнено", exception.getMessage());
    }


    @Test
    public void create_ReturnItemRequestDtoWhenUserExists() {
        Mockito.when(userService.findById(1L))
                .thenReturn(user);
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);
        Mockito.when(itemRequestMapper.toItemRequestDto(itemRequest))
                .thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.create(1L, itemRequestCreateDto);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
    }

    @Test
    public void create_ThrowUserNotFoundException_WhenUserDoesNotExist() {
        Mockito.when(userService.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> {
                    itemRequestService.create(1L, itemRequestCreateDto);
                });

        assertEquals("Объект не найден", exception.getMessage());
    }

        @Test
        void getAllRequestsByUserId_ThrowUserNotFoundExceptionIfUserDoesNotExist() {
            Mockito.when(userService.findById(1L))
                    .thenReturn(null);

            Exception exception = assertThrows(NotFoundException.class,
                    () -> {
                        itemRequestService.getAllRequestsByUserId(1L);
                    });

            assertEquals("Объект не найден", exception.getMessage());
        }
        @Test
        void getAllByRequestId_ThrowNotFoundExceptionIfItemRequestDoesNotExist() {
            Mockito.when(itemRequestRepository.findById(1L))
                    .thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundException.class,
                    () -> {
                        itemRequestService.getAllByRequestId(1L);
                    });

            assertEquals("Запрос не найден", exception.getMessage());
        }

}
