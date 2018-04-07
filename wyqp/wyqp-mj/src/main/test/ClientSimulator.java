import cn.worldwalker.game.wyqp.mj.robot.Client;

@SuppressWarnings("Duplicates")
public class ClientSimulator {

    private static final int waitTime = 100;


    public static void main(String[] args) throws Exception {
        for (int i=0; i<10000; i++) {
            System.out.println("..............." +  i);
            Client clientOwner = new Client(1);
            clientOwner.init();
            /*
            Thread.sleep(waitTime);
            clientOwner.entryHall();
            Thread.sleep(waitTime);
            clientOwner.createRoom();
            while (clientOwner.getRoomId() == null){
                Thread.sleep(100);
            }
            clientOwner.playerReady();
            Thread.sleep(waitTime);
            addRobot(clientOwner.getRoomId());
           */
        }
    }

    private static void addRobot(int roomId) throws Exception {

        for (int i=0; i<3; i++){
            Client client1 = new Client(i+2);
            client1.init();
            Thread.sleep(waitTime);
            client1.entryHall();
            Thread.sleep(waitTime);
            client1.entryRoom(roomId);
            while (client1.getRoomId() == null){
                Thread.sleep(waitTime);
            }
            client1.playerReady();
            Thread.sleep(waitTime);

        }
    }
}
