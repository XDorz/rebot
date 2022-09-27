package com.xxj.qqbot.util.common.redis;

import com.xxj.qqbot.util.common.redis.children.HashUtil;
import com.xxj.qqbot.util.common.redis.children.KeyUtil;
import com.xxj.qqbot.util.common.redis.children.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    @Autowired
    public KeyUtil keyUtil;

    @Autowired
    public StringUtil stringUtil;

    @Autowired
    public HashUtil hashUtil;
}
