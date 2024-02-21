package com.analyzer.core;

import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@GrpcService
public class LogAnalyzerServiceImpl extends LogAnalyzerServiceGrpc.LogAnalyzerServiceImplBase {
    private final Logger logger = LoggerFactory.getLogger(LogAnalyzerServiceImpl.class);
    private final Random r = new Random();
    @Override
    public void analyzeLog(LogAnalyzerServiceOuterClass.LogPacket request,
                           StreamObserver<LogAnalyzerServiceOuterClass.AnalysisResult> responseObserver) {
        // wait for some amount of time
        int waitTime = r.nextInt(1000);
        logger.info("Sleeping for {} ms", waitTime);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Returning success response");
        LogAnalyzerServiceOuterClass.AnalysisResult response = LogAnalyzerServiceOuterClass.AnalysisResult.newBuilder()
                .setSuccess(true)
                .setMessage("Log packet processed successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
