package com.practice.shareitprojectalena.request;

import com.practice.shareitprojectalena.request.entity.ItemRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("select iR from ItemRequest iR where iR.requester.id = ?1")
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId, Sort sort);

    Page<ItemRequest> findByRequesterId(Long requesterId, Pageable pageable);
}
