package com.xxj.qqbot.util.common;

public class MoriException extends Exception{

    public MoriException(){
        super();
    }

    public MoriException(String message){
        super(message);
    }

    public MoriException(String message,Throwable e){
        super(message,e);
    }
}
