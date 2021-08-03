package com.example.my_chatroom.serviceImpl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GameLogic {
    public BiMap<Integer, Player> playerMap;
    public BiMap<Integer, Player> spectatorMap;
    public int playerNum;
    public int spectatorNum;
    public GameStatus gameStatus;

    public GameLogic() {
        playerMap = HashBiMap.create();
        spectatorMap = HashBiMap.create();
        playerNum = 0;
        spectatorNum = 0;
        gameStatus = new GameStatus();
    }

    public int addPlayer(String name){
        int newPlayerId = -1;
        // 多人
        if (playerNum>=3 || gameStatus.gameIsOn){
            for (int i=10; i<=spectatorNum+10; i++){
                if (!spectatorMap.containsKey(i)){
                    newPlayerId = i;
                }
            }
            spectatorMap.put(newPlayerId, new Player(newPlayerId,name));
            spectatorNum++;
        }
        // playerMap的键值对是playerID——Player实例
        else{
            for (int i=0; i<=playerNum; i++){
                if (!playerMap.containsKey(i)){
                    newPlayerId = i;
                }
            }
            playerMap.put(newPlayerId,new Player(newPlayerId,name));
            gameStatus.logMap.put(newPlayerId,"");
            playerNum++;
            gameStatus.reset(0);
        }
        System.out.println("玩家人数:"+playerNum);
        playerMap.values().forEach(x->System.out.println(x.playerName));
        return newPlayerId;
    }

    public void removePlayer(int playerID) {
        if (playerID>=10){
            spectatorMap.remove(playerID);
            spectatorNum--;
        }
        else {
            if (gameStatus.gameIsOn){
                playerMap.remove(playerID);
                playerNum--;
                gameStatus.opMap.remove(playerID);
                gameStatus.chipMap.remove(playerID);
                gameStatus.btnMap.remove(playerID);
                gameStatus.queue.removeIf(s->s.equals(playerID));
            }
            else{
                playerMap.remove(playerID);
                playerNum--;
            }
        }
    }

    public String getCardName(int cardId){
        int suitNum = cardId/13;
        int num = cardId%13;
        String suitName;
        String numName;
        switch (suitNum){
            case 0 : suitName="黑桃"; break;
            case 1 : suitName="草花"; break;
            case 2 : suitName="红桃"; break;
            case 3 : suitName="方片"; break;
            default : suitName="Unknown";
        }
        if (num==8){
            numName = "0";
        }
        else if (num==9){
            numName = "J";
        }
        else if (num==10){
            numName = "Q";
        }
        else if (num==11){
            numName = "K";
        }
        else if (num==12){
            numName = "A";
        }
        else{
            numName = String.valueOf(num+2);
        }
        return suitName + numName;
    }

    // 生成对应张数的随机牌堆
    public List<Integer> generateDeck(int size){
        List<Integer> deck = new ArrayList<>();
        do {
            int newItem = (int) (Math.random() * 52);
            if (!deck.contains(newItem)) {
                deck.add(newItem);
            }
        } while (deck.size() < size);
        return deck;
    }

    // 根据牌分值判断牌型，Magic Number的狂欢！
    public String handType(int value){
        if (value == 1100012){
            return "ROYAL FLUSH";
        }
        else if (value>1100000){
            return "FLUSH STRAIGHT";
        }
        else if (value>1000000 && value<1100000){
            return "FOUR OF A KIND";
        }
        else if (value>900000 && value<1000000){
            return "FULL HOUSE";
        }
        else if (value>500000 && value<900000){
            return "FLUSH";
        }
        else if (value>400000 && value<500000){
            return "STRAIGHT";
        }
        else if (value>300000 && value<400000){
            return "THREE OF A KIND";
        }
        else if (value>200000 && value<300000){
            return "TWO PAIRS";
        }
        else if (value>100000 && value<200000){
            return "ONE PAIR";
        }
        else if (value<100000){
            return "HIGH CARD";
        }
        else {
            return "Cannot classify, the value is: " + value;
        }
    }

    // 记录游戏进程的类
    public class GameStatus{
        public HashMap<Integer, Integer> opMap;
        public HashMap<Integer, Integer> chipMap;
        public HashMap<Integer, Integer> btnMap;
        public List<Integer> deck;
        public int maxBet;
        public int roundNum;
        public int pot;
        public String publicLog;
        public HashMap<Integer, String> logMap;
        public List<Integer> queue;
        public boolean gameIsOn;

        public GameStatus() {
            opMap = new HashMap<>();
            chipMap = new HashMap<>();
            btnMap = new HashMap<>();
            maxBet = 0;
            roundNum = 0;
            pot = 0;
            publicLog = "";
            logMap = new HashMap<>();
            gameIsOn = false;
        }

        public void reset(int type){
            // type 0: 重新生成游戏
            if (type == 0){
                opMap = new HashMap<>();
                chipMap = new HashMap<>();
                btnMap = new HashMap<>();
                queue = new ArrayList<>();
                maxBet = 0;
                roundNum = 0;
                pot = 0;
                for (Integer x: playerMap.keySet()){
                    opMap.put(x, 0);
                    chipMap.put(x, 0);
                    btnMap.put(x, 0);
                    playerMap.get(x).hand.clear();
                    playerMap.get(x).handString = "hand";
                    queue.add(x);
                    deck = generateDeck(opMap.size()*2+5);
                }
            }
            // type 1: 结束一轮游戏
            else if (type == 1){
                opMap = new HashMap<>();
                chipMap = new HashMap<>();
                btnMap = new HashMap<>();
                maxBet = 0;
                roundNum = 0;
                pot = 0;
                for (Integer x: playerMap.keySet()){
                    opMap.put(x, 0);
                    chipMap.put(x, 0);
                    btnMap.put(x, 0);
                    playerMap.get(x).hand.clear();
                    playerMap.get(x).handString = "hand";
                    deck = generateDeck(opMap.size()*2+5);
                }
                int newButton = queue.get(0);
                queue.remove(0);
                queue.add(newButton);
            }
        }

        // 开始游戏时的判断
        public void start(){
            if (checkFold()){
                return;
            }
            String temp;
            if (roundNum==0){
                temp = blind();
                if (temp.indexOf("wait")==0){
                    return;
                }
                else {
                    roundNum++;
                    for (int i : opMap.keySet()){
                        if (opMap.get(i)>0){
                            opMap.put(i,0);
                        }
                    }
                    start();
                }
            }
            else if (roundNum==1){
                temp = flop();
                if (temp.indexOf("wait")==0){
                    return;
                }
                else {
                    roundNum++;
                    for (int i : opMap.keySet()){
                        if (opMap.get(i)>0){
                            opMap.put(i,0);
                        }
                    }
                    start();
                }
            }
            else if (roundNum==2){
                temp = turn();
                if (temp.indexOf("wait")==0){
                    return;
                }
                else {
                    roundNum++;
                    for (int i : opMap.keySet()){
                        if (opMap.get(i)>0){
                            opMap.put(i,0);
                        }
                    }
                    start();
                }
            }
            else if (roundNum==3){
                temp = river();
                if (temp.indexOf("wait")==0){
                    return;
                }
                else {
                    roundNum++;
                    for (int i : opMap.keySet()){
                        if (opMap.get(i)>0){
                            opMap.put(i,0);
                        }
                    }
                    finalCheck();
                }
            }
        }

        // 盲注阶段
        public String blind(){
            BiMap<Player,Integer> invPlayerMap = playerMap.inverse();
            List<Integer> tempQueue = new ArrayList<>(queue);
            int sb = tempQueue.get(0);
            tempQueue.remove(0);
            tempQueue.add(sb);
            int bb = tempQueue.get(0);
            tempQueue.remove(0);
            tempQueue.add(bb);
            if (gameStatus.pot == 0){
                gameStatus.raise(sb, 5);
                gameStatus.raise(bb, 10);
            }
            for (int playerId: tempQueue) {
                Player player = playerMap.get(playerId);
                if (player.hand.size()==0){
                    String temp;
                    int newCardId = deck.get(0);
                    deck.remove(0);
                    temp = drawCard(invPlayerMap.get(player),newCardId);
                    logMap.put(invPlayerMap.get(player),logMap.get(invPlayerMap.get(player))+temp+"\n");
                    player.handString += getCardName(newCardId);
                    newCardId = deck.get(0);
                    deck.remove(0);
                    temp = drawCard(invPlayerMap.get(player),newCardId);
                    logMap.put(invPlayerMap.get(player),logMap.get(invPlayerMap.get(player))+temp+"\n");
                    player.handString += getCardName(newCardId);
                }
                if (opMap.get(invPlayerMap.get(player))==0){
                    publicLog += "Waiting for "+playerMap.get(player.playerId).playerName+"...\n";
                    if (chipMap.get(invPlayerMap.get(player))==maxBet){
                        btnMap.put(invPlayerMap.get(player),1);
                    }
                    else if (chipMap.get(invPlayerMap.get(player))<maxBet){
                        btnMap.put(invPlayerMap.get(player),2);
                    }
                    return "wait"+player.playerId;
                }
            }
            return "pass";
        }

        // 翻牌阶段
        public String flop(){
            BiMap<Player,Integer> invPlayerMap = playerMap.inverse();
            for (int playerId: queue) {
                Player player = playerMap.get(playerId);
                if (player.hand.size() == 2) {
                    int newCardId1 = deck.get(0);
                    deck.remove(0);
                    publicLog += distributeCard(newCardId1)+"\n";
                    int newCardId2 = deck.get(0);
                    deck.remove(0);
                    publicLog += distributeCard(newCardId2)+"\n";
                    int newCardId3 = deck.get(0);
                    deck.remove(0);
                    publicLog += distributeCard(newCardId3)+"\n";
                    for (Player player1 : invPlayerMap.keySet()){
                        player1.handString += "public";
                        player1.handString += getCardName(newCardId1);
                        player1.handString += getCardName(newCardId2);
                        player1.handString += getCardName(newCardId3);
                    }
                }
                if (opMap.get(invPlayerMap.get(player))==0){
                    publicLog += "Waiting for "+playerMap.get(player.playerId).playerName+"...\n";
                    if (chipMap.get(invPlayerMap.get(player))==maxBet){
                        btnMap.put(invPlayerMap.get(player),1);
                    }
                    else if (chipMap.get(invPlayerMap.get(player))<maxBet){
                        btnMap.put(invPlayerMap.get(player),2);
                    }
                    return "wait"+player.playerId;
                }
            }
            return "pass";
        }

        // 转牌阶段
        public String turn(){
            BiMap<Player,Integer> invPlayerMap = playerMap.inverse();
            for (int playerId: queue) {
                Player player = playerMap.get(playerId);
                if (player.hand.size() == 5) {
                    int newCardId = deck.get(0);
                    deck.remove(0);
                    publicLog += distributeCard(newCardId)+"\n";
                    for (Player player1 : invPlayerMap.keySet()){
                        player1.handString += getCardName(newCardId);
                    }
                }
                if (opMap.get(invPlayerMap.get(player))==0){
                    publicLog += "Waiting for "+playerMap.get(player.playerId).playerName+"...\n";
                    if (chipMap.get(invPlayerMap.get(player))==maxBet){
                        btnMap.put(invPlayerMap.get(player),1);
                    }
                    else if (chipMap.get(invPlayerMap.get(player))<maxBet){
                        btnMap.put(invPlayerMap.get(player),2);
                    }
                    return "wait"+player.playerId;
                }
            }
            return "pass";
        }

        // 河牌阶段
        public String river(){
            BiMap<Player,Integer> invPlayerMap = playerMap.inverse();
            for (int playerId: queue) {
                Player player = playerMap.get(playerId);
                if (player.hand.size() == 6) {
                    int newCardId = deck.get(0);
                    deck.remove(0);
                    publicLog += distributeCard(newCardId)+"\n";
                    for (Player player1 : invPlayerMap.keySet()){
                        player1.handString += getCardName(newCardId);
                    }
                }
                if (opMap.get(invPlayerMap.get(player))==0){
                    publicLog += "Waiting for "+playerMap.get(player.playerId).playerName+"...\n";
                    if (chipMap.get(invPlayerMap.get(player))==maxBet){
                        btnMap.put(invPlayerMap.get(player),1);
                    }
                    else if (chipMap.get(invPlayerMap.get(player))<maxBet){
                        btnMap.put(invPlayerMap.get(player),2);
                    }
                    return "wait"+player.playerId;
                }
            }
            return "pass";
        }

        // 多人开牌阶段
        public void finalCheck(){
            List<Integer> finalList = new ArrayList<>();
            for (Integer playerId: playerMap.keySet()){
                if (opMap.get(playerId)>=0){
                    finalList.add(playerId);
                }
            }
            int maxScore = 0;
            List<Integer> winList = new ArrayList<>();
            for (Integer i: finalList){
                int temp = playerMap.get(i).handValue();
                if (temp > maxScore){
                    maxScore = temp;
                    winList.clear();
                    winList.add(i);
                }
                else if (temp == maxScore){
                    winList.add(i);
                }
            }
            if (winList.size()==1){
                playerMap.get(winList.get(0)).money += pot;
                String handType = handType(playerMap.get(winList.get(0)).handValue());
                reset(1);
                publicLog += handType+"! "+playerMap.get(winList.get(0)).playerName+" wins!\n";
            }
            else if (winList.size()>1){
                String handType = handType(playerMap.get(winList.get(0)).handValue());
                for (Integer i: winList){
                    playerMap.get(winList.get(i)).money += pot/winList.size();
                }
                reset(1);
                publicLog += handType+", draws.\n";
            }
            else{
                reset(1);
            }
            gameIsOn = false;
        }

        // 检查弃牌
        public boolean checkFold(){
            int cnt = 0;
            int winnerId = -1;
            for (int i: opMap.keySet()){
                if (opMap.get(i)>=0){
                    cnt ++;
                    winnerId = i;
                }
            }
            if (cnt>1){
                return false;
            }
            else{
                playerMap.get(winnerId).money += pot;
                reset(1);
                publicLog += playerMap.get(winnerId).playerName+" wins!\n";
                gameIsOn = false;
                return true;
            }
        }

        // 加注操作
        public void raise(int playerId, int chip){
            opMap.replaceAll((i, v) -> 0);
            opMap.put(playerId, 1);
            pot += chip;
            playerMap.get(playerId).money -= chip;
            maxBet = chipMap.get(playerId)+chip;
            chipMap.put(playerId, maxBet);
            btnMap.put(playerId,0);
            System.out.println(playerMap.get(playerId).playerName+"加注"+chip);
            publicLog += playerMap.get(playerId).playerName+"加注"+chip+"\n";
            start();
        }

        // 跟注操作
        public void call(int playerId){
            opMap.put(playerId, 1);
            int chip = maxBet-chipMap.get(playerId);
            playerMap.get(playerId).money -= chip;
            System.out.println(playerMap.get(playerId).playerName+"跟注"+(chip));
            publicLog += playerMap.get(playerId).playerName+"跟注"+chip+"\n";
            chipMap.put(playerId, maxBet);
            pot += chip;
            btnMap.put(playerId,0);
            start();
        }

        // 看牌操作
        public void check(int playerId){
            opMap.put(playerId, 1);
            btnMap.put(playerId,0);
            System.out.println(playerMap.get(playerId).playerName+"过牌");
            publicLog += playerMap.get(playerId).playerName+"过牌\n";
            start();
        }

        // 弃牌操作
        public void fold(int playerId){
            opMap.put(playerId, -1);
            btnMap.put(playerId,0);
            System.out.println(playerMap.get(playerId).playerName+"弃牌");
            publicLog += playerMap.get(playerId).playerName+"弃牌\n";
            start();
        }

        // 抽卡，把牌加入对应player实例的手牌里
        public String drawCard(Integer playerId, Integer cardId){
            System.out.println("【Game Start!】"+playerMap.get(playerId).playerName+" draws a "+getCardName(cardId)); //here
            playerMap.get(playerId).drawCard(cardId);
            return playerMap.get(playerId).playerName+" draws a "+getCardName(cardId);
        }

        // 发公用牌的方法
        public String distributeCard(Integer cardId){
            System.out.println("【Next round!】"+"Deals a "+getCardName(cardId)+" to each one.");  //here
            for (Player player : playerMap.values()){
                player.drawCard(cardId);
            }
            return "Deals a "+getCardName(cardId)+" to each one.";
        }
    }
}

