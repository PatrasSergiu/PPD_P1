package domain;


import java.io.Serializable;

public class Seats implements Serializable{
    private Ticket ticket;
    private Integer quantity;

    public Seats(Ticket ticket, Integer quantity) {
        this.ticket = ticket;
        this.quantity = quantity;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return ticket.toString() + "," + quantity;
    }
}
