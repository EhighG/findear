package com.findear.batch.ours.repository;

import com.findear.batch.ours.domain.PoliceMatchingLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PoliceMatchingLogRepository extends ElasticsearchRepository<PoliceMatchingLog, Long> {

}
