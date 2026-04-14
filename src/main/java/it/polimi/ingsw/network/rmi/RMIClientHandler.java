package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.GameEventDTO;

import java.rmi.RemoteException;

/**
 * Virtual view implementation that sends events to an RMI client callback.
 */
public class RMIClientHandler extends VirtualView {
    private final ClientRMIInterface clientStub;
    private final ServerManager serverManager;
    /**
     * Creates a handler bound to the provided client stub.
     *
     * @param clientStub RMI client callback
     */
    public RMIClientHandler(ClientRMIInterface clientStub, ServerManager serverManager) {
        this.clientStub = clientStub;
        this.serverManager = serverManager;
    }

    @Override
    protected void sendToClient(GameEventDTO dto) {
        if (dto == null) {
            return;
        }

        try {
            clientStub.receiveEvent(dto);
        } catch (RemoteException e) {
            serverManager.disconnectPlayer(this);
            System.err.println("[RMI] sending failed to player " + totem + ": " + e.getMessage());
        }
    }
    @Override
    protected void ping() {
        try {
            clientStub.ping();
        } catch (RemoteException e) {
            serverManager.disconnectPlayer(this);
            System.err.println("[RMI] sending failed to player " + totem + ": " + e.getMessage());
        }
    }
}
