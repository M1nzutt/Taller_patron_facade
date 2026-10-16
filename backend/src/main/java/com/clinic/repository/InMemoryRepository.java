package com.clinic.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic in-memory storage. The assignment requires all data to live in
 * memory, so this class replaces a database. It is safe for concurrent
 * requests (ConcurrentHashMap + AtomicLong).
 */
public abstract class InMemoryRepository<T> {

    private final Map<Long, T> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final Function<T, Long> idExtractor;

    protected InMemoryRepository(Function<T, Long> idExtractor) {
        this.idExtractor = idExtractor;
    }

    /** Generates the next auto-increment id, like a SQL sequence. */
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    /** Inserts or updates (upsert) the entity by its id. */
    public T save(T entity) {
        data.put(idExtractor.apply(entity), entity);
        return entity;
    }

    public Optional<T> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(data.get(id));
    }

    public boolean exists(Long id) {
        return id != null && data.containsKey(id);
    }

    /** All records sorted by id. */
    public List<T> findAll() {
        return data.keySet().stream()
                .sorted()
                .map(data::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public void delete(Long id) {
        if (id != null) {
            data.remove(id);
        }
    }

    public int count() {
        return data.size();
    }

    protected List<T> filter(Predicate<T> condition) {
        return findAll().stream().filter(condition).toList();
    }
}
