package com.love.cmd;

import java.util.Objects;

public class PostRunner implements InnerCommand {

    static final PostRunner NULL = new PostRunner(()->{});
    public static PostRunner create(Runnable runner){
        if(Objects.isNull(runner))return NULL;
        return new PostRunner(runner);
    }
    public final Runnable runner;
    private PostRunner(Runnable runner) {
        this.runner = runner;
    }
    @Override
    public Type1 type1() {
        return Type1.RUN;
    }
}
