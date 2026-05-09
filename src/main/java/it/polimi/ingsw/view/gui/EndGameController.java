package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.App;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.view.ClientManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Comparator;

import static java.lang.System.exit;

public class EndGameController {

    @FXML private StackPane root;
    @FXML private Label winnerLabel;

    private final GameAnimator animator = new GameAnimator();
    private Runnable menu;

    public void setWinner(String winner) {
        winnerLabel.setText("The winner is: " + winner + "!");
    }

    @FXML
    public void initialize() {
    }

    public  void initData(GameStateDTO state) {
        state.getPlayers().stream()
                .max(Comparator.comparingInt(p -> p.getPp()))
                .ifPresent(p -> setWinner(p.getNickname()));
    }


    @FXML
    private void onBackToMenu() {
        // TODO: implement back to menu
    }

    @FXML
    private void onExit() {
        exit(0);
    }

    public void endGame(Runnable onDone) {
        animator.animateEndGame(root.getChildren().get(0), onDone);
    }
}
