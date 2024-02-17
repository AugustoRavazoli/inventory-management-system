package io.github.augustoravazoli.inventorymanagementsystem.util;

import java.io.ByteArrayOutputStream;

public record Document(String filename, ByteArrayOutputStream content, long size) {

}
