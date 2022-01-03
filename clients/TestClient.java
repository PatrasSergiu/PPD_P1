package clients;

import domain.Seats;
import request.BuyRequest;
import request.GetSeatsRequest;
import response.BuyResponse;
import response.GetSeatsResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class TestClient {

    private static List<Seats> spectacole;
    private static Random random = new Random();

    private static BuyRequest CreateBuyRequest() {
        Integer id = random.nextInt(spectacole.size() + 1);
        Integer nr_locuri = random.nextInt(5) + 1;

        return new BuyRequest("TestClient" + String.valueOf(random.nextInt(5)), id, nr_locuri);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 4444);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

        outputStream.writeObject(new GetSeatsRequest());

        spectacole = ((GetSeatsResponse) inputStream.readObject()).getResponse();
        // create buy requests until server throws exception
        while (true) {
            try {
                outputStream.writeObject(CreateBuyRequest());
                BuyResponse response = (BuyResponse) inputStream.readObject();// check result of buy request
                if (response.getOkResponse()) {
                    System.out.println("Cumparat cu succes!");
                } else if (response.getOkResponse() == null) {
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                    return;
                } else {
                    System.out.println("Cumparare esuata.");
                }
                Thread.sleep(2000);
            } catch (Exception ignored) {
                System.out.println("Server has shutdown");
                break;
            }
        }
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
