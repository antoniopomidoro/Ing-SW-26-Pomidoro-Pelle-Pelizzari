package it.polimi.ingsw.network;

import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.rmi.ClientRMIInterface;

import java.rmi.RemoteException;

/**
 * Virtual view implementation that sends events to an RMI client callback.
 */
public class RMIClientHandler extends VirtualView {
    private final ClientRMIInterface clientStub;

    /**
     * Creates a handler bound to the provided client stub.
     *
     * @param clientStub RMI client callback
     */
    public RMIClientHandler(ClientRMIInterface clientStub) {
        this.clientStub = clientStub;
    }

    @Override
    protected void sendToClient(GameEventDTO dto) {
        if (dto == null) {
            return;
        }

        try {
            clientStub.receiveEvent(dto);
        } catch (RemoteException e) {
            // TODO: rimuovere questa view dal registro della partita e liberare risorse.
            System.err.println("[RMI] sending failed to player " + totem + ": " + e.getMessage());
        }
    }
    @Override
    protected void ping() {
        try {
            clientStub.ping();
        } catch (RemoteException e) {
            // TODO: rimuovere questa view dal registro della partita e liberare risorse.
            System.err.println("[RMI] sending failed to player " + totem + ": " + e.getMessage());
        }
    }
}
