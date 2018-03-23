import cn.worldwalker.game.wyqp.mj.robot.Client;

@SuppressWarnings("Duplicates")
public class ClientSimulator {

    private static final int waitTime = 100;


    public static void main(String[] args) throws Exception {
        Client clientOwner = new Client(1);
        clientOwner.init();
        Thread.sleep(waitTime);
        clientOwner.entryHall();
        Thread.sleep(waitTime);
        clientOwner.createRoom();
        Thread.sleep(waitTime);
        clientOwner.playerReady();
        Thread.sleep(waitTime*10);

//        clientOwner.addRobot();

//        addRobot(clientOwner.getRoomId());

//        clientOwner.close();
//        client1.close();
//        client2.close();
//        client3.close();


    }

    private static void addRobot(int roomId) throws Exception {
        Client client1 = new Client(2);
        client1.init();
        Thread.sleep(waitTime);
        client1.entryHall();
        Thread.sleep(waitTime);
        client1.entryRoom(roomId);
        Thread.sleep(waitTime);
        client1.playerReady();

        Client client2 = new Client(3);
        client2.init();
        Thread.sleep(waitTime);
        client2.entryHall();
        Thread.sleep(waitTime);
        client2.entryRoom(roomId);
        Thread.sleep(waitTime);
        client2.playerReady();


        Client client3 = new Client(4);
        client3.init();
        Thread.sleep(waitTime);
        client3.entryHall();
        Thread.sleep(waitTime);
        client3.entryRoom(roomId);
        Thread.sleep(waitTime);
        client3.playerReady();
        Thread.sleep(waitTime);
    }
}
