package Extends;

import APP.Api;
import EventHandler.CardReaderHandler;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import scm.Constants;
import utils.Coding;
import scm.CardReaderAbstract;


public class CardReader extends CardReaderAbstract {
    private static CardReader instance = null;
    public Stage windowRates = null;
    public Stage windowBans = null;
    public Stage windowExtraData = null;
    private Client client = null;
    private EntranceType entranceType = null;

    @Override
    public void onFinishEntrance() {
        clearData();
        clearExtraData();
        closeWindows();
    }

    @Override
    public void clearExtraData() {
        lastVisitforceaccess = false;
        client = null;
        entranceType = null;
    }

    @Override
    public void onCertificateLoaded() {
        /* Cargamos la información del usuario para cachearla */
        loadClient();
        /* Ahora procedemos a las validaciones. Empezamos con la de revisar los datos, el resto va en cascada.*/
        checkClientData();
    }

    public void loadClient() {
        Api api = new Api();
        client = api.clientEntranceData(lastVisitCert);
    }

    public void closeWindows() {
        if (null != CardReader.getInstance().windowRates) {
            Platform.runLater(
                    () -> {
                        windowRates.close();
                        windowRates = null;
                    });
        }
        if (null != CardReader.getInstance().windowBans) {
            Platform.runLater(
                    () -> {
                        windowBans.close();
                        windowBans = null;
                    });
        }
        if (null != CardReader.getInstance().windowExtraData) {
            Platform.runLater(
                    () -> {
                        windowExtraData.close();
                        windowExtraData = null;
                    });
        }
    }

    /* Prevenir inicialización directa sin singleton */
    private CardReader() {
    }

    public static CardReader getInstance() {
        if (instance == null) {
            instance = new CardReader();
        }
        return instance;
    }

    public void forceaccess() {
        lastVisitforceaccess = true;
        doEntrance();
    }

