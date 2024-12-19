package com.practice.shareitprojectalena.request;

import com.practice.shareitprojectalena.request.dto.ItemRequestCreateDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestDto;
import com.practice.shareitprojectalena.request.dto.ItemRequestFullDto;
import com.practice.shareitprojectalena.request.service.ItemRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader(USER_HEADER) Long userId,
                                 @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestService.create(userId, itemRequestCreateDto);
    }



    @GetMapping
    public List<ItemRequestFullDto> getAllRequests(@RequestHeader(USER_HEADER) Long userId) {
        return itemRequestService.getAllRequestsByUserId(userId);
    }

    @SneakyThrows
    @GetMapping("/all/from/size")
    public List<ItemRequestDto> getAllRequestsByPage(@RequestHeader(USER_HEADER) Long userId,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAllRequests(from, size, userId);
    }


    @SneakyThrows
    @GetMapping("/{requestId}")
    public ItemRequestFullDto getRequestById(@PathVariable Long requestId) {
        return itemRequestService.getAllByRequestId(requestId);
    }
}