// 玩家类
class Player{
    public int playerId;
    public String playerName;
    public String handString;
    public List<Integer> hand;
    public int money;

    public Player(int Id, String name) {
        this.playerId = Id;
        this.playerName = name;
        hand = new ArrayList<>();
        handString = "hand";
        money = 20000;
    }

    // 拿牌，并按顺序排列
    public void drawCard(int cardId){
        if (hand.size()>6){
            System.out.println(playerName+"'s hand is full!");
            return;
        }
        else if (hand.contains(cardId)){
            System.out.println(playerName+" already has this card!");
            return;
        }
        hand.add(cardId);
        Collections.sort(hand);
    }

    // 判断牌型，并根据牌型和踢脚牌赋分值
    public int handValue(){
        HashMap<Integer,Integer> numMap = new HashMap<>();
        HashMap<Integer,Integer> suitMap = new HashMap<>();
        Integer num;
        Integer suit;
        int handValue = 0;
        List<Integer> soloList = new ArrayList<>();
        List<Integer> pairList = new ArrayList<>();
        List<Integer> tripleList = new ArrayList<>();

        // 创建HashMap记录手牌，分别以花色和数字为键值
        for (Integer i: this.hand){
            num = i%13;
            suit = i/13;
            if (numMap.containsKey(num)){
                numMap.put(num, numMap.get(num)+1);
            }
            else{
                numMap.put(num, 1);
            }

            if (suitMap.containsKey(suit)){
                suitMap.put(suit, suitMap.get(suit)+1);
            }
            else{
                suitMap.put(suit, 1);
            }
        }

        // 记录哪些数字组成一张，一对或三条
        for (int i : numMap.keySet()){
            if (numMap.get(i)==1){
                soloList.add(i);
            }
            else if (numMap.get(i)==2){
                pairList.add(i);
            }
            else if (numMap.get(i)==3){
                tripleList.add(i);
            }
        }

        // 计算牌的value，初始值为0
        // 同花顺
        if (isFlushStraight()>0){
            return 1100000+this.isFlushStraight();
        }
        // 四条
        else if (numMap.containsValue(4)){
            soloList.addAll(pairList);
            return 1000000+Collections.max(soloList);
        }
        // 葫芦
        else if (numMap.containsValue(3) && numMap.containsValue(2)){
            handValue += 900000;
            handValue += Collections.max(tripleList)*13;
            handValue += Collections.max(pairList);
            return handValue;
        }
        // 也是葫芦，不过是两个三条产生的葫芦
        else if (tripleList.size()==2){
            handValue += 900000;
            handValue += Collections.max(tripleList)*13;
            tripleList.remove(Collections.max(tripleList));
            handValue += Collections.max(tripleList);
            return handValue;
        }
        // 同花
        else if (suitMap.containsValue(5)
                 || suitMap.containsValue(6)
                 || suitMap.containsValue(7)){
            handValue += 500000;
            int suitNum = 0;
            for (int i: suitMap.keySet()){
                if (suitMap.get(i)>=5){
                    suitNum = i;
                    break;
                }
            }
            int finalSuitNum = suitNum;
            List<Integer> temp = hand.stream()
                    .filter(x -> ( (x<13*(finalSuitNum+1)) && (x>13*finalSuitNum) ))
                    .sorted(Comparator.reverseOrder())
                    .map((Integer x)->(x%13))
                    .collect(Collectors.toList());
            handValue += temp.get(4)
                         +temp.get(3)*13
                         +temp.get(2)*13*13
                         +temp.get(1)*13*13*13
                         +temp.get(0)*13*13*13*13;
            return handValue;
        }
        // 顺子
        else if (this.isStraight(numMap)>0){
            handValue += 400000;
            return handValue+this.isStraight(numMap)*13;
        }
        // 三条
        else if (numMap.containsValue(3)){
            handValue += 300000;
            handValue += 13*13*13;
            handValue += Collections.max(tripleList)*13*13;
            handValue += Collections.max(soloList)*13;
            soloList.remove(Collections.max(soloList));
            handValue += Collections.max(soloList);
            return handValue;
        }
        // 两对
        else if (pairList.size()>1){
            handValue += 200000;
            handValue += Collections.max(pairList)*13*13;
            pairList.remove(Collections.max(pairList));
            handValue += Collections.max(pairList)*13;
            pairList.remove(Collections.max(pairList));
            pairList.addAll(soloList);
            handValue += Collections.max(pairList);
            return handValue;
        }
        // 一对
        else if (numMap.containsValue(2)){
            handValue += 100000;
            handValue += pairList.get(0)*13;
            handValue += Collections.max(soloList);
            return handValue;
        }
        // 高牌
        else{
            return Collections.max(numMap.keySet());
        }
    }

