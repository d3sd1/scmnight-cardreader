package model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;

public class ClientEntrancePricingTable extends RecursiveTreeObject<ClientEntrancePricingTable> {

    public IntegerProperty id;
    public StringProperty name;
    public FloatProperty price;

    public ClientEntrancePricingTable(ClientEntrancePricing entrance) {
        this.id = new SimpleIntegerProperty(entrance.getId());
        this.name = new SimpleStringProperty(entrance.getName());
        this.price = new SimpleFloatProperty(entrance.getPrice());
    }

}