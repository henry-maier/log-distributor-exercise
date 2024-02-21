package com.distributor.distributor.api;

import com.distributor.distributor.core.LogDistributorService;
import com.distributor.distributor.data.LogPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DistributeController {

  private static final Logger logger = LoggerFactory.getLogger(DistributeController.class);

  @Autowired private LogDistributorService logDistributorService;

  @GetMapping("/hello")
  public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
    logger.info("Received request for hello with name: {}", name);
    return String.format("Hello %s!", name);
  }

  @PostMapping("/distribute")
  public ResponseEntity<String> receiveLog(@RequestBody LogPacket logPacket) {
    // Process the received log packet
    logger.info("Received log packet of size: {}", logPacket.logMessages().size());
    logDistributorService.distributeLog(logPacket);

    // Respond with a success message
    return ResponseEntity.status(HttpStatus.OK).body("Log packet received successfully.");
  }
}
