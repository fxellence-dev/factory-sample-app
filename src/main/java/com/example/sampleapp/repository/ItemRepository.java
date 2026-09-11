package com.example.sampleapp.repository;

import com.example.sampleapp.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
