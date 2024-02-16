package com.love._entity.comp;

import com.love._core.Actor;
import com.love._core.Component;

public class ColliderComponent implements Component {
    final Actor act;
    public ColliderComponent(Actor act) {
        this.act = act;
    }
    @Override
    public Actor own() {
        return act;
    }
    @Override
    public Type type() {
        return Type.COLLIDER;
    }
}
