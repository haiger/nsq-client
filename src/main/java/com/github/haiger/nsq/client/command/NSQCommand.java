package com.github.haiger.nsq.client.command;

/**
 * @author haiger
 * @since 2016年12月27日 下午2:11:27
 */
public interface NSQCommand {
    String getCommandString();

    byte[] getCommandBytes();
}
