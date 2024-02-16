package com.love.input;

import com.love._anno.Final;
import com.love.cmd.*;
import com.love.core.SystemQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class CommandLineReader extends Thread{
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    boolean running;
    boolean closed;
    boolean quitRequested;
    Thread runningThread;
    Thread mainThread;
    @Final
    SystemQueue lines;
    public CommandLineReader(SystemQueue queue){
        mainThread = Thread.currentThread();
        lines = queue;
        start();
    }
    @Override
    public synchronized void start() {
        if(running)return;
        super.start();
    }
    Scanner scanner = new Scanner(System.in);
    boolean info(){
        logger.info("please enter command:");
        return true;
    }
    private void read(){
        String line;
        while(!quitRequested &&info()&&null!=(line=scanner.nextLine())){
            switch (line.trim()) {
                case "quit" ->{
                    quitRequested = true;
                    lines.offer(SystemQuit.INSTANCE);
                    return;
                }
                case "win list"-> lines.offer(WindowList.INSTANCE);
                case "win new" -> {
                    logger.info("please enter window name:");
                    line = scanner.nextLine();
                    lines.offer(new WindowNew(line));
                }
                case "win info" -> {
                    logger.info("please enter window id:");
                    line = scanner.nextLine();
                    try {
                        final long id = Long.parseLong(line);
                        lines.offer(new WindowInfo(id));
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                }
                case "win del" -> {
                    logger.info("please enter window id:");
                    line = scanner.nextLine();
                    try {
                        final long id = Long.parseLong(line);
                        lines.offer(new WindowDel(id));
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                }
                case "win draw"-> lines.offer(WindowDraw.INSTANCE);
                case "win swap"-> lines.offer(WindowSwap.INSTANCE);
                default -> {
                    logger.info("unknown cmd:" + line);
                }
            }
        }
    }
    @Override
    public final void run() {
        running = true;
        closed = false;
        quitRequested = false;
        runningThread = Thread.currentThread();
        while(running&&!closed){
            try{
                read();
            }catch (Throwable t){
                t.printStackTrace(System.err);
            }
        }
    }
    public void close(){
        if(!quitRequested)return;
        if(!running)return;
        if(closed)return;
        if(Thread.currentThread()!=mainThread)return;
        closed = true;
        while(runningThread.isAlive()){
            try {
                runningThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    }

}