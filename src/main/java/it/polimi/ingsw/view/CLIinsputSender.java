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
        int operation;
        Scanner sc = new Scanner(System.in);
        while(going){
            operation = sc.nextInt();
            switch (operation){
                case 1:

                case 2:

                case 3:

                case 4:

                case 5:

                default:
            }

        }

    }



    public boolean pickTopCard(int index){
         String message = NUDESender.build(ActionType.TOP_CARD,index,user.getNickname(),user.getId(),user.getId(),cli.getState().getSnapshot().getBoard().getTopCards().get(index).getInstanceId());
         user.GetConnection().send(message);
        return true;
    }




    public boolean stop(){
        going = false;
        return true;
    }
}
