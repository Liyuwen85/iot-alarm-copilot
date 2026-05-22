package com.example.iotalarmcopilot.access.infrastructure.persistence;

import com.example.iotalarmcopilot.BaseDomainException;
import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLog;
import com.example.iotalarmcopilot.access.domain.AccessDeadLetterLogRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisAccessDeadLetterLogRepository implements AccessDeadLetterLogRepository {

    private final AccessDeadLetterMapper accessDeadLetterMapper;

    public MybatisAccessDeadLetterLogRepository(AccessDeadLetterMapper accessDeadLetterMapper) {
        this.accessDeadLetterMapper = accessDeadLetterMapper;
    }

    @Override
    public AccessDeadLetterLog saveIfAbsent(AccessDeadLetterLog log) {
        AccessDeadLetterRecord record = AccessDeadLetterRecord.fromDomain(log);
        accessDeadLetterMapper.insertIgnore(record);
        AccessDeadLetterRecord savedRecord = accessDeadLetterMapper.selectOne(
                log.originalTopic(),
                log.originalPartition(),
                log.originalOffset());
        if (savedRecord == null) {
            throw new BaseDomainException("Failed to persist or load access dead letter log");
        }
        return savedRecord.toDomain();
    }
}
