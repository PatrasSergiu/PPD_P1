package controller;

import domain.Seats;
import services.TicketService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TicketController {
    private TicketService ticketService;
    private ExecutorService executorService;
    private int threads;

    public TicketController(TicketService ticketService, int th) {
        this.ticketService = ticketService;
        this.threads = th;
        this.executorService = Executors.newFixedThreadPool(threads);
    }

    public void Stop(){
        executorService.shutdownNow();
    }

    public Future<Void> Check(){
        Callable<Void> worker = new CheckCallback();
        return executorService.submit(worker);
    }

    public Future<Boolean> Buy(String clientName, Integer ticketId, Integer quantity){
        Callable<Boolean> worker = new BuyCallback(clientName, ticketId, quantity);
        return executorService.submit(worker);
    }

    public Future<List<Seats>> GetSeats(){
        Callable<List<Seats>> worker = new GetSeatsCallback();
        return executorService.submit(worker);
    }

    private class CheckCallback implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            ticketService.Check();
            return null;
        }
    }

    private class BuyCallback implements Callable<Boolean> {
        private String clientName;
        private Integer ticketId;
        private Integer quantity;

        BuyCallback(String clientName, Integer ticketId, Integer quantity) {
            this.clientName = clientName;
            this.ticketId = ticketId;
            this.quantity = quantity;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                ticketService.Buy(clientName, ticketId, quantity);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private class GetSeatsCallback implements Callable<List<Seats>>{
        @Override
        public List<Seats> call() throws Exception {
            return ticketService.GetAvailableSeats();
        }
    }
}
