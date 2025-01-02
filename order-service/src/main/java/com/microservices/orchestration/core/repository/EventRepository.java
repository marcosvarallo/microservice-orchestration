package com.microservices.orchestration.core.repository;

import com.microservices.orchestration.core.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
}
