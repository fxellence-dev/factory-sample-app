package com.example.sampleapp.controller;

import com.example.sampleapp.model.Item;
import com.example.sampleapp.repository.ItemRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public List<Item> getAll() {
        return itemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Item create(@Valid @RequestBody Item item) {
        return itemRepository.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @Valid @RequestBody Item updated) {
        return itemRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    return ResponseEntity.ok(itemRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
