//package com.practice.shareitprojectalena.booking.entity;
//
//import com.practice.shareitprojectalena.utils.BookingStatus;
//import jakarta.persistence.*;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//public class BookingStatusSet {
//   @Id
//        @GeneratedValue(strategy = GenerationType.IDENTITY)
//        private Long id;
//
//        @Enumerated(EnumType.STRING)
//        private BookingStatus status;
//
//        @OneToMany(mappedBy = "status")
//        private Set<Booking> bookings = new HashSet<>();
//
//    }
