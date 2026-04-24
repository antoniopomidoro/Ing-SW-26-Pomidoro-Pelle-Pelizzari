package it.polimi.ingsw.network.rmi;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.DTO;
import it.polimi.ingsw.network.dto.GameEventDTO;

import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Virtual view implementation that sends events to an RMI client callback.
 */
public class RMIClientHandler extends VirtualView {
    private final ClientRMIInterface clientStub;
    private final ServerManager serverManager;
    private final Runnable onDisconnect;

    /**
     * Creates a handler bound to the provided client stub.
     *
     * @param clientStub     RMI client callback
     * @param serverManager  server manager instance
     * @param onDisconnect   called before disconnect cleanup; may be {@code null}
     */
    public RMIClientHandler(ClientRMIInterface clientStub, ServerManager serverManager, Runnable onDisconnect) {
        Objects.requireNonNull(clientStub, "clientStub");
        Objects.requireNonNull(serverManager, "serverManager");
        this.clientStub = clientStub;
        this.serverManager = serverManager;
        this.onDisconnect = onDisconnect;
    }

    @Override
    protected void sendToClient(DTO dto) {
        if (dto == null) {
            return;
        }
        try {
            clientStub.receiveEvent(JacksonConfig.mapper().writeValueAsString(dto));
        } catch (RemoteException e) {
            if (onDisconnect != null) onDisconnect.run();
            serverManager.disconnectPlayer(this);
            System.err.println("[RMI] sending failed to player " + totem + ": " + e.getMessage());
        } catch (JsonProcessingException e) {
            System.err.println("[RMI] failed to serialize event: " + e.getMessage());
        }
    }

    @Override
    protected void ping() {
        try {
            clientStub.ping();
        } catch (RemoteException e) {
            if (onDisconnect != null) onDisconnect.run();
            serverManager.disconnectPlayer(this);
            System.err.println("[RMI] sending failed to player " + totem + ": " + e.getMessage());
        }
    }
}
