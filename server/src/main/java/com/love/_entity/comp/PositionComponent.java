package com.love._entity.comp;

import com.love._core.Actor;
import com.love._core.Component;

public class PositionComponent implements Component {
    final Actor act;
    float x,z,y;
    public PositionComponent(Actor act) {
        this.act = act;
    }
    @Override
    public Actor own() {
        return act;
    }
    @Override
    public Type type() {
        return Type.POSITION;
    }
    public float x(){ return x;}
    public float z(){ return z;}
    public float y(){ return y;}
}
