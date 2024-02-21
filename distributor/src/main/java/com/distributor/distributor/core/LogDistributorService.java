package com.distributor.distributor.core;

import com.distributor.distributor.data.LogMessage;
import com.distributor.distributor.data.LogPacket;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Timestamp;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;
import java.util.Queue;
import org.jctools.queues.MpscArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogDistributorService {

  private final Logger logger = LoggerFactory.getLogger(LogDistributorService.class);
  private final int MAX_QUEUE_SIZE = 100000;

  private final Queue<LogPacket> queue = new MpscArrayQueue<>(MAX_QUEUE_SIZE);

  @Autowired private LogAnalyzerServiceGetter logAnalyzerServiceGetter;

  public LogDistributorService() {
    // Start the processing thread upon instantiation
    startProcessingThread();
  }

  public boolean distributeLog(LogPacket logPacket) {
    // Add the log packet to the queue for processing
    return queue.offer(logPacket);
  }

  private void startProcessingThread() {
    // Start a separate thread for processing log packets
    Thread processingThread =
        new Thread(
            () -> {
              while (true) {
                LogPacket logPacket = queue.poll();
                if (logPacket != null) {
                  processLogPacket(logPacket);
                }
              }
            });
    processingThread.start();
  }

  private void processLogPacket(LogPacket logPacket) {
    // Add your logic here to process the received log packet
    logger.debug("Processing log packet!");

    LogAnalyzerServiceOuterClass.LogPacket logPacketProto = toProto(logPacket);
    ListenableFuture<LogAnalyzerServiceOuterClass.AnalysisResult> resultListenableFuture =
        logAnalyzerServiceGetter.getLogAnalyzerServiceStub().analyzeLog(logPacketProto);
    resultListenableFuture.addListener(
        () -> {
          try {
            LogAnalyzerServiceOuterClass.AnalysisResult result = resultListenableFuture.get();
            if (result.getSuccess()) {
              logger.debug("Log packet processed successfully. Response: {}", result.getMessage());
            } else {
              logger.warn("Log packet processing failed, {}", result.getMessage());
              logger.info("RETRYING...");
              waitAndRetry(logPacket);
            }
          } catch (Exception e) {
            logger.error("Error processing log packet", e);
            logger.info("RETRYING...");
            waitAndRetry(logPacket);
          }
        },
        Runnable::run);
  }

  private void waitAndRetry(LogPacket logPacket) {
    try {
      Thread.sleep(50);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    // try again
    processLogPacket(logPacket);
  }

  private LogAnalyzerServiceOuterClass.LogPacket toProto(LogPacket logPacket) {
    LogAnalyzerServiceOuterClass.LogPacket.Builder logPacketProtoBuilder =
        LogAnalyzerServiceOuterClass.LogPacket.newBuilder();
    logPacket
        .logMessages()
        .forEach(logMessage -> logPacketProtoBuilder.addLogMessages(toProto(logMessage)));
    return logPacketProtoBuilder.build();
  }

  private LogAnalyzerServiceOuterClass.LogMessage toProto(LogMessage logMessage) {
    return LogAnalyzerServiceOuterClass.LogMessage.newBuilder()
        .setMessage(logMessage.message())
        .setSeverityValue(logMessage.logLevel().ordinal())
        .setTimestamp(
            Timestamp.newBuilder()
                .setSeconds(logMessage.timestamp().getEpochSecond())
                .setNanos(logMessage.timestamp().getNano()))
        .build();
  }
}
