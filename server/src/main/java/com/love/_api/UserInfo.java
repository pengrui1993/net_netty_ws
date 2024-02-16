package com.love._api;



public interface UserInfo {
    int id();
    default long now(){ return Api.now();}
    default String pwd(){ return "";}
}
