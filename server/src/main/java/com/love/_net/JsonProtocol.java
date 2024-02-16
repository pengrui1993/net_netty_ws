package com.love._net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.love._api.*;
import com.love._net.rcv.*;
import com.love._net.send.RspMsgHeader;
import com.love._util.JsonUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonProtocol {
    public static RcvMsgReq en(Object req){
        return decoder.decode(String.class.cast(req));
    }
    public static Object wrapper(SendMsgBody msg,SendMsgHeader dummy){
        final Response rsp = new Response(dummy, msg);
        final String rspMsg = encoder.encode(rsp);
        return new TextWebSocketFrame(rspMsg);
    }
    public static Object wrapper(SendMsgBody msg, Session session) {
        final RcvMsgReq lastReq = session.getLastRequest();
        final RcvMsgHeader reqHeader = lastReq.header();
        final RspMsgHeader rspHeader = new RspMsgHeader(reqHeader);
        final Response rsp = new Response(rspHeader, msg);
        final String rspMsg = encoder.encode(rsp);
        return new TextWebSocketFrame(rspMsg);
    }
    private static RcvMsgBody body(JsonElement msg, Class<?extends RcvMsgBody> clazz){
        return JsonUtil.toBean(msg, clazz);
    }
    private static final Map<Integer, Function<JsonElement, RcvMsgBody>> map = new HashMap<>(){{
        put(0, msg-> body(msg, HelloMsgBody.class));
        put(1, msg-> body(msg, LoginMsgBody.class));
        put(2, msg-> body(msg, LogoutMsgBody.class));
        put(3, msg-> body(msg, PingMsgBody.class));
    }};
    interface JsonEncoder extends Protocol.Encoder<String> {}
    interface JsonDecoder extends Protocol.Decoder<String> {}
    static final Logger logger = LoggerFactory.getLogger(JsonProtocol.class);
    final static JsonEncoder encoder =JsonUtil::toJson;
    static final JsonDecoder decoder = msg -> {
        final JsonObject request = JsonUtil.toBean(msg, JsonObject.class);
        final JsonObject hd = request.get("header").getAsJsonObject();
        final int command = hd.get("cmd").getAsInt();
        final Function<JsonElement, RcvMsgBody> f = map.get(command);
        if(Objects.isNull(f)){
            logger.info("no matched command:"+command+",msg"+msg);
            return null;
        }
        final RcvMsgBody body = f.apply(request.get("body"));
        final RcvMsgReq req = new Request(JsonUtil.toBean(hd, GenericRcvMsgHeader.class),body);
        long abs = Math.abs(req.header().serverMsgTime() - req.header().clientMsgTIme());
        if(abs>1000){
//            logger.info("clock need to sync"+JsonUtil.toJson(req.header()));
        }
        return req;
    };
    static class Response implements SendMsgRsp {
        public final SendMsgHeader header;
        public final SendMsgBody body;
        public Response(SendMsgHeader sendMsgHeader, SendMsgBody msg) {
            this.header = sendMsgHeader;
            this.body = msg;
        }
    }
    static class Request implements RcvMsgReq{
        final RcvMsgHeader header;//JsonProtocol.header(hd)
        final RcvMsgBody body;
        Request(RcvMsgHeader header, RcvMsgBody body) {
            this.header = header;
            this.body = body;
        }
        @Override
        public RcvMsgHeader header() {
            return header;
        }
        @Override
        public RcvMsgBody body() {
            return body;
        }
        @Override
        public String toString() {
            return "header:"+header()+"\nbody:"+body();
        }
    }
    public static void testParse() {
        Pattern compile = Pattern.compile("\\b*\\{\\b*\"header\"\\b*:\\b*\\{\\b*\"command\"\\b*:\\b*(\\d*)}");
        String json = "{\"header\":{\"command\":100}}";
        String target = json + json + json;
        Matcher matcher = compile.matcher(target);
        while(matcher.find()){
            logger.info(matcher.group());//100
        }
    }
    public static void main(String[] args) {
        String msg = "{\"header\":{\"command\":1},\"body\":{\"uid\":0,\"mid\":0,\"clientSendTime\":0,\"serverCreateTime\":1706541733273}}";
//        logger.info(JsonUtil.toJson(new TestRequest()));
        //{"header":{"command":1},"body":{"uid":0,"mid":0,"uname":"123","pwd":"123","clientSendTime":0,"serverCreateTime":1706541733273}}
        logger.info(JsonUtil.toJson(decoder.decode(msg)));
    }
}
