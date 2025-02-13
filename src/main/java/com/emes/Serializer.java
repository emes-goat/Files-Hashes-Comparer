package com.emes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Serializer {

  private final ObjectMapper mapper = new ObjectMapper();

  public byte[] serialize(List<HashedFile> hashedFiles) throws IOException {
    return mapper.writeValueAsString(hashedFiles).getBytes(StandardCharsets.UTF_8);
  }

  public List<HashedFile> deserialize(byte[] encoded) throws JsonProcessingException {
    var decoded = new String(encoded, StandardCharsets.UTF_8);
    return mapper.readValue(decoded, new TypeReference<>() {
    });
  }
}
