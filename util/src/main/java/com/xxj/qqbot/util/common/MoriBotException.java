package com.xxj.qqbot.util.common;

public class MoriBotException extends RuntimeException {

    public MoriBotException(){
        super();
    }

    public MoriBotException(String message){
        super(message);
    }

    public MoriBotException(String message,Throwable e){
        super(message,e);
    }
}
