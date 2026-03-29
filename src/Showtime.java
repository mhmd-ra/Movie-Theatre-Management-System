import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Showtime {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty movieTitle;
    private final SimpleStringProperty roomName;
    private final SimpleStringProperty showDate;
    private final SimpleStringProperty showTime;
    private final SimpleIntegerProperty availableSeats;

    public Showtime(int id, String movieTitle, String roomName, String showDate, String showTime, int availableSeats) {
        this.id = new SimpleIntegerProperty(id);
        this.movieTitle = new SimpleStringProperty(movieTitle);
        this.roomName = new SimpleStringProperty(roomName);
        this.showDate = new SimpleStringProperty(showDate);
        this.showTime = new SimpleStringProperty(showTime);
        this.availableSeats = new SimpleIntegerProperty(availableSeats);
    }

    public int getId()              { return id.get(); }
    public String getMovieTitle()   { return movieTitle.get(); }
    public String getRoomName()     { return roomName.get(); }
    public String getShowDate()     { return showDate.get(); }
    public String getShowTime()     { return showTime.get(); }
    public int getAvailableSeats()  { return availableSeats.get(); }
}
