package request;

import java.io.Serializable;

public class BuyRequest implements Serializable{
    private String clientName;
    private Integer ticketId;
    private Integer quantity;

    public BuyRequest(String clientName, Integer ticketId, Integer quantity) {
        this.clientName = clientName;
        this.ticketId = ticketId;
        this.quantity = quantity;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
