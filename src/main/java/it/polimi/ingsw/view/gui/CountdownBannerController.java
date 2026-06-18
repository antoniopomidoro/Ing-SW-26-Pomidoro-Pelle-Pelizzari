package it.polimi.ingsw.view.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Controller for the countdown banner. It updates the countdown label. */
public class CountdownBannerController {
    @FXML private Label countLabel;

    public void setRemaining(long seconds) {
        countLabel.setText("Auto-win in " + seconds + "s");
    }
}