package com.love.core;

import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL11.*;

public interface Window {
    void close();
    default void render(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
    }
    default void setting(){
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
    }
    class Holder{
        static float angle=0;
        static{
            GL45.glBufferStorage(0,0,0);
        }
    }
    default void rotateTriangle(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glShadeModel(GL_SMOOTH);
//        glShadeModel(GL_FLAT);
        glPolygonMode(GL_FRONT,GL_FILL);
        glPolygonMode(GL_BACK,GL_LINE);
        glMatrixMode(GL_MODELVIEW_MATRIX);
        glLoadIdentity();
        glPushMatrix();
            glTranslatef(0.5f,0,0);
            Holder.angle+=0.8f;
            glRotatef(Holder.angle,0,1,0);
            glBegin(GL_TRIANGLES);
                glVertex2f(-0.5f,-0.5f);
                glColor3f(1,0,0);
                glVertex2f(0.5f,-0.5f);
                glColor3f(0,1,0);
                glVertex2f(0,0.5f);
                glColor3f(0,0,1);
            glEnd();
        glPopMatrix();
    }

    default void renderTriangle(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glShadeModel(GL_SMOOTH);
        glPolygonMode(GL_BACK,GL_LINE);
        glMatrixMode(GL_MODELVIEW_MATRIX);
        glLoadIdentity();
        glPushMatrix();
            glBegin(GL_TRIANGLES);
                glVertex2f(-0.5f,-0.5f);
                glColor3f(1,0,0);
                glVertex2f(0.5f,-0.5f);
                glColor3f(0,1,0);
                glVertex2f(0,0.5f);
                glColor3f(0,0,1);
            glEnd();
        glPopMatrix();
    }

}
