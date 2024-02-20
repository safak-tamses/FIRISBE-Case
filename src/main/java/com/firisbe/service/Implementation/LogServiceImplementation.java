package com.firisbe.service.Implementation;

import com.firisbe.aspect.sequence.SequenceGenerator;
import com.firisbe.model.Log;
import com.firisbe.repository.mongo.LogRepository;
import com.firisbe.service.Interface.LogServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogServiceImplementation implements LogServiceInterface {
    private final LogRepository repo;
    private final SequenceGenerator sequenceGenerator;

    @Override
    public void logAdding(String data) {
        long seq = sequenceGenerator.generateSequence(LogServiceImplementation.class);

        Log log = Log.builder()
                .id(String.valueOf(seq))
                .logMessage(data)
                .build();
        repo.save(log);
    }

    @Override
    @KafkaListener(
            topics = {"${kafka.topic.create}", "${kafka.topic.read}", "${kafka.topic.update}", "${kafka.topic.delete}"},
            groupId = "${kafka.groupId}"
    )
    public void listener(String data) {
        logAdding(data);
    }

    @Override
    @KafkaListener(
            topics = {"${kafka.topic.payment_log}"},
            groupId = "${kafka.groupId}"
    )
    public void paymentLog(String message) {
        logAdding(message);
    }
}
