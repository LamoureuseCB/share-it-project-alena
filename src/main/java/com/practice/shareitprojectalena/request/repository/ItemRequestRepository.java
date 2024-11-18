package com.practice.shareitprojectalena.request.repository;

import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest,Long> {

    List<ItemRequest> findItemRequestByRequester_Id(Long userId);

    List<ItemRequest> findItemRequestByRequester(User user);

}
