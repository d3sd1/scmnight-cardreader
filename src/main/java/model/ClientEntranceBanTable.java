package model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;

public class ClientEntranceBanTable extends RecursiveTreeObject<ClientEntranceBanTable> {

    public IntegerProperty id;
    public StringProperty name;

    public ClientEntranceBanTable(ClientBan clientBan) {
        this.id = new SimpleIntegerProperty(clientBan.getId());
        this.name = new SimpleStringProperty(clientBan.getName());
    }

}