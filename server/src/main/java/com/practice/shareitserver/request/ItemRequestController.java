package com.practice.shareitserver.request;


import com.practice.common.dto.ItemRequestFullResponseDto;
import com.practice.shareitserver.request.dto.ItemRequestCreateDto;
import com.practice.shareitserver.request.dto.ItemRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.practice.shareitserver.utils.RequestConstants.USER_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader(USER_HEADER) Long userId,
                                 @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestService.create(userId, itemRequestCreateDto);
    }



    @GetMapping
    public List<ItemRequestFullResponseDto> getAllRequests(@RequestHeader(USER_HEADER) Long userId) {
        return itemRequestService.getAllRequestsByUserId(userId);
    }

    @SneakyThrows
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequestsByPage(@RequestHeader(USER_HEADER) Long userId,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAllRequests(from, size, userId);
    }


    @SneakyThrows
    @GetMapping("/{requestId}")
    public ItemRequestFullResponseDto getRequestById(@PathVariable Long requestId) {
        return itemRequestService.getAllByRequestId(requestId);
    }
}
