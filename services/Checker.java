package services;

import controller.TicketController;

import java.util.concurrent.Future;

public class Checker extends Thread{
    private TicketController ticketController;
    private Boolean stop;

    public Checker(TicketController ticketController) {
        this.ticketController = ticketController;
        this.stop = false;
    }


    public void Stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        while(!stop) {
            Future<Void> v = ticketController.Check();
            try {
                v.get();
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
