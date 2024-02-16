package com.love;

import com.love._api.CommandLine;
import com.love._core.Event;
import com.love._evt.CmdLineMsgRcvEvent;
import com.love._evt.SysQuitEvent;
import com.love._net.NetManager;
import com.love._stdin.ConnUserList;
import com.love._stdin.NoImpl;
import com.love._stdin.UidUserList;
import com.love._stdin.UserDeep;
import com.love._util.LogTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Starter extends GameServer implements CommandLine,Runnable {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    public final Thread worker = new Thread(this);
    Starter(){
        worker.setDaemon(true);
        worker.start();
    }
    private void postEmit(Event event){
        post(()-> dispatcher().emit(event));
    }
    private void postEmitCmd(CommandLine.Command cmd){
        postEmit(new CmdLineMsgRcvEvent(cmd));
    }
    private void ep(Runnable r,String msg){
        try {
            r.run();
        }catch (Throwable t){
            System.out.println(msg);
        }
    }
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true){
            final String cmd = scanner.nextLine();
            switch (cmd.trim()){
                case "quit"-> postEmit(new SysQuitEvent(this));
                case "uid list"-> postEmitCmd(UidUserList.INSTANCE);
                case "conn list"-> postEmitCmd(ConnUserList.INSTANCE);
                case "user deep"-> ep(()->{
                    System.out.println("print enter uid:");
                    postEmitCmd(UserDeep.INSTANCE.reuse(Integer.parseInt(scanner.nextLine().trim())));
                },"invalid uid");
                case""->{}//ignore
                default -> {
                    logger.info("unknown cmd:{}\n please enter [quit,uid list,conn list,user deep]",cmd);
                    postEmitCmd(new NoImpl(cmd));
                }
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("server start...");
        LogTrace.enable("launch");
        new Starter().start();
        LogTrace.disable();
        System.out.println("server done...");
    }

}