    @Override
    public void checkClientData() {

        if(!isDataInfoComplete())
        {
            Platform.runLater(new Runnable() {
                private StackPane mainDialog;
                private JFXButton searchBtn;
                private JFXTextField dniLabel;
                private JFXTextField nameLabel;
                private JFXTextField surname1Label;
                private JFXTextField surname2Label;
                private JFXTextField emailLabel;
                private JFXDatePicker birthdateLabel;
                private JFXTextField addressLabel;
                private JFXComboBox genderComboBox;
                private JFXComboBox nationalityComboBox;
                private JFXButton sendBtn;
                private JFXButton closeBtn;
                private boolean searching = false;

                public void run() {
                    try {
                        CardReader.getInstance().manualAccess = true;
                        CardReaderHandler.getInstance().changeCardReaderStatus("WAITING_INPUT_DATA");
                        windowExtraData = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientData.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        windowExtraData.initStyle(StageStyle.UNDECORATED);
                        windowExtraData.setAlwaysOnTop(true);

                        /* Define inputs */
                        mainDialog = (StackPane) loader.getNamespace().get("mainDialog");
                        searchBtn = (JFXButton) loader.getNamespace().get("searchBtn");
                        dniLabel = (JFXTextField) loader.getNamespace().get("dni");
                        nameLabel = (JFXTextField) loader.getNamespace().get("name");
                        surname1Label = (JFXTextField) loader.getNamespace().get("surname1");
                        surname2Label = (JFXTextField) loader.getNamespace().get("surname2");
                        emailLabel = (JFXTextField) loader.getNamespace().get("email");
                        addressLabel = (JFXTextField) loader.getNamespace().get("address");
                        genderComboBox = (JFXComboBox) loader.getNamespace().get("gender");
                        birthdateLabel = (JFXDatePicker) loader.getNamespace().get("birthdate");
                        nationalityComboBox = (JFXComboBox) loader.getNamespace().get("nationality");
                        sendBtn = (JFXButton) loader.getNamespace().get("sendBtn");
                        closeBtn = (JFXButton) loader.getNamespace().get("closeBtn");
                        clearFields();
                        Api api = new Api();
                        genderComboBox.setItems(FXCollections.observableArrayList(api.genders()));
                        genderComboBox.setConverter(new StringConverter<Gender>() {
                            @Override
                            public String toString(Gender object) {
                                String translateName = "Otros";
                                switch (object.getName()) {
                                    case "M":
                                        translateName = "Hombre";
                                        break;
                                    case "F":
                                        translateName = "Mujer";
                                        break;
                                }
                                return translateName;
                            }

                            @Override
                            public Gender fromString(String string) {
                                return null;
                            }
                        });

                        nationalityComboBox.setItems(FXCollections.observableArrayList(api.nationalities()));
                        nationalityComboBox.setConverter(new StringConverter<Nationality>() {
                            @Override
                            public String toString(Nationality object) {
                                String translateName = "Otros";
                                switch (object.getName()) {
                                    case "ES":
                                        translateName = "Española";
                                        break;
                                }
                                return translateName;
                            }

                            @Override
                            public Nationality fromString(String string) {
                                return null;
                            }
                        });
                        sendBtn.setText("COMPLETAR");
                        closeBtn.setText("CERRAR");

                        fillFields(client);

                        sendBtn.setOnMouseClicked(click -> {
                            if (areAllFieldsFilled()) {
                                CardReaderHandler.getInstance().changeCardReaderStatus("WAITING");
                                if(null == client)
                                {
                                    client = new Client();
                                }

                                client.setAddress(addressLabel.getText());
                                client.setBirthdate(Date.from(birthdateLabel.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                client.setDni(dniLabel.getText());
                                client.setEmail(emailLabel.getText());
                                System.out.println("EMAIL : " + client.getEmail());
                                Gender gender = ((Gender) genderComboBox.getValue());
                                if (null != gender) {
                                    client.setGender(gender);
                                }

                                client.setName(nameLabel.getText());
                                Nationality nationality = ((Nationality) nationalityComboBox.getValue());
                                if (null != gender) {
                                    client.setNationality(nationality);
                                }
                                client.setSurname1(surname1Label.getText());
                                client.setSurname2(surname2Label.getText());
                                clearFields();
                                closeWindows();
                                checkClientData();
                            } else {
                                JFXDialog dialog = new JFXDialog();
                                Label dialogContent = new Label("Rellena todos los datos.");
                                dialogContent.setFont(new Font(26));
                                dialog.setContent(dialogContent);
                                dialog.setPadding(new Insets(20, 10, 20, 10));
                                dialog.setDialogContainer(mainDialog);
                                dialog.show();
                            }
                        });

                        closeBtn.setOnMouseClicked(click -> {
                            CardReaderHandler.getInstance().changeCardReaderStatus("WAITING");
                            clearFields();
                            onFinishEntrance();
                        });
                        /* On search, fill content if found and disable it edition. Else, show a popup */
                        searchBtn.setOnMouseClicked(click -> {
                            Client client = new Client();
                            if (!dniLabel.getText().equals("") && dniLabel.getText() != null) {
                                client.setDni(dniLabel.getText());
                                client = api.clientDataById(dniLabel.getText());
                            }
                            searching = true;
                            fillFields(client);

                        });

                        windowExtraData.setScene(scene);
                        windowExtraData.show();
                    } catch (Exception e)

                    {
                        CardReader.getInstance().manualAccess = false;
                        Logger logger = Logger.getLogger(getClass().getName());
                        logger.log(Level.SEVERE, "Failed to create new Window.", e);
                    }
                }

                public void clearFields() {
                    dniLabel.clear();
                    nameLabel.clear();
                    surname1Label.clear();
                    surname2Label.clear();
                    emailLabel.clear();
                    addressLabel.clear();
                    birthdateLabel.setValue(null);
                    genderComboBox.setValue(null);
                    nationalityComboBox.setValue(null);
                }

                public boolean areAllFieldsFilled() {
                    return dniLabel.getText() != null && !dniLabel.getText().equals("")
                            && nameLabel.getText() != null && !nameLabel.getText().equals("")
                            && surname1Label.getText() != null && !surname1Label.getText().equals("")
                            && surname2Label.getText() != null && !surname2Label.getText().equals("")
                            && emailLabel.getText() != null && !emailLabel.getText().equals("")
                            && addressLabel.getText() != null && !addressLabel.getText().equals("")
                            && birthdateLabel.getValue() != null && !birthdateLabel.getValue().equals("")
                            && genderComboBox.getValue() != null
                            && nationalityComboBox.getValue() != null;
                }

                public void fillFields(Client client) {

                    if (null == client && searching == true) {
                        JFXDialog dialog = new JFXDialog();
                        Label dialogContent = new Label("Cliente no encontrado.");
                        dialogContent.setFont(new Font(26));
                        dialog.setContent(dialogContent);
                        dialog.setPadding(new Insets(20, 10, 20, 10));
                        dialog.setDialogContainer(mainDialog);
                        dialog.show();
                        searching = false;
                    } else if (null != client) {
                        /* Disable base elements */
                        searchBtn.setDisable(true);
                        /* Disable name if it's set */
                        if (null != client.getDni() && !client.getDni().equals("")) {
                            dniLabel.setText(client.getDni());
                            dniLabel.setDisable(true);
                        }
                        /* Disable name if it's set */
                        if (null != client.getName() && !client.getName().equals("")) {
                            nameLabel.setText(client.getName());
                            nameLabel.setDisable(true);
                        }
                        /* Disable surname1 if it's set */
                        if (null != client.getSurname1() && !client.getSurname1().equals("")) {
                            surname1Label.setText(client.getSurname1());
                            surname1Label.setDisable(true);
                        }
                        /* Disable surname2 if it's set */
                        if (null != client.getSurname2() && !client.getSurname2().equals("")) {
                            surname2Label.setText(client.getSurname2());
                            surname2Label.setDisable(true);
                        }
                        /* Disable email if it's set */
                        if (null != client.getEmail() && !client.getEmail().equals("")) {
                            emailLabel.setText(client.getEmail());
                            emailLabel.setDisable(true);
                        }
                        /* Disable email if it's set */
                        if (null != client.getAddress() && !client.getAddress().equals("")) {
                            addressLabel.setText(client.getAddress());
                            addressLabel.setDisable(true);
                        }
                        /* Disable birthdate if it's set */
                        if (null != client.getBirthdate() && !client.getBirthdate().equals("")) {
                            birthdateLabel.setValue(client.getBirthdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            birthdateLabel.setDisable(true);
                        }
                        /* Disable gender if it's set */
                        if (null != client.getGender() && null != client.getGender().getName() && !client.getGender().getName().equals("")) {
                            int it = 0, index = -1;
                            for (Object obj : genderComboBox.getItems()) {
                                Gender gen = (Gender) obj;
                                if (gen.getName().equals(client.getGender().getName())) {
                                    index = it;
                                }
                                it++;
                            }
                            if (index != -1) {
                                genderComboBox.getSelectionModel().select(index);
                                genderComboBox.setDisable(true);
                            }
                        }
                        /* Disable nationality if it's set */
                        if (null != client.getNationality() && null != client.getNationality().getName() && !client.getNationality().getName().equals("")) {
                            int it = 0, index = -1;
                            for (Object obj : nationalityComboBox.getItems()) {
                                Nationality nat = (Nationality) obj;
                                if (nat.getName().equals(client.getNationality().getName())) {
                                    index = it;
                                }
                                it++;
                            }
                            if (index != -1) {
                                nationalityComboBox.getSelectionModel().select(index);
                                nationalityComboBox.setDisable(true);
                            }
                        }
                    }
                }
            });

        }
        else
        {
            checkClientBans();
        }
    }

    @Override
    public void checkPricing() {
        /*
            Ahora preguntar qué tarifa consumirá el cliente.
         */

        Api api = new Api();
        System.out.println("ENTRANCE: " + entranceType.getName());
        System.out.println("CHECKING PRICING: " );
        if (entranceType.getName().equals("JOIN")) {
            List<ClientEntrancePricing> apiRates = api.clientEntrancePricing(client.getDni());
            System.out.println("EMPTY: " + apiRates.isEmpty());
            if (!apiRates.isEmpty()) {
                Platform.runLater(
                        () -> {
                            try {
                                CardReader.getInstance().closeWindows();
                                CardReaderHandler.getInstance().changeCardReaderStatus("WAITING_RATE");
                                windowRates = new Stage();
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Table.fxml"));
                                Parent root = loader.load();

                                Scene scene = new Scene(root);
                                windowRates.initStyle(StageStyle.UNDECORATED);
                                windowRates.setAlwaysOnTop(true);

                                JFXTreeTableColumn<ClientEntrancePricingTable, String> rateName = new JFXTreeTableColumn<>("Tarifa");
                                rateName.setPrefWidth(380);
                                rateName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ClientEntrancePricingTable, String>, ObservableValue<String>>() {
                                    @Override
                                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ClientEntrancePricingTable, String> param) {
                                        return param.getValue().getValue().name;
                                    }
                                });


                                JFXTreeTableColumn<ClientEntrancePricingTable, String> ratePrice = new JFXTreeTableColumn<>("Precio");
                                ratePrice.setPrefWidth(150);
                                ratePrice.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ClientEntrancePricingTable, String>, ObservableValue<String>>() {
                                    @Override
                                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ClientEntrancePricingTable, String> param) {
                                        DecimalFormat df = new DecimalFormat();
                                        df.setMaximumFractionDigits(2);
                                        return new SimpleStringProperty(df.format(param.getValue().getValue().price.floatValue()) + " €");
                                    }
                                });

                                ObservableList<ClientEntrancePricingTable> rates = FXCollections.observableArrayList();
                                for (ClientEntrancePricing rate : apiRates) {
                                    rates.add(new ClientEntrancePricingTable(rate));
                                }

                                JFXTreeTableView<ClientEntrancePricingTable> treeView = (JFXTreeTableView<ClientEntrancePricingTable>) loader.getNamespace().get("treeView");

                                final TreeItem<ClientEntrancePricingTable> roottree = new RecursiveTreeItem<ClientEntrancePricingTable>(rates, RecursiveTreeObject::getChildren);
                                treeView.getColumns().setAll(rateName, ratePrice);
                                treeView.setRoot(roottree);
                                treeView.setShowRoot(false);
                                JFXButton sendBtn = (JFXButton) loader.getNamespace().get("sendBtn");
                                sendBtn.setText("SELECCIONAR TARIFA");
                                StackPane mainDialog = (StackPane) loader.getNamespace().get("mainDialog");
                                sendBtn.setOnMouseClicked(event -> {
                                    CardReaderHandler.getInstance().changeCardReaderStatus("READING");
                                    TreeItem<ClientEntrancePricingTable> selected = treeView.getSelectionModel().getSelectedItem();
                                    if (null == selected || null == selected.getValue()) {
                                        JFXDialog dialog = new JFXDialog();
                                        Label dialogContent = new Label("Debes seleccionar una tarifa.");
                                        dialogContent.setFont(new Font(26));
                                        dialog.setContent(dialogContent);
                                        dialog.setPadding(new Insets(20, 10, 20, 10));
                                        dialog.setDialogContainer(mainDialog);
                                        dialog.show();
                                        CardReaderHandler.getInstance().changeCardReaderStatus("WAITING_RATE");
                                    } else {
                                        windowRates.close();
                                        ClientEntrancePricing rate = new ClientEntrancePricing();
                                        rate.setId(selected.getValue().id.getValue().intValue());
                                        rate.setName(selected.getValue().name.getValue().toString());
                                        rate.setPrice(selected.getValue().id.getValue().floatValue());
                                        lastVisitRate = rate;
                                        doEntrance();
                                    }
                                });

                                windowRates.setScene(scene);
                                windowRates.show();

                            } catch (Exception e) {
                                Logger logger = Logger.getLogger(getClass().getName());
                                logger.log(Level.SEVERE, "Failed to create new Window.", e);
                            }
                        }
                );
            } else {
                doEntrance();
            }
        } else {
            doEntrance();
        }
    }

    public boolean isDataInfoComplete() {
         return client != null && client.getDni() != null && !client.getDni().equals("")
                && client.getName() != null && !client.getName().equals("")
                && client.getName() != null && !client.getName().equals("")
                && client.getSurname1() != null && !client.getSurname1().equals("")
                && client.getSurname2() != null && !client.getSurname2().equals("")
                && client.getEmail() != null && !client.getEmail().equals("")
                && client.getAddress() != null && !client.getAddress().equals("")
                && client.getBirthdate() != null
                && client.getGender() != null
                && client.getNationality() != null;
    }

    public void checkClientBans() {
        Api api = new Api();
        entranceType = api.clientEntranceType(client.getDni());
        if (entranceType.getName().equals("JOIN")) {
            List<ClientBan> userBans = api.clientEntranceBans(client.getDni());
            if (!userBans.isEmpty()) {
                Platform.runLater(
                        () -> {
                            try {
                                CardReader.getInstance().closeWindows();
                                CardReaderHandler.getInstance().changeCardReaderStatus("ERROR_CONFLICTIVE");
                                windowBans = new Stage();
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Table.fxml"));
                                Parent root = loader.load();

                                Scene scene = new Scene(root);
                                windowBans.initStyle(StageStyle.UNDECORATED);
                                windowBans.setAlwaysOnTop(true);

                                JFXTreeTableColumn<ClientEntranceBanTable, String> rateName = new JFXTreeTableColumn<>("USUARIO CONFLICTIVO");
                                rateName.setPrefWidth(534);
                                rateName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ClientEntranceBanTable, String>, ObservableValue<String>>() {
                                    @Override
                                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ClientEntranceBanTable, String> param) {
                                        return param.getValue().getValue().name;
                                    }
                                });

                                ObservableList<ClientEntranceBanTable> rates = FXCollections.observableArrayList();
                                for (ClientBan userBan : userBans) {
                                    rates.add(new ClientEntranceBanTable(userBan));
                                }


                                JFXTreeTableView<ClientEntranceBanTable> treeView = (JFXTreeTableView<ClientEntranceBanTable>) loader.getNamespace().get("treeView");
                                final TreeItem<ClientEntranceBanTable> roottree = new RecursiveTreeItem<ClientEntranceBanTable>(rates, RecursiveTreeObject::getChildren);
                                treeView.getColumns().setAll(rateName);
                                treeView.setRoot(roottree);
                                treeView.setShowRoot(false);
                                JFXButton sendBtn = (JFXButton) loader.getNamespace().get("sendBtn");
                                sendBtn.setText("ACEPTAR ENTRADA");
                                StackPane mainDialog = (StackPane) loader.getNamespace().get("mainDialog");
                                sendBtn.setOnMouseClicked(event -> {
                                    CardReaderHandler.getInstance().changeCardReaderStatus("READING");
                                    checkPricing();
                                    windowBans.close();
                                    lastVisitforceaccess = true;
                                });

                                windowBans.setScene(scene);
                                windowBans.show();

                            } catch (Exception e) {
                                Logger logger = Logger.getLogger(getClass().getName());
                                logger.log(Level.SEVERE, "Failed to create new Window.", e);
                            }
                        }
                );

            } else {
                checkPricing();
            }
        } else {
            checkPricing();
        }
    }

    @Override
    public void doEntrance() {
        Api api = new Api();
        ClientEntrance clientEntrance = api.clientEntrance(new ClientEntrance(client,lastVisitforceaccess, Constants.IS_VIP_READER, entranceType, lastVisitRate));
        checkAccess(clientEntrance);
    }

    @Override
    public void checkAccess(Entrance entrance) {
        ClientEntrance clientEntrance = (ClientEntrance) entrance;
        if (clientEntrance.getClient().getDni() == null) {
            CardReaderHandler.getInstance().cardReaderError("DNI_API_ERROR");
        } else if (clientEntrance.getType().getName().equals("FORCED_ACCESS")) {
            CardReaderHandler.getInstance().changeCardReaderStatusUserJoin("ACCEPTED_FORCED", clientEntrance);
        } else if (clientEntrance.getType().getName().equals("JOIN")) {
            CardReaderHandler.getInstance().changeCardReaderStatusUserJoin("ACCEPTED_JOIN", clientEntrance);
        } else if (clientEntrance.getType().getName().equals("LEAVE")) {
            CardReaderHandler.getInstance().changeCardReaderStatusUserJoin("ACCEPTED_LEAVE", clientEntrance);
        } else if (clientEntrance.getType().getName().equals("DENIED_FULL")) {
            CardReaderHandler.getInstance().changeCardReaderStatus("ERROR_ROOM_FULL");
            CardReaderHandler.getInstance().changeCardReaderStatusUserJoin("ERROR_ROOM_FULL", clientEntrance);
        } else {
            CardReaderHandler.getInstance().changeCardReaderStatus("ERROR");
        }
    }
}
