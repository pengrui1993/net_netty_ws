package com.love._core;

import java.util.Objects;

public interface Actor{
    Actor NULL = comp -> Component.NULL;
    enum Type{
        NULL,PLAYER,BOX,BULLET,ITEM,TERRAIN,WALL
    }
    default Type type(){return Type.NULL;}
    Component get(Component.Type type);
    default boolean has(Component.Type type){return Objects.nonNull(get(type));}
}
