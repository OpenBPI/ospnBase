package com.ospn.core;

import com.alibaba.fastjson.JSONObject;
import com.ospn.common.OsnSender;
import com.ospn.common.OsnServer;
import com.ospn.data.CryptData;
import com.ospn.data.SessionData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.ospn.common.OsnUtils.logError;
import static com.ospn.common.OsnUtils.logInfo;
import static com.ospn.core.IMData.getSessionData;
import static com.ospn.core.IMData.imNotifyPort;
import static com.ospn.utils.CryptUtils.makeMessage;

public class LTPData extends OsnServer {
    public static ILTPEvent mCallback = null;
    public static OsnSender mSender = null;
    public static String ipConnector = null;
    public static int ospnServicePort = 8400;
    public static int ltpNotifyPort = 0;
    public static CryptData apps = new CryptData();

    public void init(Properties prop, ILTPEvent callback){
        mCallback = callback;
        ipConnector = prop.getProperty("ipConnector");
        if(prop.getProperty("ospnServicePort") != null){
            ospnServicePort = Integer.parseInt(prop.getProperty("ospnServicePort"));
        }
        ltpNotifyPort = Integer.parseInt(prop.getProperty("ltpNotifyPort"));
        apps.osnID = prop.getProperty("appID");
        apps.osnKey = prop.getProperty("appKey");
        logInfo("Connector: " + ipConnector);
        if(apps.osnID == null || apps.osnKey == null){
            logError("appid or appkey empty");
        }else{
            IMData.setService(apps.osnID, apps.osnKey);
        }

        AddService(ltpNotifyPort, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel arg0) {
                arg0.pipeline().addLast(new OsnServer.MessageDecoder());
                arg0.pipeline().addLast(new OsnServer.MessageEncoder());
                arg0.pipeline().addLast(new MessageHandler());
            }
        });
        mSender = OsnSender.newInstance(ipConnector, ospnServicePort, null, false, 2000, new JsonSender(), null);
        pushOsnID(apps);
    }
    public void sendJson(JSONObject json){
        mSender.send(json);
    }
    public void sendJson(String command, String to, JSONObject data){
        JSONObject json = makeMessage(command, apps.osnID, to, data, apps.osnKey, null);
        sendJson(json);
    }
    public void sendMessage(String to, JSONObject data){
        JSONObject json = makeMessage("Message", apps.osnID, to, data, apps.osnKey, null);
        sendJson(json);
    }
    public void pushOsnID(CryptData cryptData) {
        JSONObject json = new JSONObject();
        json.put("command", "pushOsnID");
        json.put("osnID", cryptData.osnID);
        mSender.send(json);
        logInfo("osnID: " + cryptData.osnID);
    }
    public void popOsnID(String osnID){
        JSONObject json = new JSONObject();
        json.put("command", "pushOsnID");
        json.put("osnID", osnID);
        mSender.send(json);
        logInfo("osnID: " + osnID);
    }
    private static class JsonSender implements OsnSender.Callback {
        public void onDisconnect(OsnSender sender, String error) {
            try {
                logInfo(sender.mIp + ":" + sender.mPort + "/" + sender.mTarget + ", error: " + error);
                Thread.sleep(2000);
                mSender = OsnSender.newInstance(sender);
            } catch (Exception ignore) {
            }
        }

        public void onCacheJson(OsnSender sender, JSONObject json) {
            logInfo("drop target: " + sender.mTarget + ", ip: " + sender.mIp);
        }

        public List<JSONObject> onReadCache(OsnSender sender, String target, int count) {
            return new ArrayList<>();
        }
    }
    private static class MessageHandler extends SimpleChannelInboundHandler<JSONObject>{
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, JSONObject json) throws Exception {
            String command = json.getString("command");
            if(command != null && command.equalsIgnoreCase("Heart")){
                ctx.writeAndFlush(json);
                return;
            }
            mCallback.handleMessage(ctx, json);
        }
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.close();
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
        }
    }
}
