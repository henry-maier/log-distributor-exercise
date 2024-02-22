package com.distributor.distributor.core;

import com.distributor.distributor.core.UniqueFloatGenerator;
import com.google.protobuf.Message;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class UniqueFloatGeneratorTest {

    @Test
    void testGenerateUniqueFloat_WithinRange() {
        // Test generating unique floats for multiple message objects
        LogAnalyzerServiceOuterClass.LogPacket logPacket1 = LogAnalyzerServiceOuterClass.LogPacket.newBuilder()
                .addLogMessages(LogAnalyzerServiceOuterClass.LogMessage.newBuilder().setMessage("Message 1").build())
                .build();
        LogAnalyzerServiceOuterClass.LogPacket logPacket2 = LogAnalyzerServiceOuterClass.LogPacket.newBuilder()
                .addLogMessages(LogAnalyzerServiceOuterClass.LogMessage.newBuilder().setMessage("Message 2").build())
                .build();

        // Generate unique floats for the same message objects
        float float1 = UniqueFloatGenerator.generateUniqueFloat(logPacket1);
        float float2 = UniqueFloatGenerator.generateUniqueFloat(logPacket2);

        // Verify that generated floats are within the range [0, 1]
        assertFloatInRange(float1);
        assertFloatInRange(float2);
    }

    @Test
    void testGenerateUniqueFloat_SameObjectSameValue() {
        // Test generating unique floats for the same message object
        LogAnalyzerServiceOuterClass.LogPacket logPacket = LogAnalyzerServiceOuterClass.LogPacket.newBuilder()
                .addLogMessages(LogAnalyzerServiceOuterClass.LogMessage.newBuilder().setMessage("Message 1").build())
                .build();

        // Generate unique floats for the same message object twice
        float float1 = UniqueFloatGenerator.generateUniqueFloat(logPacket);
        float float2 = UniqueFloatGenerator.generateUniqueFloat(logPacket);

        // Verify that the generated floats are the same for the same message object
        assertEquals(float1, float2);
    }

    @Test
    void testGenerateUniqueFloat_ExceptionHandling() {
        // Mock an exception occurring during float generation
        LogAnalyzerServiceOuterClass.LogPacket logPacket = Mockito.mock(LogAnalyzerServiceOuterClass.LogPacket.class);
        when(logPacket.toByteArray()).thenThrow(new RuntimeException("Exception occurred"));

        // Generate a float when an exception occurs
        float result = UniqueFloatGenerator.generateUniqueFloat(logPacket);

        // Verify that the result is 0 when an exception occurs
        assertEquals(0.0f, result);
    }

    private void assertFloatInRange(float value) {
        // Verify that the float value is within the range [0, 1]
        assertTrue(value >= 0 && value <= 1);
    }
}
