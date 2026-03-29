import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Movie {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty genre;
    private final SimpleIntegerProperty durationMinutes;
    private final SimpleStringProperty rating;

    public Movie(int id, String title, String genre, int durationMinutes, String rating) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.genre = new SimpleStringProperty(genre);
        this.durationMinutes = new SimpleIntegerProperty(durationMinutes);
        this.rating = new SimpleStringProperty(rating);
    }

    public int getId()              { return id.get(); }
    public String getTitle()        { return title.get(); }
    public String getGenre()        { return genre.get(); }
    public int getDurationMinutes() { return durationMinutes.get(); }
    public String getRating()       { return rating.get(); }
}
