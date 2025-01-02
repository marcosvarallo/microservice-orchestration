package com.microservices.orchestration.core.service;

import com.microservices.orchestration.config.exception.ValidationException;
import com.microservices.orchestration.core.document.Event;
import com.microservices.orchestration.core.dto.EventFilters;
import com.microservices.orchestration.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters filters) {
        validateEmptyFilters(filters);
        if (!ObjectUtils.isEmpty(filters.getOrderId())) {
            return findByOrderId(filters.getOrderId());
        } else {
            return findByTransactionId(filters.getTransactionId());
        }
    }

    private Event findByOrderId(String orderId) {
        return eventRepository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by OrderId."));
    }

    private Event findByTransactionId(String transactionId) {
        return eventRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by TransactionId."));
    }

    private void validateEmptyFilters(EventFilters filters) {
        if (ObjectUtils.isEmpty(filters.getOrderId()) && ObjectUtils.isEmpty(filters.getTransactionId())) {
            throw new ValidationException("OrderId or transactionId cannot be empty");
        }
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }
}
