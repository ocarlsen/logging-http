package com.ocarlsen.logging.http.format;

public interface HeaderFormatter<T> {
    String format(T headers);
}
