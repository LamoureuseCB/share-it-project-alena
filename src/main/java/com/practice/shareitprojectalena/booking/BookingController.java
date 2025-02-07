package com.practice.shareitprojectalena.booking;

import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemRepository;
import com.practice.shareitprojectalena.utils.State;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingMapper bookingMapper;
    private final BookingService bookingService;
    private final ItemRepository itemRepository;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto create(
            @RequestHeader(USER_HEADER) Long bookerId,
            @RequestBody @Valid BookingCreateDto bookingCreateDto) {
        logger.info("Получен запрос на создание бронирования: {}", bookingCreateDto);
        try {
            Item item = itemRepository.findById(bookingCreateDto.getItemId())
                    .orElseThrow(() -> new NotFoundException("Предмет не найден"));
            Long ownerId = item.getOwner().getId();
            if (ownerId.equals(bookerId)) {
                throw new ForbiddenException("Владелец не должен бронировать свою вещь");
            }
            Booking booking = bookingMapper.fromCreate(bookingCreateDto);
            logger.info("Созданный объект Booking: {}", booking);
            Booking createdBooking = bookingService.create(booking, bookerId);
            logger.info("Созданный объект Booking из сервиса: {}", createdBooking);
            return bookingMapper.toResponse(createdBooking);
        } catch (NotFoundException exception) {
            logger.error("Ошибка: {}", exception.getMessage());
            throw exception;
        }
    }


    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(
            @RequestHeader(USER_HEADER) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved) {
        Booking updatingBooking = bookingService.update(bookingId, ownerId, approved);
        return bookingMapper.toResponse((updatingBooking));

    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findById(@RequestHeader(USER_HEADER) Long bookerId,
                                       @PathVariable Long bookingId) {
        Booking booking = bookingService.findById(bookingId);
        if (!booking.getBooker().getId().equals(bookerId) &&
                !booking.getItem().getOwner().getId().equals(bookerId)) {
            throw new ConflictException("Нет доступа к информации об этом бронировании");
        }
        return bookingMapper.toResponse(booking);

    }

    @GetMapping
    public List<BookingResponseDto> getBookingsByState(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") State state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Booking> bookings = bookingService.getBookingByBooker(state, userId, from, size);
        return bookings.stream().map(bookingMapper::toResponse).toList();
    }


    @GetMapping("/owner/{ownerId}")
    public Page<BookingResponseDto> findAllByOwnerIdAndState(@RequestHeader(USER_HEADER) Long userId,
                                                             @PathVariable Long ownerId,
                                                             @RequestParam(value = "state", defaultValue = "ALL") State state,
                                                             Pageable pageable) {
        if (!userId.equals(ownerId)) {
            throw new ConflictException("Вы не можете просматривать бронирования другого пользователя");
        }

        Page<Booking> bookings = bookingService.getByStateAndOwner(state, ownerId, pageable);

        return bookings.map(bookingMapper::toResponse);
    }


    @GetMapping(value = "/owner")
    public List<BookingResponseDto> getBookingsByOwner(
            @RequestHeader(USER_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") State state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        Page<Booking> bookings = bookingService.getBookingByBooker(state, ownerId, from, size);
        return bookings.stream().map(bookingMapper::toResponse).toList();
    }
}





