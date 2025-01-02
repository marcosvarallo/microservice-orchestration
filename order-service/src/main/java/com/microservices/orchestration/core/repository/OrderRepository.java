package com.microservices.orchestration.core.repository;

import com.microservices.orchestration.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
