package startServer;

import controller.TicketController;
import domain.Seats;
import request.BuyRequest;
import request.GetSeatsRequest;
import response.BuyResponse;
import response.GetSeatsResponse;
import services.Checker;
import services.TicketService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {
    public int threads;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private TicketController ticketController;
    Timer timer;
    static boolean alive = true;

    Server(int th) {
        threads = th;
        timer = new Timer();
    }

    public void start(int port, TicketController ticketController) throws IOException {
        try {
            serverSocket = new ServerSocket(port);
            //serverSocket.setSoTimeout(10000);
            timer.schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Server shut down.");
                                stop();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    3000
            );
            this.ticketController = ticketController;
            executorService = Executors.newFixedThreadPool(threads);
            while (true)
                executorService.submit(new EchoClientHandler(serverSocket.accept(), ticketController));
        }
        catch(SocketException ex) {
            System.out.println("Server has succesfully shut down.");
        }
    }

    public void stop() throws IOException {
        alive = false;
        timer.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            executorService.shutdownNow();
                            ticketController.Stop();
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                3000
        );
    }

    public static void main(String[] args) throws Exception{
        int threads = Integer.parseInt(String.valueOf(args[0]));
        TicketService p = new TicketService();
        TicketController c = new TicketController(p, threads);
        Server s = new Server(threads);

        // Start checker
        Checker checker = new Checker(c);
        checker.start();

        s.start(4444, c);


        checker.Stop();
        checker.join();
        return;
    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        private TicketController ticketController;

        EchoClientHandler(Socket socket, TicketController ticketController) {
            this.clientSocket = socket;
            this.ticketController = ticketController;
        }

        public void run() {
            try {
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());

                while(true){
                    Object request = inputStream.readObject();
                    if (request instanceof GetSeatsRequest){
                        outputStream.writeObject(HandleGetSeats());
                    }
                    else if (request instanceof BuyRequest){
                        outputStream.writeObject(HandleBuyRequest((BuyRequest)request));
                    }
                    else break;

                    outputStream.reset();
                }

                inputStream.close();
                outputStream.close();
                this.clientSocket.close();

            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception!!");
            }
        }

        GetSeatsResponse HandleGetSeats() throws ExecutionException, InterruptedException {
            Future<List<Seats>> nrLocuri = ticketController.GetSeats();
            return new GetSeatsResponse(nrLocuri.get());
        }

        BuyResponse HandleBuyRequest(BuyRequest request) throws ExecutionException, InterruptedException {
            Future<Boolean> buy = ticketController.Buy(request.getClientName(), request.getTicketId(), request.getQuantity());
            if(alive) {
                return new BuyResponse(buy.get());
            }
            else {
                return new BuyResponse(null);
            }
        }

    }
}
