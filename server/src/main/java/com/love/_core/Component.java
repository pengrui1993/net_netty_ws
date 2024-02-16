package com.love._core;

public interface Component {
    enum Type{
        NULL,POSITION,COLLIDER
    }
    Component NULL = new Component() {
        @Override
        public Actor own() {
            return Actor.NULL;
        }

        @Override
        public Type type() {
            return Type.NULL;
        }
    };

    Actor own();

    Type type();
}
