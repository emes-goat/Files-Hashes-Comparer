package com.emes;

import java.time.Instant;

public record HashedFile(
    String path,
    String hash,
    Instant timestamp
) {

}