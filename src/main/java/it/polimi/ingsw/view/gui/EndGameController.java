package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.App;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.view.ClientManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.util.Comparator;

import static java.lang.System.exit;

public class EndGameController {

    @FXML private AnchorPane root;
    @FXML private Label winnerLabel;
    @FXML private Label scores;

    private Runnable menu;

    public void setMenu(Runnable callback) {
        this.menu = callback;
    }

    public void setWinner(String winner) {
        winnerLabel.setText("The winner is: " + winner + "!");
    }

    @FXML
    public void initialize() {
    }

    public  void initData(GameStateDTO state) {
        int playerCount = state.getPlayers().size();
        int onlinePlayers = 0;
        String onName = "";
        for(int i = 0; i < playerCount; i++) {
            if(state.getPlayers().get(i).isConnected()) {
                onlinePlayers++;
            } else {
                onName = state.getPlayers().get(i).getNickname();
            }
        }

        if(onlinePlayers > 1) {
            state.getPlayers().stream()
                    .max(Comparator.comparingInt(p -> p.getPp()))
                    .ifPresent(p -> setWinner(p.getNickname()));
            state.getPlayers().stream()
                    .sorted(Comparator.comparingInt(GameStateDTO.PlayerDTO::getPp).reversed())
                    .forEach(p -> scores.setText(scores.getText() + p.getNickname() + ": " + p.getPp() + "\n"));
        } else if(onlinePlayers == 1) {
            setWinner(onName);
            scores.setText("All other players abandoned the game.");
        }
    }


    @FXML
    private void onBackToMenu() {
        if (menu != null) menu.run();
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}
