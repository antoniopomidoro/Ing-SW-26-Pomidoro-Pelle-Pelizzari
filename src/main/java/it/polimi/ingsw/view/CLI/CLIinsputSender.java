package it.polimi.ingsw.view.CLI;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.view.ActionType;
import it.polimi.ingsw.view.ClientManager;
import it.polimi.ingsw.view.NUDESender;
import it.polimi.ingsw.view.gui.ActionSenders.ActionSender;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

//handles lobby and inputs by the user
public class CLIinsputSender implements Runnable {
    private boolean going;
    private final ClientManager user;
    private final CLIinterface cli;
    private final ActionSender actionSender;
    private final Map<String, Consumer<String[]>> handlers = new LinkedHashMap<>();
    private String gameid;

    public CLIinsputSender(ClientManager user, CLIinterface cli) {
        this.user = user;
        this.cli = cli;
        this.actionSender = new ActionSender(user);
        this.going = true;
        handlers.put("create",      this::lobbyCreate);
        handlers.put("enter",       this::lobbyEnter);
        handlers.put("selecttotem", this::lobbySelectTotem);
        handlers.put("topcard",     args -> pickTopCard(Integer.parseInt(args[1])));
        handlers.put("bottomcard",  args -> pickBottomCard(Integer.parseInt(args[1])));
        handlers.put("topbuild",    args -> pickTopBuilding(Integer.parseInt(args[1])));
        handlers.put("bottombuild", args -> pickBottomBuilding(Integer.parseInt(args[1])));
        handlers.put("view",        args -> cli.viewPlayerHand(Totem.valueOf(args[1].toUpperCase()), getSnapshotForAction()));
        handlers.put("tile",        args -> pickTile(Integer.parseInt(args[1])));
        handlers.put("help",        args -> printHelp());
    }

    @Override
    public void run() {
        printHelp();
        Scanner sc = new Scanner(System.in);
        while (going) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] elements = line.split(" ");
            Consumer<String[]> handler = handlers.get(elements[0].toLowerCase());
            if (handler != null) {
                try {
                    handler.accept(elements);
                } catch (NumberFormatException e) {
                    System.out.println("invalid argument " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("invalid value: " + e.getMessage());
                }
            } else {
                System.out.println("invalid command: '" + elements[0] + "'. write 'help' for list.");
            }
        }
    }

    private void printHelp() {
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════ MESOS - COMMANDS ═════════════════════════════════════════════════════════╗");
        System.out.println("  LOBBY:");
        System.out.println("\t- create <playersNumber> <totem> <nickname>");
        System.out.println("\t- enter <gameId> <nickname>");
        System.out.println("\t- selecttotem <gameId> <totem>");
        System.out.println("  GAME:");
        System.out.println("\t- topcard <index>\n" + "\t- bottomcard <index>\n" + "\t- topbuild <index>\n" + "\tbottombuild <index>\n" + "\ttile <index>");
        System.out.println("\tTotems: " + Arrays.toString(Totem.values()));

        System.out.println("  EXAMPLES:\n" +
                "\t> create 2 red Fabrizio (create a 2 player lobby with red totem and nick Fabrizio).\n" +
                "\t> enter 123456 Cugola (enter in the 123456 lobby with nickname Cugola, when you enter in a lobby you don't have a totem yet).\n" +
                "\t> selecttotem red (after joining select the totem) \n" +
                "\t> <card function> 3 (select the card of the selected line)\n" +
                "\t> tile 1 (place your totem on the selected tile)\n" +
                "\t> view yellow (view the hand of the yellow player)\n");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝");
    }

    private void lobbyCreate(String[] args) {
        if (args.length < 4) { System.out.println("Usage: create <numGiocatori> <totem> <nickname>"); return; }
        int numPlayers = Integer.parseInt(args[1]);
        Totem totem = Totem.valueOf(args[2].toUpperCase());
        String nick = args[3];
        actionSender.sendCreateGame(nick, numPlayers, totem);
    }

    private void lobbyEnter(String[] args) {
        if (args.length < 3) { System.out.println("Usage: enter <gameId> <nickname>"); return; }
        gameid = args[1];
        String nick = args[2];
        actionSender.sendJoinGame(gameid, nick);
    }

    private void lobbySelectTotem(String[] args) {
        if (args.length < 2) { System.out.println("Usage: selecttotem  <totem>"); return; }
        Totem totem = Totem.valueOf(args[1].toUpperCase());
        String nick = user.getNickname();
        if (nick == null) { System.out.println("create or enter in a lobby first"); return; }
        user.setId(gameid);
        actionSender.sendSelectTotem(totem);
    }

    public boolean pickTopCard(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> topCards = snapshot.getBoard().getTopCards();
        if (!isValidIndex(index, topCards.size(), "topcard")) return false;
        String cardId = snapshot.getBoard().getTopCards().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.TOP_CARD, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.getConnection().send(message);
        return true;
    }

    public boolean pickBottomCard(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> bottomCards = snapshot.getBoard().getBottomCards();
        if (!isValidIndex(index, bottomCards.size(), "bottomcard")) return false;
        String cardId = snapshot.getBoard().getBottomCards().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.BOTTOM_CARD, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.getConnection().send(message);
        return true;
    }

    public boolean pickTopBuilding(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> topBuildings = snapshot.getBoard().getTopBuildings();
        if (!isValidIndex(index, topBuildings.size(), "topbuild")) return false;
        String cardId = snapshot.getBoard().getTopBuildings().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.TOP_BUILDING, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.getConnection().send(message);
        return true;
    }

    public boolean pickBottomBuilding(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> bottomBuildings = snapshot.getBoard().getBottomBuildings();
        if (!isValidIndex(index, bottomBuildings.size(), "bottombuild")) return false;
        String cardId = snapshot.getBoard().getBottomBuildings().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.BOTTOM_BUILDING, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.getConnection().send(message);
        return true;
    }

    public boolean pickTile(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> tiles = snapshot.getBoard().getTiles();
        if (!isValidIndex(index, tiles.size(), "tile")) return false;
        String message = NUDESender.build(ActionType.TILE, index, user.getNickname(), user.getId(), user.getPlayerTotem(), null);
        if (message != null) user.getConnection().send(message);
        return true;
    }

    private GameStateDTO getSnapshotForAction() {
        if (cli.getState() == null || cli.getState().getSnapshot() == null || cli.getState().getSnapshot().getBoard() == null) {
            System.out.println("state is not avaiable wait for server response.");
            return null;
        }
        if (user.getNickname() == null || user.getId() == null || user.getPlayerTotem() == null) {
            System.out.println("Identita giocatore incompleta. Completa rejoin/lobby prima di giocare.");
            return null;
        }
        GameStateDTO snapshot = cli.getState().getSnapshot();
        Totem activePlayer = snapshot.getActivePlayer();
        if (activePlayer != null && !activePlayer.equals(user.getPlayerTotem())) {
            System.out.println("not your turn, active player: " + activePlayer + ".");
            return null;
        }
        return snapshot;
    }

    private boolean isValidIndex(int index, int size, String commandName) {
        if (index < 0 || index >= size) {
            System.out.println("invalid index  " + commandName + ": " + index + " (range 0-" + (size - 1) + ")");
            return false;
        }
        return true;
    }

    public boolean stop() {
        going = false;
        return true;
    }
}
