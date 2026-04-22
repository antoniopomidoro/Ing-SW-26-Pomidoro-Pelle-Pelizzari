package it.polimi.ingsw.network;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.GamePhase;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.PhaseDataExporter;
import it.polimi.ingsw.model.game.PhaseRestoreContext;
import it.polimi.ingsw.model.game.StatePhases.GamePhaseBehavior;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameStateSaveDTO;
import it.polimi.ingsw.network.dto.GameStateSaveDTO.SaveBoardDTO;
import it.polimi.ingsw.network.dto.GameStateSaveDTO.SaveDecksDTO;
import it.polimi.ingsw.network.dto.GameStateSaveDTO.SavePhaseDTO;
import it.polimi.ingsw.network.dto.GameStateSaveDTO.SaveTileDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Centralized persistence service for converting and storing GameState snapshots.
 */
public final class GameStatePersistence {

    private GameStatePersistence() {
    }

    public static void save(GameState state, String gameId) {
        Objects.requireNonNull(state, "state cannot be null");
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId cannot be null or blank");
        }

        File file = new File("saves/" + gameId + ".json");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Unable to create save directory");
        }

        try {
            JacksonConfig.mapper().writeValue(file, toDTO(state));
        } catch (IOException e) {
            throw new IllegalStateException("Error during save for game " + gameId, e);
        }
    }

    public static GameState load(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId cannot be null or blank");
        }

        File file = new File("saves/" + gameId + ".json");
        try {
            GameStateSaveDTO dto = JacksonConfig.mapper().readValue(file, GameStateSaveDTO.class);
            return fromDTO(dto);
        } catch (IOException e) {
            throw new IllegalStateException("Error during load for game " + gameId, e);
        }
    }

    public static GameStateSaveDTO toDTO(GameState state) {
        Objects.requireNonNull(state, "state cannot be null");

        GameStateSaveDTO dto = new GameStateSaveDTO();
        dto.setGameId(state.getGameId());
        dto.setAge(state.getAge());
        dto.setTurn(state.getTurn());
        dto.setCurrentTileIndex(state.getCurrentTileIndex());
        dto.setCurrentPlayerIndex(state.getCurrentPlayerIndex());
        dto.setCurrentPlayerOrderIndex(state.getCurrentPlayerOrderIndex());
        dto.setExtraIndex(state.getExtraIndex());
        dto.setConfig(state.getConfig());
        dto.setPlayers(new ArrayList<>(state.getPlayers()));
        dto.setBoard(toBoardDTO(state.getBoard()));
        dto.setDecks(toDecksDTO(state.getDeck()));
        dto.setPhase(toPhaseDTO(state));
        dto.setTurnOrder(state.getTurnOrder().stream().map(Player::getId).collect(Collectors.toList()));
        dto.setOrderTileOrder(state.getOrderTileOrder().stream().map(Player::getId).collect(Collectors.toList()));
        return dto;
    }

    public static GameState fromDTO(GameStateSaveDTO dto) {
        Objects.requireNonNull(dto, "dto cannot be null");

        GameState state = new GameState();
        state.setAge(dto.getAge());
        state.setTurn(dto.getTurn());
        state.setCurrentTileIndex(dto.getCurrentTileIndex());
        state.setCurrentPlayerIndex(dto.getCurrentPlayerIndex());
        state.setCurrentPlayerOrderIndex(dto.getCurrentPlayerOrderIndex());
        state.setExtraIndex(dto.getExtraIndex());
        state.setGameId(dto.getGameId());
        state.setConfig(dto.getConfig());

        List<Player> players = new ArrayList<>(dto.getPlayers());
        players.forEach(p -> p.setConnected(false));
        state.setPlayers(players);

        Board board = reconstructBoard(dto.getBoard(), players);
        state.setBoard(board);
        state.setDeck(reconstructDecks(dto.getDecks()));
        state.setTurnOrder(resolvePlayerList(dto.getTurnOrder(), players));
        state.restoreOrderTileOrder(resolvePlayerList(dto.getOrderTileOrder(), players));

        GamePhaseBehavior restoredPhase = restorePhase(dto.getPhase(), players, board);
        state.restorePhase(restoredPhase);
        return state;
    }

    private static SaveBoardDTO toBoardDTO(Board board) {
        SaveBoardDTO dto = new SaveBoardDTO();
        dto.setOrderTile(board.getOrderTile());
        dto.setTopCards(new ArrayList<>(board.getTopCards()));
        dto.setBottomCards(new ArrayList<>(board.getBottomCards()));
        dto.setTopBuildings(new ArrayList<>(board.getTopBuildings()));
        dto.setBottomBuildings(new ArrayList<>(board.getBottomBuildings()));

        List<SaveTileDTO> tiles = board.getTiles().getTiles().stream()
                .map(GameStatePersistence::toTileDTO)
                .collect(Collectors.toList());
        dto.setTiles(tiles);
        return dto;
    }

    private static SaveTileDTO toTileDTO(Tile tile) {
        SaveTileDTO dto = new SaveTileDTO();
        dto.setMinPlayers(tile.getMinPlayers());
        dto.setFoodBonus(tile.getFoodBonus());
        dto.setUpperPicks(tile.getUpperPicks());
        dto.setBottomPicks(tile.getBottomPicks());
        Player occupier = tile.getOccupier();
        dto.setOccupier(occupier != null ? occupier.getId() : null);
        return dto;
    }

    private static SaveDecksDTO toDecksDTO(Decks decks) {
        SaveDecksDTO dto = new SaveDecksDTO();
        dto.setCards(decks.getCards());
        dto.setBuildings(decks.getAllBuildings());
        return dto;
    }

    private static SavePhaseDTO toPhaseDTO(GameState state) {
        SavePhaseDTO dto = new SavePhaseDTO();
        GamePhaseBehavior phase = state.getCurrentPhase();
        dto.setPhaseName(phase.getClass().getSimpleName());
        phase.exportData(new PhaseDataExporter() {
            @Override
            public void exportPlayerTurnData(Player activePlayer, Tile activeTile, int upperPicks, int bottomPicks) {
                dto.setActivePlayerTotem(activePlayer.getId());
                dto.setActiveTileIndex(state.getBoard().getTiles().indexOf(activeTile));
                dto.setUpperPicks(upperPicks);
                dto.setBottomPicks(bottomPicks);
            }
        });
        return dto;
    }

    private static GamePhaseBehavior restorePhase(SavePhaseDTO dto, List<Player> players, Board board) {
        if (dto == null) {
            throw new IllegalStateException("Save file corrupted: phase data cannot be null");
        }
        String phaseName = dto.getPhaseName();
        if (phaseName == null) {
            throw new IllegalStateException("Save file corrupted: phase name cannot be null");
        }
        PhaseRestoreContext ctx = new PhaseRestoreContext(dto, players, board);
        return GamePhase.valueOf(phaseName).restore(ctx);
    }

    private static Board reconstructBoard(SaveBoardDTO dto, List<Player> players) {
        List<Tile> tiles = new ArrayList<>();
        for (SaveTileDTO tileDto : dto.getTiles()) {
            Tile tile = new Tile();
            tile.setMinPlayers(tileDto.getMinPlayers());
            tile.setFoodBonus(tileDto.getFoodBonus());
            tile.setUpperPicks(tileDto.getUpperPicks());
            tile.setBottomPicks(tileDto.getBottomPicks());
            if (tileDto.getOccupier() != null) {
                tile.setOccupier(findPlayer(tileDto.getOccupier(), players));
            }
            tiles.add(tile);
        }

        Board board = new Board(dto.getOrderTile(), new TileSet(tiles));
        board.setTopCards(dto.getTopCards());
        board.setBottomCards(dto.getBottomCards());
        board.setTopBuildings(dto.getTopBuildings());
        board.setBottomBuildings(dto.getBottomBuildings());
        return board;
    }

    private static Decks reconstructDecks(SaveDecksDTO dto) {
        Decks decks = new Decks();
        decks.setCards(dto.getCards());
        decks.setBuildings(dto.getBuildings());
        return decks;
    }

    private static List<Player> resolvePlayerList(List<Totem> totems, List<Player> players) {
        return totems.stream().map(totem -> findPlayer(totem, players)).collect(Collectors.toList());
    }

    private static Player findPlayer(Totem totem, List<Player> players) {
        return players.stream()
                .filter(player -> player.getId() == totem)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Save file corrupted: no player found for totem " + totem));
    }
}

