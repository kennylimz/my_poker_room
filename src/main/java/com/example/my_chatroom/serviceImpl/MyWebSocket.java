package com.example.my_chatroom.serviceImpl;

import com.example.my_chatroom.serviceImpl.GameLogic;
import com.example.my_chatroom.bean.UserInfo;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *  * @Description: websocket的具体实现类
 *  * 使用springboot的唯一区别是要@Component声明下，而使用独立容器是由容器自己管理websocket的，
 *  * 但在springboot中连容器都是spring管理的。
 *     虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean，
 *     所以可以用一个静态set保存起来。
 */
@ServerEndpoint(value = "/websocket/{nickName}")
@Component
public class MyWebSocket {
    //用来存放每个客户端对应的MyWebSocket对象。

    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    private static Map<Session, UserInfo> connectmap = new HashMap<>();
    private static BiMap<Integer, Session> IDtoSession = HashBiMap.create();
    private static GameLogic gameLogic = new GameLogic();

    //用session作为key，保存用户信息
    //与某个客户端的连接会话，需要通过它来给客户端发送数据

    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("nickName") String nickName) {
        int playerId;
        this.session = session;
        UserInfo userInfo = new UserInfo(session.getId(), nickName);
        connectmap.put(session, userInfo);
        webSocketSet.add(this);
        playerId = gameLogic.addPlayer(nickName);
        IDtoSession.put(playerId,session);
        System.out.println(nickName+"上线了！当前在线人数为" + webSocketSet.size());
        //群发消息，告诉每一位
        if (playerId>5){
            broadcastLog(nickName+"上线了！编号为："+playerId+"（旁观者）");
        }
        else{
            broadcastLog(nickName+"上线了！编号为："+playerId);
            updateStatus(playerId);
        }
        broadcastLog("当前在线人数为："+webSocketSet.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        String nickName=connectmap.get(session).getNickName();
        connectmap.remove(session);
        webSocketSet.remove(this);
        BiMap<Session, Integer> SessiontoID = IDtoSession.inverse();
        int playerID = SessiontoID.get(session);
        SessiontoID.remove(session);
        gameLogic.removePlayer(playerID);
        System.out.println(nickName+"下线了！当前在线人数为" + webSocketSet.size());
        //群发消息，告诉每一位
        broadcastLog(nickName+"下线，当前在线人数为："+webSocketSet.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * */
    @OnMessage
    public void onMessage(String message, Session session) {
        if (message.indexOf("ins")==0){
            //获得指令
            String instruction = message.substring(3,message.length());
            BiMap<Session,Integer> SessiontoID = IDtoSession.inverse();
            int playerId = SessiontoID.get(session);
            System.out.println("来自"+connectmap.get(session).getNickName()+"的指令: " + instruction);
            instructionHandler(playerId, instruction);
        }
        else if(message.indexOf("cha")==0){
            message = message.substring(3,message.length());
            String nickName=connectmap.get(session).getNickName();
            System.out.println("来自"+nickName+"的消息: " + message);
            //群发消息
            broadcastMsg(nickName+"："+message);
        }
        else if (message.indexOf("ope")==0){
            String operation = message.substring(3,message.length());
            BiMap<Session,Integer> SessiontoID = IDtoSession.inverse();
            int playerId = SessiontoID.get(session);
            System.out.println("来自"+connectmap.get(session).getNickName()+"的操作: " + operation);
            operationHandler(playerId,operation);
        }
    }

    /**
     * 发生错误时调用
     *
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }
    /**
     * 群发自定义消息
     * */
    public void broadcastMsg(String message)  {
        message = "msg"+message;
        for (MyWebSocket item : webSocketSet) {
            try {
                Thread.sleep((long) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
            item.session.getAsyncRemote().sendText(message);
            //异步发送消息.
        }
    }
    /**
     * 单发自定义消息
     * */
    public void privateMsg(int playerId, String message)  {
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        message = "msg"+message;
        //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
        IDtoSession.get(playerId).getAsyncRemote().sendText(message);
        //异步发送消息.
    }
    /**
     * 群发日志
     */
    public void broadcastLog(String message)  {
        message = "log"+message;
        for (MyWebSocket item : webSocketSet) {
            try {
                Thread.sleep((long) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
            item.session.getAsyncRemote().sendText(message);
            //异步发送消息.
        }
    }
    /**
     * 单发自定义消息
     * */
    public void privateLog(int playerId, String message)  {
        try {
            Thread.sleep((long) (Math.random() * 200));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        message = "log"+message;
        //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
        IDtoSession.get(playerId).getAsyncRemote().sendText(message);
        //异步发送消息.
    }
    /**
     * 更新状态栏
     */
    public void updateStatus(int playerId) {
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int status = gameLogic.gameStatus.btnMap.get(playerId);
        String message = "";
        message += "id"+playerId;
        message += "money"+gameLogic.playerMap.get(playerId).money;
        message += "pot"+gameLogic.gameStatus.pot;
        message += "type"+status;
        message += gameLogic.playerMap.get(playerId).handString;
        message = "sta"+message;
        //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
        IDtoSession.get(playerId).getAsyncRemote().sendText(message);
        //异步发送消息.
    }
    /**
     * 处理指令
     */
    public void instructionHandler(int instructor, String instruction){
        if (instruction.indexOf("sim")==0){
            gameLogic.gameStatus.roundSim();
        }
        else if (instruction.indexOf("list")==0){
            Session session = IDtoSession.get(instructor);
            for (int i : IDtoSession.keySet()){
                privateLog(instructor,i+"——"+connectmap.get(IDtoSession.get(i)).getNickName());
            }
        }
        else if (instruction.indexOf("start")==0){
            gameLogic.gameStatus.start();
            for (int playerId: gameLogic.playerMap.keySet()){
                updateStatus(playerId);
            }
        }
        else if (instruction.indexOf("help")==0){
            broadcastLog("list——玩家列表");
            broadcastLog("start——开始一轮");
        }
        printLog();
    }
    /**
     * 处理操作
     */
    public void operationHandler(int operator, String instruction){
        if (instruction.equals("raise")){
            int raiseNum = gameLogic.gameStatus.maxBet+100;
            gameLogic.gameStatus.raise(operator,raiseNum);
        }
        else if (instruction.equals("call")){
            gameLogic.gameStatus.call(operator);
        }
        else if (instruction.equals("check")){
            gameLogic.gameStatus.check(operator);
        }
        else if (instruction.equals("fold")){
            gameLogic.gameStatus.fold(operator);
        }
        for (int i: gameLogic.playerMap.keySet()){
            updateStatus(i);
        }
        printLog();
    }

    /**
     * 打印日志
     */
    public void printLog(){
        for (int i: gameLogic.gameStatus.logMap.keySet()){
            String[] privateArray = gameLogic.gameStatus.logMap.get(i).split("\n");
            for (String string : privateArray){
                if (string.length()!=0){
                    privateLog(i, string);
                }
            }
            gameLogic.gameStatus.logMap.put(i,"");
        }
        String[] publicArray = gameLogic.gameStatus.publicLog.split("\n");
        for (String string : publicArray){
            if (string.length()!=0){
                broadcastLog(string);
            }
        }
        gameLogic.gameStatus.publicLog = "";
    }
}