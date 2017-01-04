package com.github.haiger.nsq.client;

import org.junit.Test;

public class TestStringFormat {

    @Test
    public void testFormatByte() {
        System.out.println(String.format("%s %s\n", "FIN", new String("123".getBytes())));
    }

}
