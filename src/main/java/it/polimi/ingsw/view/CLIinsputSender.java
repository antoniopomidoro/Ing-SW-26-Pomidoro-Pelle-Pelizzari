package it.polimi.ingsw.view;
import java.util.Scanner;

public class CLIinsputSender implements Runnable{
    private boolean going;
    private ClientManager user;
    private CLIinterface cli;

    public CLIinsputSender(ClientManager user, CLIinterface cli){
        this.user = user;
        this.cli = cli;
        going = true;
    }



    @Override
    public void run() {
        String operation;
        Scanner sc = new Scanner(System.in);
        String[] elements;
        while(going){
            operation = sc.nextLine();
            elements = operation.split(" ");


            switch (elements[0].toLowerCase()){
                case "topcard":
                    pickTopCard(Integer.parseInt(elements[1]));

                case "bottomcard":
                    pickBottomCard(Integer.parseInt(elements[1]));

                case "topbuild":
                    pickTopBuilding(Integer.parseInt(elements[1]));

                case "bottombuild":
                    pickBottomBuilding(Integer.parseInt(elements[1]));

                case "tile":
                    pickTile(Integer.parseInt(elements[1]));

                default:
                    System.out.println("invalid command please try again");
            }

        }

    }



    public boolean pickTopCard(int index){
         String message = NUDESender.build(ActionType.TOP_CARD,index,user.getNickname(),user.getId(),user.getId(),cli.getState().getSnapshot().getBoard().getTopCards().get(index).getInstanceId());
         user.GetConnection().send(message);
        return true;
    }

    public boolean pickBottomCard(int index){
        String message = NUDESender.build(ActionType.BOTTOM_CARD,index,user.getNickname(),user.getId(),user.getId(),cli.getState().getSnapshot().getBoard().getTopCards().get(index).getInstanceId());
        user.GetConnection().send(message);
        return true;
    }

    public boolean pickTopBuilding(int index){
        String message = NUDESender.build(ActionType.TOP_BUILDING,index,user.getNickname(),user.getId(),user.getId(),cli.getState().getSnapshot().getBoard().getTopCards().get(index).getInstanceId());
        user.GetConnection().send(message);
        return true;
    }

    public boolean pickBottomBuilding(int index){
        String message = NUDESender.build(ActionType.BOTTOM_BUILDING,index,user.getNickname(),user.getId(),user.getId(),cli.getState().getSnapshot().getBoard().getTopCards().get(index).getInstanceId());
        user.GetConnection().send(message);
        return true;
    }

    public boolean pickTile(int index){
        String message = NUDESender.build(ActionType.TILE,index,user.getNickname(),user.getId(),user.getId(),cli.getState().getSnapshot().getBoard().getTopCards().get(index).getInstanceId());
        user.GetConnection().send(message);
        return true;
    }






    public boolean stop(){
        going = false;
        return true;
    }
}
