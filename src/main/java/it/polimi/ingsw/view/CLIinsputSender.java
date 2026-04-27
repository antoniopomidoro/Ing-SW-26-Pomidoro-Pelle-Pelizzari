package it.polimi.ingsw.view;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameStateDTO;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class CLIinsputSender implements Runnable {
    private boolean going;
    private final ClientManager user;
    private final CLIinterface cli;
    private final Map<String, Consumer<String[]>> handlers = new LinkedHashMap<>();

    public CLIinsputSender(ClientManager user, CLIinterface cli) {
        this.user = user;
        this.cli = cli;
        this.going = true;
        handlers.put("create",      this::lobbyCreate);
        handlers.put("enter",       this::lobbyEnter);
        handlers.put("selecttotem", this::lobbySelectTotem);
        handlers.put("topcard",     args -> pickTopCard(Integer.parseInt(args[1])));
        handlers.put("bottomcard",  args -> pickBottomCard(Integer.parseInt(args[1])));
        handlers.put("topbuild",    args -> pickTopBuilding(Integer.parseInt(args[1])));
        handlers.put("bottombuild", args -> pickBottomBuilding(Integer.parseInt(args[1])));
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
                    System.out.println("Argomento non valido (atteso numero): " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Valore non valido: " + e.getMessage());
                }
            } else {
                System.out.println("Comando non valido: '" + elements[0] + "'. Scrivi 'help' per la lista.");
            }
        }
    }

    private void printHelp() {
        System.out.println();
        System.out.println("╔══════════════════ MESOS - COMANDI ══════════════════╗");
        System.out.println("  LOBBY:");
        System.out.println("    create <numGiocatori> <totem> <nickname>");
        System.out.println("    enter <gameId> <nickname>");
        System.out.println("    selecttotem <gameId> <totem>");
        System.out.println("  PARTITA:");
        System.out.println("    topcard <i>  bottomcard <i>  topbuild <i>  bottombuild <i>  tile <i>");
        System.out.println("  Totems: " + Arrays.toString(Totem.values()));
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    private void lobbyCreate(String[] args) {
        if (args.length < 4) { System.out.println("Uso: create <numGiocatori> <totem> <nickname>"); return; }
        int numPlayers = Integer.parseInt(args[1]);
        Totem totem = Totem.valueOf(args[2].toUpperCase());
        String nick = args[3];
        user.setNickname(nick);
        user.setPlayerTotem(totem);
        String json = NUDESender.buildLobbyCreate(nick, numPlayers, totem);
        if (json != null) user.GetConnection().send(json);
    }

    private void lobbyEnter(String[] args) {
        if (args.length < 3) { System.out.println("Uso: enter <gameId> <nickname>"); return; }
        String gameId = args[1];
        String nick = args[2];
        user.setNickname(nick);
        user.setId(gameId);
        user.setPlayerTotem(null);
        String json = NUDESender.buildLobbyEnter(gameId, nick);
        if (json != null) user.GetConnection().send(json);
    }

    private void lobbySelectTotem(String[] args) {
        if (args.length < 3) { System.out.println("Uso: selecttotem <gameId> <totem>"); return; }
        String gameId = args[1];
        Totem totem = Totem.valueOf(args[2].toUpperCase());
        String nick = user.getNickname();
        if (nick == null) { System.out.println("Prima esegui 'create' o 'enter' con un nickname."); return; }
        user.setPlayerTotem(totem);
        String json = NUDESender.buildLobbySelectTotem(gameId, nick, totem);
        if (json != null) user.GetConnection().send(json);
    }

    public boolean pickTopCard(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> topCards = snapshot.getBoard().getTopCards();
        if (!isValidIndex(index, topCards.size(), "topcard")) return false;
        String cardId = snapshot.getBoard().getTopCards().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.TOP_CARD, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.GetConnection().send(message);
        return true;
    }

    public boolean pickBottomCard(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> bottomCards = snapshot.getBoard().getBottomCards();
        if (!isValidIndex(index, bottomCards.size(), "bottomcard")) return false;
        String cardId = snapshot.getBoard().getBottomCards().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.BOTTOM_CARD, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.GetConnection().send(message);
        return true;
    }

    public boolean pickTopBuilding(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> topBuildings = snapshot.getBoard().getTopBuildings();
        if (!isValidIndex(index, topBuildings.size(), "topbuild")) return false;
        String cardId = snapshot.getBoard().getTopBuildings().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.TOP_BUILDING, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.GetConnection().send(message);
        return true;
    }

    public boolean pickBottomBuilding(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> bottomBuildings = snapshot.getBoard().getBottomBuildings();
        if (!isValidIndex(index, bottomBuildings.size(), "bottombuild")) return false;
        String cardId = snapshot.getBoard().getBottomBuildings().get(index).getInstanceId();
        String message = NUDESender.build(ActionType.BOTTOM_BUILDING, index, user.getNickname(), user.getId(), user.getPlayerTotem(), cardId);
        if (message != null) user.GetConnection().send(message);
        return true;
    }

    public boolean pickTile(int index) {
        GameStateDTO snapshot = getSnapshotForAction();
        if (snapshot == null) return false;
        List<?> tiles = snapshot.getBoard().getTiles();
        if (!isValidIndex(index, tiles.size(), "tile")) return false;
        String message = NUDESender.build(ActionType.TILE, index, user.getNickname(), user.getId(), user.getPlayerTotem(), null);
        if (message != null) user.GetConnection().send(message);
        return true;
    }

    private GameStateDTO getSnapshotForAction() {
        if (cli.getState() == null || cli.getState().getSnapshot() == null || cli.getState().getSnapshot().getBoard() == null) {
            System.out.println("Stato partita non disponibile. Attendi un aggiornamento dal server.");
            return null;
        }
        if (user.getNickname() == null || user.getId() == null || user.getPlayerTotem() == null) {
            System.out.println("Identita giocatore incompleta. Completa rejoin/lobby prima di giocare.");
            return null;
        }
        GameStateDTO snapshot = cli.getState().getSnapshot();
        Totem activePlayer = snapshot.getActivePlayer();
        if (activePlayer != null && !activePlayer.equals(user.getPlayerTotem())) {
            System.out.println("Non e il tuo turno. Giocatore attivo: " + activePlayer + ".");
            return null;
        }
        return snapshot;
    }

    private boolean isValidIndex(int index, int size, String commandName) {
        if (index < 0 || index >= size) {
            System.out.println("Indice non valido per " + commandName + ": " + index + " (range 0-" + (size - 1) + ")");
            return false;
        }
        return true;
    }

    public boolean stop() {
        going = false;
        return true;
    }
}
