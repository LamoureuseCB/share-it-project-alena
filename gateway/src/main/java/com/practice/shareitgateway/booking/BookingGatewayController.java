package com.practice.shareitgateway.booking;

import com.practice.shareitgateway.booking.dto.BookingCreateDto;
import com.practice.shareitgateway.booking.dto.BookingResponseDto;
import com.practice.shareitgateway.error.exceptions.NotFoundException;
import com.practice.shareitgateway.utils.State;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.practice.shareitgateway.utils.RequestConstants.SERVER_URL;
import static com.practice.shareitgateway.utils.RequestConstants.USER_HEADER;


@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@Slf4j
public class BookingGatewayController {

    private final RestTemplate restTemplate;

    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set(USER_HEADER, String.valueOf(userId));
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @PostMapping
    public BookingResponseDto create(
            @RequestHeader(USER_HEADER) Long bookerId,
            @RequestBody @Valid BookingCreateDto bookingCreateDto) {
        if (bookingCreateDto.getStart().isBefore(LocalDateTime.now()) ||
                bookingCreateDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата бронирования не может быть в прошлом");
        }

        if (bookingCreateDto.getStart().isAfter(bookingCreateDto.getEnd()) ||
                bookingCreateDto.getStart().isEqual(bookingCreateDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }
        HttpHeaders headers = createHeaders(bookerId);
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, headers);
        return restTemplate.postForObject(SERVER_URL + "/bookings", request, BookingResponseDto.class);


    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(
            @RequestHeader(USER_HEADER) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved) {

        if (ownerId == null || bookingId == null) {
            throw new IllegalArgumentException("ownerId и bookingId не могут быть null");
        }

        HttpHeaders headers = createHeaders(ownerId);
        HttpEntity<Void> request = new HttpEntity<>(headers);

      BookingResponseDto response = restTemplate.patchForObject(
                SERVER_URL + "/bookings/" + bookingId + "?approved=" + approved,
                request,
                BookingResponseDto.class
        );
      log.info("Ответ от сервера {}", response);
      return response;
    }



    @GetMapping("/{bookingId}")
    public BookingResponseDto findBookingById(@RequestHeader(USER_HEADER) Long bookerId,
                                              @PathVariable Long bookingId) {
        HttpHeaders headers = createHeaders(bookerId);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            return restTemplate.getForObject(
                    SERVER_URL + "/booking/" + bookingId,
                    BookingResponseDto.class,
                    request
            );
        } catch (HttpClientErrorException.NotFound exception) {
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        }
    }


    @GetMapping
    public List<BookingResponseDto> getBookingsByState(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") State state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Некорректно введены параметры пагинации");
        }

        HttpHeaders headers = createHeaders(userId);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        BookingResponseDto[] bookings = restTemplate.getForObject(
                SERVER_URL + "/bookings?state=" + state + "&from=" + from + "&size=" + size,
                BookingResponseDto[].class,
                request
        );

        return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
    }


    @GetMapping("/owner/{ownerId}")
    public Page<BookingResponseDto> findAllByOwnerIdAndState(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long ownerId,
            @RequestParam(value = "state", defaultValue = "ALL") State state,
            Pageable pageable) {

        HttpHeaders headers = createHeaders(userId);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = SERVER_URL + "/bookings/owner/" + ownerId +
                "?state=" + state +
                "&page=" + pageable.getPageNumber() +
                "&size=" + pageable.getPageSize() +
                "&sort=" + pageable.getSort().toString().replace(": ", ",");

        BookingResponseDto[] bookings = restTemplate.getForObject(
                url,
                BookingResponseDto[].class,
                request
        );

        List<BookingResponseDto> content = bookings != null ? List.of(bookings) : Collections.emptyList();
        return new PageImpl<>(content, pageable, content.size());
    }


    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsByOwner(
            @RequestHeader(USER_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") State state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        HttpHeaders headers = createHeaders(ownerId);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        BookingResponseDto[] bookings = restTemplate.getForObject(
                SERVER_URL + "/bookings/owner?state=" + state + "&from=" + from + "&size=" + size,
                BookingResponseDto[].class,
                request
        );

        return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
    }


}





