package com.xxj.qqbot.util.common;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class ParamMap<K,V> extends HashMap<K,V> {

    @Override
    public V get(Object key) {
        if(key==null){
            throw new MoriBotException("指定的key["+key.toString()+"]不能为空");
        }
        V value = super.get(key);
        if(value==null){
            throw new MoriBotException("未探寻到key为["+key.toString()+"]的value值");
        }
        return value;
    }

    public V get(Object... keys) {
        V result = getWithoutThrow(keys);
        if(result==null) throw new MoriBotException("消息中没有给出需要的参数！");
        return result;
    }

    public V getOrDefault(V v,Object... keys){
        V result=null;
        for (Object key : keys) {
            if(key==null) continue;
            if((result=super.getOrDefault(key,null))!=null){
                return result;
            }
        }
        return v;
    }

    public V getWithoutThrow(Object key){
        if(key==null) return null;
        return super.get(key);
    }

    public V getWithoutThrow(Object... keys){
        V v=null;
        for (Object key : keys) {
            if(key==null) continue;
            if((v=super.getOrDefault(key,null))!=null){
                return v;
            }
        }
        return null;
    }
}
