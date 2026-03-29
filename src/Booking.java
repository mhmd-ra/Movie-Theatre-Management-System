import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Booking {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty movieTitle;
    private final SimpleStringProperty showDate;
    private final SimpleStringProperty showTime;
    private final SimpleIntegerProperty seatsBooked;
    private final SimpleStringProperty totalPrice;
    private final SimpleStringProperty status;

    public Booking(int id, String movieTitle, String showDate, String showTime,
                   int seatsBooked, String totalPrice, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.movieTitle = new SimpleStringProperty(movieTitle);
        this.showDate = new SimpleStringProperty(showDate);
        this.showTime = new SimpleStringProperty(showTime);
        this.seatsBooked = new SimpleIntegerProperty(seatsBooked);
        this.totalPrice = new SimpleStringProperty(totalPrice);
        this.status = new SimpleStringProperty(status);
    }

    public int getId()              { return id.get(); }
    public String getMovieTitle()   { return movieTitle.get(); }
    public String getShowDate()     { return showDate.get(); }
    public String getShowTime()     { return showTime.get(); }
    public int getSeatsBooked()     { return seatsBooked.get(); }
    public String getTotalPrice()   { return totalPrice.get(); }
    public String getStatus()       { return status.get(); }
}
