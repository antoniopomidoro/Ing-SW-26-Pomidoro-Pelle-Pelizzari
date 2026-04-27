package it.polimi.ingsw.view;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;

import java.util.List;
import java.util.Map;

public class CLIinterface implements UserInterface, Runnable {

    // --- ANSI helpers (no library needed) ---
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";
    private static final String MAG    = "\u001B[35m";

    private final ClientManager user;
    private boolean going;
    private GameEventDTO state;

    public CLIinterface(ClientManager user) {
        this.user = user;
        this.going = true;
        new Thread(new CLIinsputSender(user, this), "CLI-input").start();
        new Thread(this, "CLI-renderer").start();
    }

    @Override
    public boolean setUp(GameEventDTO state) {
        this.state = state;
        return true;
    }

    @Override
    public synchronized boolean update(GameEventDTO state) {
        this.state = state;
        notifyAll();
        return true;
    }


    public synchronized boolean updateLobby(LobbyUpdateDTO lobbyUpdate) {
        printLobby(lobbyUpdate);
        notifyAll();
        return true;
    }


    public synchronized boolean showTotemSelection(TotemSelectionDTO totemSelection) {
        printTotemSelection(totemSelection);
        notifyAll();
        return true;
    }


    public synchronized boolean showError(ErrorDTO error) {
        System.out.println();
        System.out.println(RED + BOLD + "  ✖ ERRORE: " + error.getErrorCode().name() + RESET);
        System.out.println();
        return true;
    }

