import java.io.Serializable;
import java.time.LocalDate;

public class Booking implements Serializable {
    private String customerName;
    private String theatre;
    private String movie;
    private String showTime;
    private LocalDate showDate;
    private String seats;

    public Booking(String customerName, String theatre, String movie, String showTime, LocalDate showDate, String seats) {
        this.customerName = customerName;
        this.theatre = theatre;
        this.movie = movie;
        this.showTime = showTime;
        this.showDate = showDate;
        this.seats = seats;
    }

    public String getCustomerName() { return customerName; }
    public String getTheatre() { return theatre; }
    public String getMovie() { return movie; }
    public String getShowTime() { return showTime; }
    public LocalDate getShowDate() { return showDate; }
    public String getSeats() { return seats; }

    @Override
    public String toString() {
        return "Name: " + customerName + " | Theatre: " + theatre +
               " | Movie: " + movie + " | Date: " + showDate +
               " | Time: " + showTime + " | Seats: " + seats;
    }
}
