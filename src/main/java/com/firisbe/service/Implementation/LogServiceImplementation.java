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
            topics = {"${kafka.topic.success}"},
            groupId = "${kafka.groupId}"
    )
    public void successLogListener(String message) {
        // Başarılı log dinleyicisi için log kaydetme işlevi
        logAdding("Success: " + message);
    }

    @Override
    @KafkaListener(
            topics = {"${kafka.topic.error}"},
            groupId = "${kafka.groupId}"
    )
    public void errorLogListener(String message) {
        // Hata log dinleyicisi için log kaydetme işlevi
        logAdding("Error: " + message);
    }

    @Override
    @KafkaListener(
            topics = {"${kafka.topic.paymentLog}"},
            groupId = "${kafka.groupId}"
    )
    public void paymentLogListener(String message) {
        // Ödeme log dinleyicisi için log kaydetme işlevi
        logAdding("Payment Log: " + message);
    }
}
