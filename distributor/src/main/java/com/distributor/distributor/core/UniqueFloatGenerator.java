package com.distributor.distributor.core;

import com.google.protobuf.Message;
import org.apache.commons.codec.digest.MurmurHash3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniqueFloatGenerator {
  private static final Logger logger = LoggerFactory.getLogger(UniqueFloatGenerator.class);

  public static float generateUniqueFloat(Message message) {
    try {
      // Serialize the message to a byte array
      byte[] byteArray = message.toByteArray();

      // Calculate the MurmurHash3 hash of the byte array
      // todo figure out if this is bad on arm
      int hash = MurmurHash3.hash32x86(byteArray);

      // Normalize the hash to a float value between 0 and 1
      return normalizeHash(hash);
    } catch (Exception e) {
      logger.error("Error generating unique float from logpacket, returning 0", e);
      return 0.0f;
    }
  }

  private static float normalizeHash(int hash) {
    // Map the hash value from the full integer range to the range [0, 1]
    return (float) (hash & 0x7FFFFFFF) / 0x7FFFFFFF;
  }
}
