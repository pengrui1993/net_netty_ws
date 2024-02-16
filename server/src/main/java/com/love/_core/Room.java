package com.love._core;

import com.love._api.EventDispatcher;
import com.love._api.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Room {
    EventDispatcher roomDispatcher();
    Server server();
    boolean roomPost(Runnable run);
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Room.class);
        logger.info("Hello World");
    }
}
