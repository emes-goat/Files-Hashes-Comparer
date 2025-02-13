package com.emes;

import java.nio.file.Path;

public record HashedFile(Path path, String hash, Long size) {

}
