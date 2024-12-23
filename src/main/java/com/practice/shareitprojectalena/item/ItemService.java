package com.practice.shareitprojectalena.item;

import com.practice.shareitprojectalena.error.exceptions.ForbiddenException;
import com.practice.shareitprojectalena.error.exceptions.InvalidPageException;
import com.practice.shareitprojectalena.error.exceptions.InvalidSizeException;
import com.practice.shareitprojectalena.error.exceptions.NotFoundException;
import com.practice.shareitprojectalena.item.comment.CommentRepository;
import com.practice.shareitprojectalena.request.ItemRequestRepository;
import com.practice.shareitprojectalena.request.entity.ItemRequest;
import com.practice.shareitprojectalena.user.UserRepository;
import com.practice.shareitprojectalena.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@AllArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
        private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;

    public Item create(Item item, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по данному ID не найден"));
        item.setOwner(user);
        if (item.getRequest() != null) {
            ItemRequest request = itemRequestRepository.findById(item.getRequest().getId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
        }

        return itemRepository.save(item);
    }


    public Item findById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Объект не найден"));
    }

    public Item update(Item item, Long itemId, Long userId) {
        Item existingItem = findById(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Вещь для проката по данному ID не найдена");
        }
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Обновлять параметры вещи может только владелец");
        }
        itemMapper.merge(existingItem, item);
        return itemRepository.save(existingItem);
    }


    @SneakyThrows
    public List<Item> findAll(Long userId, int from, int size) {
        if (from < 0) throw new InvalidPageException("Ошибка!Страница не должна быть меньше нуля");
        if (size <= 0) throw new InvalidSizeException("Ошибка!Размер должен быть положительным");

        Pageable pageable = PageRequest.of(from / size, size);
        return itemRepository.findAllByOwner_Id(userId, pageable);
    }

    public void delete(Long id) {
        Item item = findById(id);
        itemRepository.deleteById(item.getId());
    }

    @SneakyThrows
    public List<Item> searchItems(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        if (from < 0) throw new InvalidPageException("Ошибка!Страница не должна быть меньше нуля");
        if (size <= 0) throw new InvalidSizeException("Ошибка!Размер должен быть положительным");

        Pageable pageable = PageRequest.of(from, size);
        return itemRepository.search(text, pageable);
    }

    public List<Item> findByRequestId(Long requesterId) {
        return itemRepository.findByRequestId(requesterId);
    }

//    public Item getItemWithComments(Long itemId) {
//        Item item = findById(itemId);
//        List<Comment> comments = item.getComments();
//        return item;
//    }
}

