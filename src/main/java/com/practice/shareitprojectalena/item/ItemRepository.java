package com.practice.shareitprojectalena.item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long> {
    List<Item> findAllByOwner_Id(Long userId);
   @Query("select i from Item i " +
           "where( upper(i.name)  like concat ('%',upper(:text) ,'%')" +
           " or upper(i.description) like concat ('%',upper(:text),'%'))" +
           " and i.isAvailable = true ")
    List<Item> search(String text);

}
