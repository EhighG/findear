package com.findear.batch.ours.repository;

import com.findear.batch.ours.domain.FindearMatchingLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FindearMatchingLogRepository extends ElasticsearchRepository<FindearMatchingLog, Long> {
}
