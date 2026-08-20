package com.kurz.itemapi;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * The real, fully working implementation of {@link ItemService}. A
 * {@code @WebMvcTest(ItemController.class)} slice never loads this class - it's a
 * {@code @Service}, not part of the web layer - which is exactly the behavior
 * {@code ItemControllerTest} is written to prove.
 */
@Service
public class ItemServiceImpl implements ItemService {

    private final List<Item> catalog = List.of(
            new Item(1L, "Keyboard", "Peripherals"),
            new Item(2L, "Monitor", "Displays"),
            new Item(3L, "Webcam", "Peripherals")
    );

    @Override
    public List<Item> findAll() {
        return catalog;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return catalog.stream()
                .filter(item -> item.id().equals(id))
                .findFirst();
    }
}
