package com.firisbe.service.Implementation;

import com.firisbe.aspect.sequence.SequenceGenerator;
import com.firisbe.model.Log;
import com.firisbe.repository.mongo.LogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogServiceImplementationTest {

    @Mock
    private LogRepository mockRepo;

    @Mock
    private SequenceGenerator mockSequenceGenerator;

    private LogServiceImplementation logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logService = new LogServiceImplementation(mockRepo, mockSequenceGenerator);
    }

    @AfterEach
    void tearDown() {
        // Any cleanup code can go here
    }

    @Test
    void logAdding() {
        // Given
        when(mockSequenceGenerator.generateSequence(LogServiceImplementation.class)).thenReturn(1L);

        // When
        logService.logAdding("Test log message");

        // Then
        verify(mockRepo).save(any(Log.class));
    }

    @Test
    void successLogListener() {
        // Given
        String message = "Success message";

        // When
        logService.successLogListener(message);

        // Then
        verify(mockRepo).save(any(Log.class));
    }

    @Test
    void errorLogListener() {
        // Given
        String message = "Error message";

        // When
        logService.errorLogListener(message);

        // Then
        verify(mockRepo).save(any(Log.class));
    }

    @Test
    void paymentLogListener() {
        // Given
        String message = "Payment message";

        // When
        logService.paymentLogListener(message);

        // Then
        verify(mockRepo).save(any(Log.class));
    }
}