    @Override
    public void run() {
        while (going) {
            print();
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // ============================================================
    //                    GAME STATE PRINT
    // ============================================================
    public boolean print() {
        if (state == null || state.getSnapshot() == null) return false;

        GameStateDTO snap = state.getSnapshot();
        System.out.flush();
        clearScreen();
        printGameHeader(snap);
        printBoard(snap);
        printPlayers(snap);
        printSelfHand(snap);
        printPrompt();
        return true;
    }

    private void printGameHeader(GameStateDTO snap) {
        String banner =
                BOLD + CYAN +
                        "╔══════════════════════════════════════════════════════════════════╗\n" +
                        "║                 M E S O S   —   Cranio Games                     ║\n" +
                        "╚══════════════════════════════════════════════════════════════════╝" +
                        RESET;
        System.out.println(banner);

        System.out.printf("%s Game:%s %s   %sAge:%s %s   %sTurn:%s %d   %sPhase:%s %s   %sActive:%s %s%n",
                BOLD, RESET, safe(snap.getGameId()),
                BOLD, RESET, snap.getAge() == null ? "-" : snap.getAge().name(),
                BOLD, RESET, snap.getTurn(),
                BOLD, RESET, safe(snap.getCurrentPhaseName()),
                BOLD, RESET, snap.getActivePlayer() == null ? "-" : snap.getActivePlayer().name());
        System.out.println(DIM + "──────────────────────────────────────────────────────────────────" + RESET);
    }

    private void printBoard(GameStateDTO snap) {
        var board = snap.getBoard();
        if (board == null) {
            System.out.println(DIM + "(board non disponibile)" + RESET);
            return;
        }

        System.out.println(BOLD + YELLOW + "▼ TOP CARDS" + RESET);
        printCardsRow(board.getTopCards());

        System.out.println(BOLD + YELLOW + "▼ BOTTOM CARDS" + RESET);
        printCardsRow(board.getBottomCards());

        System.out.println(BOLD + GREEN + "▼ TOP BUILDINGS" + RESET);
        printBuildingsRow(board.getTopBuildings());

        System.out.println(BOLD + GREEN + "▼ BOTTOM BUILDINGS" + RESET);
        printBuildingsRow(board.getBottomBuildings());

        System.out.println(BOLD + MAG + "▼ TILES" + RESET);
        printTilesRow(board.getTiles());
        System.out.println();
    }

    private void printCardsRow(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            System.out.println(DIM + "  (vuoto)" + RESET);
            return;
        }
        // Render each card as a 16-wide, 5-line "card box".
        int n = cards.size();
        String[][] lines = new String[n][];
        for (int i = 0; i < n; i++) lines[i] = renderCardBox(cards.get(i), i);
        printRow(lines);
    }

    private void printBuildingsRow(List<Building> buildings) {
        if (buildings == null || buildings.isEmpty()) {
            System.out.println(DIM + "  (vuoto)" + RESET);
            return;
        }
        int n = buildings.size();
        String[][] lines = new String[n][];
        for (int i = 0; i < n; i++) lines[i] = renderBuildingBox(buildings.get(i), i);
        printRow(lines);
    }

    private void printTilesRow(List<Tile> tiles) {
        if (tiles == null || tiles.isEmpty()) {
            System.out.println(DIM + "  (vuoto)" + RESET);
            return;
        }
        int n = tiles.size();
        String[][] lines = new String[n][];
        for (int i = 0; i < n; i++) lines[i] = renderTileBox(tiles.get(i), i);
        printRow(lines);
    }

    private void printRow(String[][] boxes) {
        if (boxes.length == 0) return;
        int rows = boxes[0].length;
        for (int r = 0; r < rows; r++) {
            StringBuilder sb = new StringBuilder("  ");
            for (String[] box : boxes) sb.append(box[r]).append(' ');
            System.out.println(sb);
        }
    }

    private String[] renderCardBox(Card card, int index) {
        String type = card == null || card.getCategory() == null ? "?" : card.getCategory().name();
        String age  = card == null || card.getAge() == null ? "-" : card.getAge().name();
        String id   = card == null ? "-" : String.valueOf(card.getCardId());
        String color = colorForType(type);
        return new String[]{
                color + "┌──────────────┐" + RESET,
                color + "│ " + pad("[" + index + "] " + age, 12) + " │" + RESET,
                color + "│ " + pad(type,                    12) + " │" + RESET,
                color + "│ " + pad("id:" + id,              12) + " │" + RESET,
                color + "└──────────────┘" + RESET
        };
    }

    private String[] renderBuildingBox(Building b, int index) {
        String age  = b == null || b.getAge() == null ? "-" : b.getAge().name();
        String pp   = b == null ? "?" : String.valueOf(b.getPP());
        String food = b == null ? "?" : String.valueOf(b.getFoodCost());
        return new String[]{
                GREEN + "┌──────────────┐" + RESET,
                GREEN + "│ " + pad("[" + index + "] " + age, 12) + " │" + RESET,
                GREEN + "│ " + pad("PP:" + pp,               12) + " │" + RESET,
                GREEN + "│ " + pad("Food:" + food,           12) + " │" + RESET,
                GREEN + "└──────────────┘" + RESET
        };
    }

    private String[] renderTileBox(Tile t, int index) {
        String occ = (t == null || t.getOccupier() == null) ? "free" : t.getOccupier().getId().name();
        String fb  = t == null ? "?" : String.valueOf(t.getFoodBonus());
        String up  = t == null ? "?" : String.valueOf(t.getUpperPicks());
        String bp  = t == null ? "?" : String.valueOf(t.getBottomPicks());
        return new String[]{
                MAG + "┌──────────────┐" + RESET,
                MAG + "│ " + pad("Tile [" + index + "]", 12) + " │" + RESET,
                MAG + "│ " + pad("U:" + up + " B:" + bp,12) + " │" + RESET,
                MAG + "│ " + pad("food:" + fb,           12) + " │" + RESET,
                MAG + "│ " + pad(occ,                    12) + " │" + RESET,
                MAG + "└──────────────┘" + RESET
        };
    }

    private void printPlayers(GameStateDTO snap) {
        var players = snap.getPlayers();
        if (players == null || players.isEmpty()) return;
        System.out.println(BOLD + BLUE + "▼ GIOCATORI" + RESET);
        System.out.printf("  %-3s %-12s %-6s %-4s %-4s %-6s %-9s%n",
                "TT", "Nick", "Conn", "PP", "Food", "Stars", "Cards/Build");
        System.out.println(DIM + "  ───────────────────────────────────────────────────────────" + RESET);
        for (var p : players) {
            String tt = p.getTotem() == null ? "-" : p.getTotem().name().substring(0, Math.min(3, p.getTotem().name().length()));
            String conn = p.isConnected() ? GREEN + "ON " + RESET : RED + "OFF" + RESET;
            int cards = p.getCards() == null ? 0 : p.getCards().size();
            int builds = p.getBuildings() == null ? 0 : p.getBuildings().size();
            System.out.printf("  %-3s %-12s %-6s %-4d %-4d %-6d %d/%d%n",
                    tt, safe(p.getNickname()), conn, p.getPp(), p.getFood(), p.getStars(), cards, builds);
        }
        System.out.println();
    }

    private void printSelfHand(GameStateDTO snap) {
        if (user.getPlayerTotem() == null) return;
        snap.getPlayers().stream()
                .filter(p -> user.getPlayerTotem().equals(p.getTotem()))
                .findFirst()
                .ifPresent(self -> {
                    System.out.println(BOLD + CYAN + "▼ LA TUA MANO" + RESET);
                    System.out.println("  Carte:");
                    printCardsRow(self.getCards());
                    System.out.println("  Edifici:");
                    printBuildingsRow(self.getBuildings());
                    System.out.println();
                });
    }

    private void printPrompt() {
        System.out.println(DIM + "──────────────────────────────────────────────────────────────────" + RESET);
        System.out.println(BOLD + "Comandi:" + RESET);
        System.out.println("  topcard <i> | bottomcard <i> | topbuild <i> | bottombuild <i> | tile <i>");
        System.out.print(BOLD + "> " + RESET);
    }

    // ============================================================
    //                    LOBBY / TOTEM / UTIL
    // ============================================================
    private void printLobby(LobbyUpdateDTO lobby) {
        clearScreen();
        System.out.println(BOLD + CYAN + "╔════════════════════ LOBBY ════════════════════╗" + RESET);
        System.out.printf("  %sStato:%s %s%n", BOLD, RESET, lobby.getLobbyState());
        System.out.printf("  %sGame ID:%s %s%n", BOLD, RESET, safe(lobby.getIdGame()));
        if (lobby.getRequiredPlayers() > 0) {
            System.out.printf("  %sGiocatori:%s %d / %d%n", BOLD, RESET,
                    lobby.getCurrentPlayers(), lobby.getRequiredPlayers());
        }
        if (lobby.getSnapshot() != null) {
            System.out.println(DIM + "  (snapshot allegato — la partita sta per partire)" + RESET);
        }
        System.out.println(BOLD + CYAN + "╚════════════════════════════════════════════════╝" + RESET);
    }

    private void printTotemSelection(TotemSelectionDTO sel) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "▼ SELEZIONE TOTEM (game " + safe(sel.getGameId()) + ")" + RESET);
        System.out.println("  Disponibili: " + GREEN + sel.getAvailableTotems() + RESET);
        if (!sel.getTakenBy().isEmpty()) {
            System.out.println("  Già presi:");
            for (Map.Entry<?, ?> e : sel.getTakenBy().entrySet()) {
                System.out.println("    - " + e.getKey() + DIM + " → " + RESET + e.getValue());
            }
        }
        System.out.println();
    }

    // ============================================================
    //                    UserInterface — 9 new methods
    // ============================================================

    @Override
    public void onGameError(GameEventDTO dto) {
        System.out.println();
        System.out.println(RED + BOLD + "  ✖ ERRORE PARTITA: " + (dto.getEventType() != null ? dto.getEventType().name() : "?") + RESET);
        System.out.println();
    }

    @Override
    public synchronized void onPlayerTurnStarted(GameEventDTO dto) {
        update(dto);
        System.out.println(BOLD + GREEN + "  ▶ Turno di: " + (dto.getCulprit() != null ? dto.getCulprit().name() : "?") + RESET);
    }

    @Override
    public synchronized void onPlayerDisconnected(GameEventDTO dto) {
        System.out.println();
        System.out.println(RED + "  ⚠ Giocatore disconnesso: " + (dto.getCulprit() != null ? dto.getCulprit().name() : "?") + RESET);
        System.out.println();
    }

    @Override
    public synchronized void onGameEnded(GameEventDTO dto) {
        update(dto);
        System.out.println(BOLD + CYAN + "  ★ PARTITA TERMINATA ★" + RESET);
    }

    @Override
    public synchronized void onLobbyWaiting(LobbyUpdateDTO dto) {
        if (dto.getIdGame() != null) user.setId(dto.getIdGame());
        updateLobby(dto);
    }

    @Override
    public synchronized void onLobbyRejoin(LobbyUpdateDTO dto) {
        if (dto.getIdGame() != null) user.setId(dto.getIdGame());
        updateLobby(dto);
        if (dto.getSnapshot() != null) {
            String nickname = user.getNickname();
            if (nickname != null) {
                dto.getSnapshot().getPlayers().stream()
                        .filter(player -> nickname.equalsIgnoreCase(player.getNickname()))
                        .findFirst()
                        .map(GameStateDTO.PlayerDTO::getTotem)
                        .ifPresent(user::setPlayerTotem);
            }
            this.state = new GameEventDTO(GameEvent.Type.BOARD_UPDATE, null, dto.getSnapshot(), null);
            notifyAll();
        }
        System.out.println(BOLD + YELLOW + "  ↩ Reconnessione alla partita in corso..." + RESET);
    }

    @Override
    public synchronized void onGameStarting(LobbyUpdateDTO dto) {
        if (dto.getIdGame() != null) user.setId(dto.getIdGame());
        updateLobby(dto);
        System.out.println(BOLD + GREEN + "  ✔ La partita sta per iniziare!" + RESET);
    }

    @Override
    public synchronized void onTotemSelection(TotemSelectionDTO dto) {
        showTotemSelection(dto);
    }

    @Override
    public synchronized void onLobbyError(ErrorDTO dto) {
        showError(dto);
    }

    public boolean stop() {
        going = false;
        synchronized (this) { notifyAll(); }
        return true;
    }

    public GameEventDTO getState() {
        return state;
    }

    // ============================================================
    //                          HELPERS
    // ============================================================
    private static String safe(String s) { return s == null ? "-" : s; }

    private static String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private static String colorForType(String type) {
        if (type == null) return RESET;
        return switch (type.toUpperCase()) {
            case "CHARACTER" -> CYAN;
            case "EVENT"     -> YELLOW;
            case "BUILDING"  -> GREEN;
            default          -> BLUE;
        };
    }

    private static void clearScreen() {
        // Clear & home cursor — opzionale, utile su terminali ANSI.
        System.out.print("\u001B[H\u001B[2J");
        System.out.flush();
    }
}