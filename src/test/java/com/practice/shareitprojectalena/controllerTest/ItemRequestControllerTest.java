package com.practice.shareitprojectalena.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shareitprojectalena.request.ItemRequestController;
import com.practice.shareitprojectalena.request.dto.ItemRequestCreateDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestFullDto;
import com.practice.shareitprojectalena.request.service.ItemRequestService;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void create_RequestSuccess() {
        User requestor = new User();
        requestor.setName("Иван");
        Long requestorId = 1L;
        requestor.setId(requestorId);

        ItemRequestCreateDto newRequestCreateDto = new ItemRequestCreateDto();
        newRequestCreateDto.setDescription("Электрическая дрель");

        ItemRequestDto itemRequestDtoResponce = new ItemRequestDto();
        Long id = 1L;
        itemRequestDtoResponce.setId(id);
        itemRequestDtoResponce.setDescription("Электрическая дрель");
        itemRequestDtoResponce.setRequesterId(requestor.getId());

        when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        when(itemRequestService.create(requestor.getId(), newRequestCreateDto)).thenReturn(itemRequestDtoResponce);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header(USER_HEADER, requestor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRequestCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description", Matchers.equalTo("Электрическая дрель")))
                .andExpect(jsonPath("$.requesterId", Matchers.equalTo(1)));
    }


    @Test
    @SneakyThrows
    void getAllRequests_Success() {
        Long userId = 1L;
        ItemRequestFullDto request1 = new ItemRequestFullDto();
        request1.setId(1L);
        request1.setDescription("Электрическая дрель");
        request1.setRequesterId(userId);
        request1.setCreated(LocalDateTime.now());

        ItemRequestFullDto request2 = new ItemRequestFullDto();
        request2.setId(2L);
        request2.setDescription("Молоток");
        request2.setRequesterId(userId);
        request2.setCreated(LocalDateTime.now());

        List<ItemRequestFullDto> ListOfRequests = Arrays.asList(request1, request2);

        when(itemRequestService.getAllRequestsByUserId(userId)).thenReturn(ListOfRequests);
        mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$[0].description", Matchers.is("Электрическая дрель")))
                .andExpect(jsonPath("$[1].id", Matchers.is(2)))
                .andExpect(jsonPath("$[1].description", Matchers.is("Молоток")));
    }

    @Test
    @SneakyThrows
    void getRequestById_Success() {
        Long requestId = 1L;
        ItemRequestFullDto expectedRequest = new ItemRequestFullDto();
        expectedRequest.setId(requestId);
        expectedRequest.setDescription("Электрическая дрель");
        expectedRequest.setRequesterId(1L);

        when(itemRequestService.getAllByRequestId(requestId)).thenReturn(expectedRequest);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.description", Matchers.is("Электрическая дрель")))
                .andExpect(jsonPath("$.requesterId", Matchers.is(1)));
    }

    @Test
    @SneakyThrows
    void getAllRequestsByPage_Success() {
        Long userId = 1L;
        int from = 0;
        int size = 10;

        ItemRequestDto request1 = new ItemRequestDto();
        request1.setId(1L);
        request1.setDescription("Электрическая дрель");
        request1.setRequesterId(userId);

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setId(2L);
        request2.setDescription("Молоток");
        request2.setRequesterId(userId);

        List<ItemRequestDto> expectedRequests = Arrays.asList(request1, request2);

        when(itemRequestService.getAllRequests(from, size, userId)).thenReturn(expectedRequests);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all/from/size")
                        .header(USER_HEADER, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$[0].description", Matchers.is("Электрическая дрель")))
                .andExpect(jsonPath("$[1].id", Matchers.is(2)))
                .andExpect(jsonPath("$[1].description", Matchers.is("Молоток")));
    }


}

