package GUI;

import APP.Api;
import EventHandler.CardReaderEvent;
import EventHandler.CardReaderHandler;
import EventHandler.CardReaderInterface;
import Extends.CardReader;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import model.UserEntrance;
import scm.App;
import java.awt.Desktop;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.ClientEntrance;
import scm.Constants;
import utils.AppWorker;
import Extends.PanelCommon;
import gui.PanelErrors;
import model.GuiPanel;

public class Controller implements Initializable
{

    @FXML
    protected Label appTitle;
    @FXML
    private AnchorPane menuOpener;
    @FXML
    private Pane paneEffectDisable;
    @FXML
    protected Label status;
    @FXML
    protected Label statusMessage;
    @FXML
    protected StackPane statusBackground;
    @FXML
    protected Tooltip tooltipVersion;
    @FXML
    protected AnchorPane closeApp;
    @FXML
    protected AnchorPane forceEntrance;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        tooltipVersion.setText("Versión del lector: " + Constants.APP_VERSION);
        appTitle.setText("SCM - Lector de clientes (Con entrada)");
        paneEffectDisable.setVisible(false);
        menuOpener.setVisible(false);

        updateCardReaderStatus("LOADING");
        closeApp.managedProperty().bind(closeApp.visibleProperty());
        forceEntrance.managedProperty().bind(forceEntrance.visibleProperty());
        
        CardReaderHandler.getInstance().addCardReaderListener(new CardReaderInterface()
        {
            @Override
            public void changeReaderStatus(CardReaderEvent evt)
            {
                updateCardReaderStatus(evt.getSource().toString(), (ClientEntrance) evt.getEntrance());
            }

            @Override
            public void readerGotError(CardReaderEvent evt)
            {
                updateCardReaderError(evt.getSource().toString());
            }
        });
        
        /* New thread for back-end */
        new Thread(() ->
        {
            /* Initialize app stuff */
            App.getInstance().initialize();
            /* Initialize app stuff */
            CardReader.getInstance().initialize();
        }).start();
    }

    /* Reader Changes */
    private void updateCardReaderStatus(String status)
    {
        updateCardReaderStatus(status, new ClientEntrance());
    }

    public void updateCardReaderError(String errorCode)
    {
        if (!CardReaderHandler.readerError())
        {
            forceEntrance.setVisible(false);
            closeApp.setVisible(true);
            CardReaderHandler.setReaderError();
            Platform.runLater(() ->
            {
                PanelErrors guiErrors = new PanelErrors();
                GuiPanel error = guiErrors.loadError(errorCode);
                updateReader(error);

                AppWorker app = new AppWorker();
                if (error.getAction().equals("reload"))
                {
                    app.reloadAfter(error.getTimeout());
                }
                else if (error.getAction().equals("close"))
                {
                    app.closeAfter(error.getTimeout());
                }
                else if (!error.getAction().equals("none"))
                {
                    app.reload();
                }
            });
        }
    }

    private void updateReader(GuiPanel panel)
    {
        this.status.setText(panel.getMainText());
        this.status.setStyle("-fx-text-fill: " + panel.getTextColor());
        this.statusMessage.setStyle("-fx-text-fill: " + panel.getTextColor());
        this.statusBackground.setStyle("-fx-background-color: " + panel.getBgColor());
        this.statusMessage.setText(panel.getBottomText());
    }

    private void updateCardReaderStatus(String status, ClientEntrance userEntrance)
    {
        if (!CardReaderHandler.readerError())
        {
            forceEntrance.setVisible(false);
            closeApp.setVisible(false);
            Platform.runLater(() ->
            {

                PanelCommon guiErrors = new PanelCommon();
                GuiPanel panel = guiErrors.loadPanel(status, userEntrance);
                updateReader(panel);

                /* Cosicas del menu */
                if (status.equals("ERROR_CONFLICTIVE") || status.equals("ERROR_ROOM_FULL"))
                {
                    forceEntrance.setVisible(true);
                }

                updateReader(panel);
            });
        }
    }

    @FXML
    private void navExitFrames(MouseEvent event)
    {
        menuOpener.setVisible(false);
        paneEffectDisable.setVisible(false);
    }

    @FXML
    private void navOpenMenu(MouseEvent event)
    {
        menuOpener.setVisible(!menuOpener.isVisible());
        paneEffectDisable.setVisible(true);
    }

    @FXML
    private void navMinimize(MouseEvent event)
    {
        Stage window = (Stage) ((ImageView) event.getSource()).getScene().getWindow();
        window.setIconified(true);
    }

    @FXML
    private void navReload(MouseEvent event)
    {
        AppWorker app = new AppWorker();
        app.reload();
    }

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private void navClickDragWindow(MouseEvent event)
    {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void navDragWindow(MouseEvent event)
    {
        ((Stage) ((GridPane) event.getSource()).getScene().getWindow()).setX(event.getScreenX() - xOffset);
        ((Stage) ((GridPane) event.getSource()).getScene().getWindow()).setY(event.getScreenY() - yOffset);
    }

    @FXML
    private void menuOpenSCMPanel(MouseEvent event)
    {
        try
        {
            Desktop.getDesktop().browse(new URL(Constants.SCM_WEB).toURI());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            CardReaderHandler.getInstance().cardReaderError("OPEN_WEB_ERROR");
        }
    }

    @FXML
    private void menuCloseAppPanel(MouseEvent event)
    {
        System.exit(0);
    }

    @FXML
    private void menuManualEntrance(MouseEvent event) {
        Platform.runLater(() ->
        {
            CardReader.getInstance().onFinishEntrance();
            CardReader.getInstance().checkClientData();
        });
    }

    @FXML
    private void menuForceEntrancePanel(MouseEvent event)
    {
        Platform.runLater(() ->
        {
            CardReader.getInstance().forceaccess();
        });
    }

}
