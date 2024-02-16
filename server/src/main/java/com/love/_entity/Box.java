package com.love._entity;

import com.love._entity.comp.PositionComponent;
import com.love._core.Actor;
import com.love._core.Component;

public class Box implements Actor {
    PositionComponent position;

    @Override
    public Component get(Component.Type type) {
        return null;
    }
}
