package com.kurz.itemapi;

import java.util.List;
import java.util.Optional;

/**
 * The controller's single collaborator. Because {@link ItemController} depends on this
 * interface rather than constructing a catalog itself, a {@code @WebMvcTest} slice can
 * substitute a mock for it instead of loading a real implementation.
 */
public interface ItemService {

    List<Item> findAll();

    Optional<Item> findById(Long id);
}
