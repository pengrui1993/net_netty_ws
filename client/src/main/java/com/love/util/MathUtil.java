package com.love.util;

import org.joml.Matrix4f;

public class MathUtil {


    public static void m(){
        Matrix4f result = new Matrix4f();
        float px = 1,py = 2,pz = 3;
        float rx = 0,ry = 90,rz = 0;
        float sx = 1,sy = 1,sz = 1;
        result.translate(px,py,pz);
        result.rotate(rx, 1, 0, 0);
        result.rotate(ry, 0, 1, 0);
        result.rotate(rz, 0, 0, 1);
        result.scale(sx,sy,sz);
        float[] data = new float[16];
        result.get(data);//store data into buffer from matrix

        for(int col = 0;col<4;col++){
            for(int row =0;row<4;row++){
                System.out.print(data[row*4+col]+" ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        m();
    }
}
