package Extends;

import model.GuiPanel;
import model.ClientEntrance;

public class PanelCommon
{

    public GuiPanel loadPanel(String panelCode)
    {
        return loadPanel(panelCode, null);
    }

    public GuiPanel loadPanel(String panelCode, ClientEntrance userEntrance)
    {
        GuiPanel panel = new GuiPanel("#ffffff", "#000000", "Estado no reconocido", "", "", 0);
        String userData = "";
        if(null != userEntrance && null != userEntrance.getClient())
        {
            userData = userEntrance.getClient().getName() + " " + userEntrance.getClient().getSurname1() + " " + userEntrance.getClient().getSurname2();
        }
        switch (panelCode.toUpperCase())
        {
            case "UPDATING":
                panel.setTextColor("#5bc0de");
                panel.setMainText("Actualizando...");
                break;
            case "WAITING":
                panel.setTextColor("#117a8b");
                panel.setMainText("Esperando tarjeta");
                break;
            case "LOADING":
                panel.setTextColor("#117a8b");
                panel.setMainText("Cargando...");
                break;
            case "LOADED":
                panel.setTextColor("#117a8b");
                panel.setMainText("¡Cargado!");
                break;
            case "EXTRACT":
                panel.setTextColor("#d39e00");
                panel.setMainText("Retira tarjeta");
                break;
            case "READING":
                panel.setTextColor("#0062cc");
                panel.setMainText("Leyendo tarjeta");
                break;
            case "ERROR":
                panel.setTextColor("#bd2130");
                panel.setMainText("Tarjeta no aceptada");
                break;
            case "WAITING_RATE":
                panel.setTextColor("#117a8b");
                panel.setMainText("Esperando selección de tarifa");
                break;
            case "WAITING_INPUT_DATA":
                panel.setTextColor("#117a8b");
                panel.setMainText("Esperando datos validados");
                break;
            case "ERROR_CONFLICTIVE":
                panel.setTextColor("#bd2130");
                panel.setMainText("Cliente conflictivo");
                panel.setBottomText(userData);
                break;
            case "ERROR_ROOM_FULL":
                panel.setTextColor("#bd2130");
                panel.setMainText("Sala llena");
                panel.setBottomText(userData);
                break;
            case "ACCEPTED_LEAVE":
                panel.setTextColor("#1e7e34");
                panel.setMainText("Salida de cliente");
                panel.setBottomText(userData);
                break;
            case "ACCEPTED_JOIN":
                panel.setTextColor("#1e7e34");
                panel.setMainText("Entrada de cliente");
                panel.setBottomText(userData);
                break;
            case "ACCEPTED_FORCED":
                panel.setTextColor("#1e7e34");
                panel.setMainText("Entrada de cliente (Forzada)");
                panel.setBottomText(userData);
                break;
        }
        return panel;
    }
}
