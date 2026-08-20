package com.kurz.itemapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * A small, fully working REST controller. Its job in this lab isn't to be extended -
 * it already does everything it needs to - it's to be tested at the web layer with
 * MockMvc, in isolation from the real {@link ItemServiceImpl}.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> getAll() {
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable("id") Long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
