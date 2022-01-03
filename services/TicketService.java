package services;

import domain.Bill;
import domain.Ticket;
import domain.Seats;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketService {
    private ReentrantReadWriteLock lock;
    private HashMap<Integer, Seats> nrLocuri;
    private HashMap<String, Integer> soldTickets;
    private ConcurrentLinkedQueue<Bill> bills;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private Double lastCheckSold;
    private Double currentSold;

    public TicketService(){
        lock = new ReentrantReadWriteLock();
        nrLocuri = new HashMap<>();
        soldTickets = new HashMap<>();
        bills = new ConcurrentLinkedQueue<>();
        lastCheckSold = 0.0;
        currentSold = 0.0;

        LoadTickets();
    }

    public void Buy(String clientName, Integer ticketId, Integer quantity) {
        lock.readLock().lock();

        // Ticket not found
        if (!nrLocuri.containsKey(ticketId)){
            lock.readLock().unlock();
            throw new RuntimeException("Ticket: " + ticketId + "doesn't exist!");
        }
        // lock nrLocuri for the ticket with the given ticket id
        synchronized (nrLocuri.get(ticketId)) {
            Seats s = nrLocuri.get(ticketId);
            if (s.getQuantity() < quantity){
                lock.readLock().unlock();
                throw new RuntimeException("Quantity too small: " + ticketId);
            }
            // Update quantity
            s.setQuantity(s.getQuantity() - quantity);

            // Register new bill
            Ticket p = nrLocuri.get(ticketId).getTicket();
            if(soldTickets.containsKey(p.getName())) {
                soldTickets.put(p.getName(), quantity + soldTickets.get(p.getName()));
            }
            else {
                soldTickets.put(p.getName(), quantity);
            }
            Bill bill = new Bill(p.getId(), quantity, quantity * p.getPrice(), clientName);
            bills.add(bill);

            // Update current sold
            currentSold += (quantity * p.getPrice());
        }

        lock.readLock().unlock();
    }

    public void Check(){
        lock.writeLock().lock();

        try(FileWriter fw = new FileWriter("C:\\Users\\Patras Sergiu\\Desktop\\Teme facultate\\PPD_Proiect1\\P1 PPD\\log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.write("[ STARTED CHECKING -  " + dateFormat.format(new Date()) + " ]\n\n");

            Double totalBillsValue = CalculateTotalBills();
            if(totalBillsValue > 0) {
                out.write(" Vanzari:\n");
                for (Bill b : bills){
                    out.write("   " + b.getDate() + " " + b.getTicketId() + " " +
                            b.getClientName() + " " + b.getTotal()
                            + "LEI pentru " + b.getQuantity() + " bilete\n");
                }
            }
            out.write("\nUpdate-uri: \n");
            out.write(" Pre check  - " + lastCheckSold + "\n");
            out.write(" Sold this check - " + totalBillsValue + "\n");
            out.write(" Total Sold - " + currentSold + "\n");

            Double rt = totalBillsValue + lastCheckSold;

            if( Math.abs(rt - currentSold)  > 0.00001) {
                out.write("ERROR Corrupted sold!");
            }

            out.write("\nTotal sold seats: \n");
            for (var entry : soldTickets.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();
                out.write(" For the show: " + key + " we sold tickets from seats 1 to " + value + "\n");
            }

            out.write("\nLocuri ramase: \n");
            List<Seats> availableSeats = GetAvailableSeats();
            if(availableSeats.isEmpty()) {

            }
            for(Seats x : availableSeats) {
                Ticket ticket = x.getTicket();
                out.write(" " + x.getQuantity() + " locuri ramase la spectacolul " + ticket.getName() + "\n");
            }
            out.write("\n[ FINISHED CHECKING -  " + dateFormat.format(new Date()) + " ]\n\n");

            // Free bills
            lastCheckSold = currentSold;
            bills.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }

        lock.writeLock().unlock();
    }

    public List<Seats> GetAvailableSeats(){
        lock.readLock().lock();

        ArrayList<Seats> availableTickets = new ArrayList<>();
        for(Integer id : nrLocuri.keySet()){
            synchronized (nrLocuri.get(id)) {
                availableTickets.add(nrLocuri.get(id));
            }
        }

        lock.readLock().unlock();
        return availableTickets;
    }

    private Double CalculateTotalBills(){
        Double total = 0.0;
        for(Bill b : bills) {
            total += b.getTotal();
        }

        return total;
    }

    private void LoadTickets(){
        try(BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Patras Sergiu\\Desktop\\Teme facultate\\PPD_Proiect1\\P1 PPD\\tickets.txt"))) {
            String line;
            while (null != (line = br.readLine())) {
                String[] parts = line.split(",");
                System.out.println(line + "\n");

                Ticket p = new Ticket(parts[1],parts[2],Double.parseDouble(parts[3]), Integer.parseInt(parts[0]));
                Seats s = new Seats(p, Integer.parseInt(parts[4]));
                nrLocuri.put(p.getId(), s);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
