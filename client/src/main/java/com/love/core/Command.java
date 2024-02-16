package com.love.core;

public interface Command {
    enum Type{CONSOLE,NET,INNER}
    Type type();
}
