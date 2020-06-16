package com.vladmykol.takeandcharge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import java.time.Duration;
import java.time.Instant;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMongoAuditing
public class MongoConfig {
    private final MongoTemplate mongoTemplate;
    private final MongoConverter mongoConverter;

    @EventListener(ContextRefreshedEvent.class)
    public void autoIndexCreation() {
        Instant start = Instant.now();
        var mappingContext = (MongoMappingContext) mongoConverter.getMappingContext();
        var resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        // consider only entities that are annotated with @Document
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Document.class))
                .forEach(it -> {
                    var indexOps = mongoTemplate.indexOps(it.getType());
                    resolver.resolveIndexFor(it.getType()).forEach(indexOps::ensureIndex);
                });
        Duration timeElapsed = Duration.between(start, Instant.now());
        log.info("Mongo DB index creation on startup took: {}", DurationFormatUtils.formatDurationHMS(timeElapsed.toMillis()));
    }
}
