package com.love._api;

public enum Api {
    WS_GATE(1, Api.WS_GATE_PATH),
    HTTP_HELLO(1000, Api.HTTP_HELLO_PATH);
    ;
    public final int id;
    public final String path;
    public static final String API_PREFIX = "/api/";
    public static final String WS_PREFIX = API_PREFIX+"ws/";
    public static final String WS_GATE_PATH = WS_PREFIX+"gate";

    public static final String HTTP_PREFIX = API_PREFIX+"http/";
    public static final String HTTP_HELLO_PATH = HTTP_PREFIX+"hello";
    Api(int id, String path) {
        this.id = id;
        this.path = path;
    }
    public static long now(){ return System.currentTimeMillis();}
}
