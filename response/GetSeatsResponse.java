package response;

import domain.Seats;

import java.io.Serializable;
import java.util.List;

public class GetSeatsResponse implements Serializable {
    private List<Seats> response;

    public GetSeatsResponse(List<Seats> response) {
        this.response = response;
    }

    public List<Seats> getResponse() {
        return response;
    }

    public void setResponse(List<Seats> response) {
        this.response = response;
    }
}
