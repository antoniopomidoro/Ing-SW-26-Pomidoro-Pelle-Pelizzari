package it.polimi.ingsw.view.CLI;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.ClientManager;
import it.polimi.ingsw.view.UserInterface;

import java.util.List;
import java.util.Map;
//handles game updates
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
    private LobbyUpdateDTO currentLobby;
    private TotemSelectionDTO currentTotems;
    private it.polimi.ingsw.model.player.Totem currentActivePlayer;
  private Thread inputSender;
    public CLIinterface(ClientManager user) {
        this.user = user;
        this.going = true;
         inputSender = new Thread(new CLIinsputSender(user, this), "CLI-input");
         inputSender.start();
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
        if (state != null) {
            if (state.getEventType() == it.polimi.ingsw.model.game.GameEvent.Type.PLAYER_TURN_STARTED && state.getCulprit() != null) {
                this.currentActivePlayer = state.getCulprit();
            } else if (state.getEventType() == it.polimi.ingsw.model.game.GameEvent.Type.END_TURN_COMPLETED
                    || state.getEventType() == it.polimi.ingsw.model.game.GameEvent.Type.TURN_COMPLETED) {
                this.currentActivePlayer = null;
            }else if(state.getEventType() == GameEvent.Type.END_GAME_COMPLETED){


            }
        }
        notifyAll();
        return true;
    }


    public synchronized boolean updateLobby(LobbyUpdateDTO lobbyUpdate) {
        this.currentLobby = lobbyUpdate;
        System.out.flush();
        printCombinedLobby();
        notifyAll();
        return true;
    }


    public synchronized boolean showTotemSelection(TotemSelectionDTO totemSelection) {
        this.currentTotems = totemSelection;
        System.out.flush();
        printCombinedLobby();
        notifyAll();
        return true;
    }


    public synchronized boolean showError(ErrorDTO error) {
        System.out.println();
        System.out.println(RED + BOLD + "  ✖ ERROR: " + error.getErrorCode().name() + RESET);
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
        if (user.getPlayerTotem() == snap.getActivePlayer()) {
            System.out.println(BOLD + GREEN + "\n  ►►► IT IS YOUR TURN! ◄◄◄" + RESET);
        }
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

        it.polimi.ingsw.model.player.Totem displayActive = this.currentActivePlayer != null ? this.currentActivePlayer : snap.getActivePlayer();

        System.out.printf("%s Game:%s %s   %sAge:%s %s   %sTurn:%s %d   %sPhase:%s %s   %sActive:%s %s%n",
                BOLD, RESET, safe(snap.getGameId()),
                BOLD, RESET, snap.getAge() == null ? "-" : snap.getAge().name(),
                BOLD, RESET, snap.getTurn(),
                BOLD, RESET, safe(snap.getCurrentPhaseName()),
                BOLD, RESET, displayActive == null ? "-" : displayActive.name());
        System.out.println(DIM + "──────────────────────────────────────────────────────────────────" + RESET);
    }

    private void printBoard(GameStateDTO snap) {
        var board = snap.getBoard();
        if (board == null) {
            System.out.println(DIM + "(board not available)" + RESET);
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
            System.out.println(DIM + "  (empty)" + RESET);
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
            System.out.println(DIM + "  (empty)" + RESET);
            return;
        }
        int n = buildings.size();
        String[][] lines = new String[n][];
        for (int i = 0; i < n; i++) lines[i] = renderBuildingBox(buildings.get(i), i);
        printRow(lines);
    }

    private void printTilesRow(List<Tile> tiles) {
        if (tiles == null || tiles.isEmpty()) {
            System.out.println(DIM + "  (empty)" + RESET);
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
        String id   = card == null ? "-" : String.valueOf(card.getCardId());
        String color = colorForType(type);

        List<String> chunks = splitText(card != null ? card.toString() : "?", 36, 4);

        return new String[]{
                color + "┌" + "─".repeat(38) + "┐" + RESET,
                color + "│ " + pad("[" + index + "] id:" + id, 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(0), 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(1), 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(2), 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(3), 36) + " │" + RESET,
                color + "└" + "─".repeat(38) + "┘" + RESET
        };
    }

    private String[] renderBuildingBox(Building b, int index) {
        String color = GREEN;
        String id = b == null ? "-" : String.valueOf(b.getCardId());

        List<String> chunks = splitText(b != null ? b.toString() : "?", 36, 4);

        return new String[]{
                color + "┌" + "─".repeat(38) + "┐" + RESET,
                color + "│ " + pad("[" + index + "] id:" + id, 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(0), 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(1), 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(2), 36) + " │" + RESET,
                color + "│ " + pad(chunks.get(3), 36) + " │" + RESET,
                color + "└" + "─".repeat(38) + "┘" + RESET
        };
    }

    private String[] renderTileBox(Tile t, int index) {
        String occ = (t == null || t.getOccupier() == null) ? "free" : t.getOccupier().getId().name();
        String fb  = t == null ? "?" : String.valueOf(t.getFoodBonus());
        String up  = t == null ? "?" : String.valueOf(t.getUpperPicks());
        String bp  = t == null ? "?" : String.valueOf(t.getBottomPicks());
        return new String[]{
                MAG + "┌" + "─".repeat(38) + "┐" + RESET,
                MAG + "│ " + pad("Tile [" + index + "]", 36) + " │" + RESET,
                MAG + "│ " + pad("U:" + up + " B:" + bp, 36) + " │" + RESET,
                MAG + "│ " + pad("food:" + fb,           36) + " │" + RESET,
                MAG + "│ " + pad(occ,                    36) + " │" + RESET,
                MAG + "│ " + pad("",                     36) + " │" + RESET,
                MAG + "└" + "─".repeat(38) + "┘" + RESET
        };
    }

    private void printPlayers(GameStateDTO snap) {
        var players = snap.getPlayers();
        if (players == null || players.isEmpty()) return;
        System.out.println(BOLD + BLUE + "▼ PLAYERS" + RESET);
        System.out.printf("  %-3s %-12s %-6s %-4s %-4s %-6s %-9s%n",
                "TT", "Nick", "Conn", "PP", "Food", "Stars", "Cards/Build");
        System.out.println(DIM + "  ───────────────────────────────────────────────────────────" + RESET);
        it.polimi.ingsw.model.player.Totem displayActive = this.currentActivePlayer != null ? this.currentActivePlayer : snap.getActivePlayer();


        for (var p : players) {
            String tt = p.getTotem() == null ? "-" : p.getTotem().name().substring(0, Math.min(3, p.getTotem().name().length()));
            String conn = p.isConnected() ? GREEN + "ON " + RESET : RED + "OFF" + RESET;
            int cards = p.getCards() == null ? 0 : p.getCards().size();
            int builds = p.getBuildings() == null ? 0 : p.getBuildings().size();

            boolean isActive = displayActive != null && displayActive.equals(p.getTotem());
            String activeMarker = isActive ? YELLOW + " [ACTIVE]" + RESET : "";



            System.out.printf("  %-3s %-12s %-6s %-4d %-4d %-6d %d/%d%s%n",
                    tt, safe(p.getNickname()), conn, p.getPp(), p.getFood(), p.getStars(), cards, builds, activeMarker);
        }



        System.out.println();
    }

    private void printSelfHand(GameStateDTO snap) {
        if (user.getPlayerTotem() == null) return;
        snap.getPlayers().stream()
                .filter(p -> user.getPlayerTotem().equals(p.getTotem()))
                .findFirst()
                .ifPresent(self -> {
                    System.out.println(BOLD + CYAN + "▼ YOUR HAND" + RESET);
                    System.out.println("  Cards:");
                    printCardsRow(self.getCards());
                    System.out.println("  Buildings:");
                    printBuildingsRow(self.getBuildings());
                    System.out.println();
                });
    }

    private void printPrompt() {
        System.out.println(DIM + "──────────────────────────────────────────────────────────────────" + RESET);
        System.out.println(BOLD + "Commands:" + RESET);
        System.out.println("  topcard <i> | bottomcard <i> | topbuild <i> | bottombuild <i> | tile <i> | view <t> \n digit help for all commands and example");
        System.out.print(BOLD + "> " + RESET);
    }

    // ============================================================
    //                    LOBBY / TOTEM / UTIL
    // ============================================================
    private void printCombinedLobby() {
        clearScreen();
        if (currentLobby != null) {
            System.out.println(BOLD + CYAN + "╔════════════════════ LOBBY ════════════════════╗" + RESET);
            System.out.printf("  %sState:%s %s%n", BOLD, RESET, currentLobby.getLobbyState());
            System.out.printf("  %sGame ID:%s %s%n", BOLD, RESET, safe(currentLobby.getIdGame()));
            if (currentLobby.getRequiredPlayers() > 0) {
                System.out.printf("  %sPlayers:%s %d / %d%n", BOLD, RESET,
                        currentLobby.getCurrentPlayers(), currentLobby.getRequiredPlayers());
            }
            if (currentLobby.getSnapshot() != null) {
                System.out.println(DIM + "  (snapshot attached — game is about to start)" + RESET);
            }
            System.out.println(BOLD + CYAN + "╚════════════════════════════════════════════════╝" + RESET);
        }

        if (currentTotems != null) {
            System.out.println();
            System.out.println(BOLD + YELLOW + "▼ TOTEM SELECTION (game " + safe(currentTotems.getGameId()) + ")" + RESET);
            System.out.println("  Available: " + GREEN + currentTotems.getAvailableTotems() + RESET);
            if (!currentTotems.getTakenBy().isEmpty()) {
                System.out.println("  Already taken:");
                for (Map.Entry<?, ?> e : currentTotems.getTakenBy().entrySet()) {
                    System.out.println("    - " + e.getKey() + DIM + " → " + RESET + e.getValue());
                }
            }
            System.out.println();
        }
    }

      // ============================================================
    //                    UserInterface
    // ============================================================

    @Override
    public void onGameError(GameEventDTO dto) {
        System.out.println();
        System.out.println(RED + BOLD + "  ✖ GAME ERROR: " + (dto.getEventType() != null ? dto.getEventType().name() : "?") + RESET);
        System.out.println();
    }

    @Override
    public synchronized void onPlayerTurnStarted(GameEventDTO dto) {
        if (dto.getCulprit() != null) {
            this.currentActivePlayer = dto.getCulprit();
        }
        update(dto);
        System.out.println(BOLD + GREEN + "  ▶ Turn of: " + (dto.getCulprit() != null ? dto.getCulprit().name() : "?") + RESET);
    }

    @Override
    public synchronized void onPlayerDisconnected(GameEventDTO dto) {
        System.out.println();
        System.out.println(RED + "  ⚠ Player disconnected: " + (dto.getCulprit() != null ? dto.getCulprit().name() : "?") + RESET);
        System.out.println();
    }

    @Override
    public synchronized void onGameEnded(GameEventDTO dto) {
        update(dto);
            new Thread(new CLIEnder(this, user, dto), "CLI-ender").start();

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
        System.out.println(BOLD + YELLOW + "  ↩ Reconnecting to ongoing game..." + RESET);
    }

    @Override
    public synchronized void onGameStarting(LobbyUpdateDTO dto) {
        if (dto.getIdGame() != null) user.setId(dto.getIdGame());
        updateLobby(dto);
        System.out.println(BOLD + GREEN + "  ✔ The game is about to start!" + RESET);
    }

    @Override
    public synchronized void onTotemSelection(TotemSelectionDTO dto) {
        showTotemSelection(dto);
    }

    @Override
    public synchronized void onLobbyError(ErrorDTO dto) {
        showError(dto);
    }

    public void stop() {
        going = false;
        inputSender.interrupt();
        synchronized (this) { notifyAll(); }

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

    private List<String> splitText(String text, int maxLen, int maxLines) {
        List<String> res = new java.util.ArrayList<>();
        if (text == null) text = "";
        text = text.replace("\n", " ").trim();
        while (!text.isEmpty() && res.size() < maxLines - 1) {
            if (text.length() <= maxLen) {
                res.add(text);
                text = "";
            } else {
                int splitAt = text.lastIndexOf(' ', maxLen);
                if (splitAt == -1) splitAt = maxLen;
                res.add(text.substring(0, splitAt));
                text = text.substring(splitAt).trim();
            }
        }
        if (!text.isEmpty() && res.size() < maxLines) {
            if (text.length() > maxLen) {
                res.add(text.substring(0, maxLen - 3) + "...");
            } else {
                res.add(text);
            }
        }
        while (res.size() < maxLines) res.add("");
        return res;
    }

    public void viewPlayerHand(Totem totem, GameStateDTO snapshot) {
        if (totem == null) return;
        if (snapshot == null) return;
        snapshot.getPlayers().stream()
                .filter(p -> totem.equals(p.getTotem()))
                .findFirst()
                .ifPresentOrElse(self -> {
                    if (totem != snapshot.getActivePlayer()) {
                        System.out.println(BOLD + CYAN + "▼ " + self.getNickname() + "'s HAND" + RESET);
                        System.out.println("  Cards:");
                        printCardsRow(self.getCards());
                        System.out.println("  Buildings:");
                        printBuildingsRow(self.getBuildings());
                        System.out.println();
                    } else {
                        System.out.println(RED + totem + " totem is your totem!");
                    }
                }, () -> {
                    System.out.println(RED + totem + "totem is not used in this game!");
                });
        printPrompt();
    }
}