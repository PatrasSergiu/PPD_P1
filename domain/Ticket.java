package domain;


import java.io.Serializable;

public class Ticket implements Serializable{
    private String name;
    private String time;
    private Double price;
    private Integer id;

    public Ticket(String name, String time, Double price, Integer id) {
        this.name = name;
        this.time = time;
        this.price = price;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + time + " " + price;
    }
}
