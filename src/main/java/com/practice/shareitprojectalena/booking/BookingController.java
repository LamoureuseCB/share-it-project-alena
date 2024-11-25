package com.practice.shareitprojectalena.booking;

import com.practice.shareitprojectalena.utils.BookingStatus;
import com.practice.shareitprojectalena.booking.dto.BookingCreateDto;
import com.practice.shareitprojectalena.booking.dto.BookingResponseDto;
import com.practice.shareitprojectalena.error.exceptions.ConflictException;
import com.practice.shareitprojectalena.item.Item;
import com.practice.shareitprojectalena.item.ItemService;
import com.practice.shareitprojectalena.user.entity.User;
import com.practice.shareitprojectalena.user.UserService;
import com.practice.shareitprojectalena.utils.State;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;


import java.util.List;


import static com.practice.shareitprojectalena.utils.RequestConstants.USER_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingMapper bookingMapper;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;


    @PostMapping
    public BookingResponseDto create(
            @RequestHeader(USER_HEADER) Long bookerId,
            @RequestBody @Valid BookingCreateDto bookingCreateDto) {
        Item item = itemService.findById(bookingCreateDto.getItemId());
        if (!item.getIsAvailable()) {
            throw new ConflictException("Предмет недоступен для бронирования");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ConflictException("Владелец не должен  бронировать свою вещь");
        }
        User booker = userService.findById(bookerId);
        Booking booking = bookingMapper.fromCreate(bookingCreateDto, item);
        booking.setBooker(booker);
        return bookingMapper.toResponse(bookingService.create(booking, bookerId, item));
    }


    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(
            @RequestHeader(USER_HEADER) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved) {
        Booking existingBooking = bookingService.findById(bookingId);
        if (!existingBooking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ConflictException("Вы не можете изменять это бронирование, если Вы не владелец");
        }
        if (approved) {
            existingBooking.setStatus(BookingStatus.APPROVED);
        } else {
            existingBooking.setStatus(BookingStatus.REJECTED);
        }
        Booking updatingBooking = bookingService.update(bookingId, ownerId, approved);
        return bookingMapper.toResponse((updatingBooking));

    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findById(@RequestHeader(USER_HEADER) Long bookerId,
                                       @PathVariable Long bookingId) {
        Booking booking = bookingService.findById(bookingId);
        if (!booking.getBooker().getId().equals(bookerId) &&
                !booking.getItem().getOwner().getId().equals(bookerId)) {
            throw new ConflictException("нет доступа к информации об этом бронировании");
        }
        return bookingMapper.toResponse(booking);

    }

    @GetMapping
    public List<BookingResponseDto> getBookingsByState(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") State state) {
        List<Booking> bookings = bookingService.getBookingByBooker(state, userId);
        return bookings.stream().map(bookingMapper::toResponse).toList();
    }


    @GetMapping("/owner/{ownerId}")
    public List<BookingResponseDto> findAllByOwnerIdAndState(@RequestHeader(USER_HEADER) Long userId,
                                                     @PathVariable Long ownerId,
                                                     @RequestParam(value = "state",
                                                             defaultValue = "ALL") State state) {
        if (!userId.equals(ownerId)) {
            throw new ConflictException("Вы не можете просматривать бронирования другого пользователя");
        }
        List<Booking> bookings = bookingService.getByStateAndOwner(state, ownerId);

        return bookings.stream().map(bookingMapper::toResponse).toList();

    }
    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsByOwner(
            @RequestHeader(USER_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") State state) {
        List<Booking> bookings = bookingService.getBookingByBooker(state, ownerId);
        return bookings.stream().map(bookingMapper::toResponse).toList();
    }
}





