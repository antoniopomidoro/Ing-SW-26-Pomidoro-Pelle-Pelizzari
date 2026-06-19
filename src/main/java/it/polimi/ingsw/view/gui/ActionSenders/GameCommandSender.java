package it.polimi.ingsw.view.gui.ActionSenders;

/**
 * Abstraction for sending game-phase actions from the GUI.
 * Implemented by {@link ActionSender}.
 */
public interface GameCommandSender {

    /**
     * Picks the top card at the given board index.
     *
     * @param index          position in the top-cards list
     * @param cardInstanceId the card's instance UUID (server-side validation)
     */
    void sendPickTopCard(int index, String cardInstanceId);

    /**
     * Picks the bottom card at the given board index.
     *
     * @param index          position in the bottom-cards list
     * @param cardInstanceId the card's instance UUID
     */
    void sendPickBottomCard(int index, String cardInstanceId);

    /**
     * Picks the top building at the given board index.
     *
     * @param index          position in the top-buildings list
     * @param cardInstanceId the building's instance UUID
     */
    void sendPickTopBuilding(int index, String cardInstanceId);

    /**
     * Picks the bottom building at the given board index.
     *
     * @param index          position in the bottom-buildings list
     * @param cardInstanceId the building's instance UUID
     */
    void sendPickBottomBuilding(int index, String cardInstanceId);

    /**
     * Places the local player's totem on the tile at the given index.
     *
     * @param index position in the tiles list
     */
    void sendPickTile(int index);
}
