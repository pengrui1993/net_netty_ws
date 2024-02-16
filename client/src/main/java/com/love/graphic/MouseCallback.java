package com.love.graphic;

import java.util.List;

public interface MouseCallback {
    default void onCursorEnter(boolean entered){};
    default void onCursorPos(double x,double y){}
    default void onMouseButton(int button, int action, int mods){}
    default void onScroll(double x, double y){}//double xoffset, double yoffset
    default void onDrop(int count,long name){}
    default void onDrop(List<String> files){}
}
