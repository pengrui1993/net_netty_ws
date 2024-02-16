package com.love._util;

import com.love._api.Api;

import java.util.Optional;

public class ClockRunner {
    static final Runnable NULL = ()->{};
    final Runnable run;
    final int duration;
    long last;
    final boolean first;
    public ClockRunner(Runnable run,int dur,boolean first){
        this.run = Optional.ofNullable(run).orElse(NULL);
        this.duration = Math.max(dur, 10);
        this.first = first;
        this.last = first?now()-dur:now();
    }
    public ClockRunner(Runnable run,int dur){
        this(run,dur,true);
    }
    public ClockRunner(Runnable run){
        this(run,1000,false);
    }
    public void tick(){
        long tmp = now();
        if(tmp-last>=duration){
            last = tmp;
            run.run();
        }
    }
    long now(){ return Api.now();}
}
