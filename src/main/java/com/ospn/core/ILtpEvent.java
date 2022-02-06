package com.ospn.core;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

public interface ILTPEvent {
    void handleMessage(ChannelHandlerContext ctx, JSONObject json);
}
