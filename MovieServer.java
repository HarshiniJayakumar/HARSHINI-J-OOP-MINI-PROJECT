import model.Booking;
import database.DBConnection;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class MovieServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🎬 Movie Booking Server running on port " + PORT);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try(
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ) {
            Object obj = ois.readObject();

            if(obj instanceof Booking) {
                Booking booking = (Booking) obj;
                saveBookingToDB(booking);
                oos.writeObject("✅ Booking Confirmed: " + booking.toString());
                oos.flush();
            } else if(obj instanceof String) {
                String request = (String) obj;

                if("ADMIN_VIEW".equals(request)) {
                    List<Booking> bookings = getAllBookings();
                    oos.writeObject(bookings);
                    oos.flush();
                } else if("GET_BOOKED_SEATS".equals(request)) {
                    // Receive details
                    String movie = (String) ois.readObject();
                    String time = (String) ois.readObject();
                    LocalDate date = (LocalDate) ois.readObject();
                    List<String> bookedSeats = getBookedSeats(movie, time, date);
                    oos.writeObject(bookedSeats);
                    oos.flush();
                }
            }

        } catch(Exception e) { e.printStackTrace(); }
    }

    private void saveBookingToDB(Booking booking) {
        try(Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO bookings(customer_name, theatre, movie, show_time, show_date, seats) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, booking.getCustomerName());
            ps.setString(2, booking.getTheatre());
            ps.setString(3, booking.getMovie());
            ps.setString(4, booking.getShowTime());
            ps.setDate(5, java.sql.Date.valueOf(booking.getShowDate()));
            ps.setString(6, booking.getSeats());
            ps.executeUpdate();
            ps.close();
        } catch(SQLException ex) { ex.printStackTrace(); }
    }

    private List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        try(Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM bookings");
            while(rs.next()) {
                Booking b = new Booking(
                        rs.getString("customer_name"),
                        rs.getString("theatre"),
                        rs.getString("movie"),
                        rs.getString("show_time"),
                        rs.getDate("show_date").toLocalDate(),
                        rs.getString("seats")
                );
                list.add(b);
            }
            rs.close();
            stmt.close();
        } catch(SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    private List<String> getBookedSeats(String movie, String time, LocalDate date){
        List<String> seats = new ArrayList<>();
        try(Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT seats FROM bookings WHERE movie=? AND show_time=? AND show_date=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, movie);
            ps.setString(2, time);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String[] booked = rs.getString("seats").split(",");
                seats.addAll(Arrays.asList(booked));
            }
            rs.close();
            ps.close();
        } catch(SQLException ex) { ex.printStackTrace(); }
        return seats;
    }
}
