package com.love._api;

/**
 * client to server
 */
public interface RcvMsgHeader {
    int cmd();
    long serverMsgTime();
    long clientMsgTIme();
    int sequenceId();
    int requestAnswerMatchId();
    default long now(){ return Api.now();}

}
