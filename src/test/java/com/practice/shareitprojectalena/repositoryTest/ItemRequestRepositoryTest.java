//package com.practice.shareitprojectalena.repositoryTest;
//
//import com.practice.shareitprojectalena.request.ItemRequestRepository;
//import com.practice.shareitprojectalena.request.entity.ItemRequest;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//
//@DataJpaTest
//public class ItemRequestRepositoryTest {
//    @Autowired
//    private ItemRequestRepository itemRequestRepository;
//
//    @Test
//    void findAllByRequesterIdOrderByCreatedDesc_ShouldReturnSortedRequests() {
//        Long requesterId = 1L;
//        Sort sort = Sort.by(Sort.Direction.DESC, "created");
//        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId, sort);
//        assertFalse(itemRequests.isEmpty());
//    }
//
//    @Test
//    void findByRequesterId_ShouldReturnPageOfRequests() {
//        Long requesterId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<ItemRequest> itemRequests = itemRequestRepository.findByRequesterId(requesterId, pageable);
//        assertFalse(itemRequests.isEmpty());
//    }
//}
//
//