    //判断同花顺的函数
    public int isFlushStraight(){
        int cnt = 0;
        int highest = 0;
        for (int i=0; i<hand.size()-1; i++){
            if (hand.get(i)+1==hand.get(i+1)){
                cnt++;
                if (cnt>=4){
                    highest = hand.get(i+1);
                }
            }
            else {
                cnt = 0;
            }
        }
        highest = highest%13;
        if (highest<4){
            highest = 0;
        }
        if (hand.contains(0)
            && hand.contains(1)
            && hand.contains(2)
            && hand.contains(3)
            && hand.contains(12)){
            highest = 3;
        }
        else if (hand.contains(13)
                 && hand.contains(14)
                 && hand.contains(15)
                 && hand.contains(16)
                 && hand.contains(25)){
            highest = 3;
        }
        else if (hand.contains(26)
            && hand.contains(27)
            && hand.contains(28)
            && hand.contains(29)
            && hand.contains(38)){
            highest = 3;
        }
        else if (hand.contains(39)
            && hand.contains(40)
            && hand.contains(41)
            && hand.contains(42)
            && hand.contains(51)){
            highest = 3;
        }
        return highest;
    }

    // 判断顺子的函数，没顺子返回0，有顺子返回顺子中的最高牌
    public int isStraight(HashMap<Integer,Integer> numMap){
        List<Integer> temp = numMap.keySet().stream().distinct().sorted().collect(Collectors.toList());
        // 算法由cyz提供
        int cnt = 0;
        for (int i=0; i<temp.size()-1; i++){
            if (temp.get(i)+1==temp.get(i+1)){
                cnt++;
                if (cnt>=4){
                    return temp.get(i+1);
                }
            }
            else {
                cnt = 0;
            }
        }
        // A2345
        if (temp.contains(0)
            && temp.contains(1)
            && temp.contains(2)
            && temp.contains(3)
            && temp.contains(12)){
            return 3;
        }
        return 0;
    }
}
