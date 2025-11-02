import model.Booking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class CustomerUI extends JFrame {

    private JTextField nameField;
    private JComboBox<String> movieList, timeList;
    private JComboBox<LocalDate> dateList;
    private JButton[][] seatButtons = new JButton[5][6];
    private JButton confirmButton;
    private JTextArea bookingDetails;

    private Set<String> selectedSeats = new HashSet<>();
    private Set<String> bookedSeats = new HashSet<>();
    private final String theatreName = "Tamil";

    public CustomerUI() {
        setTitle("🎬 Movie Ticket Booking");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        // Top Panel
        JPanel topPanel = new JPanel(new GridLayout(4,2,10,10));
        topPanel.add(new JLabel("Customer Name:"));
        nameField = new JTextField();
        topPanel.add(nameField);

        topPanel.add(new JLabel("Select Movie:"));
        String[] movies = {"Avengers", "Inception", "Interstellar", "Titanic"};
        movieList = new JComboBox<>(movies);
        topPanel.add(movieList);

        topPanel.add(new JLabel("Select Time:"));
        String[] times = {"10:00 AM","1:00 PM","4:00 PM","7:00 PM"};
        timeList = new JComboBox<>(times);
        topPanel.add(timeList);

        topPanel.add(new JLabel("Select Date:"));
        LocalDate today = LocalDate.now();
        dateList = new JComboBox<>(new LocalDate[]{today,today.plusDays(1),today.plusDays(2),today.plusDays(3)});
        topPanel.add(dateList);

        add(topPanel, BorderLayout.NORTH);

        // Seats Panel
        JPanel seatPanel = new JPanel(new GridLayout(5,6,5,5));
        char row = 'A';
        for(int i=0;i<5;i++){
            for(int j=0;j<6;j++){
                String seatCode = row+String.valueOf(j+1);
                JButton btn = new JButton(seatCode);
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                btn.setForeground(Color.WHITE);
                btn.setBackground(Color.GREEN);

                btn.addActionListener(e -> {
                    JButton b = (JButton) e.getSource();
                    String seat = b.getText();
                    if(bookedSeats.contains(seat)){
                        JOptionPane.showMessageDialog(this, "Seat " + seat + " already booked!");
                    } else if(selectedSeats.contains(seat)){
                        selectedSeats.remove(seat);
                        b.setBackground(Color.GREEN);
                    } else{
                        selectedSeats.add(seat);
                        b.setBackground(Color.YELLOW);
                    }
                    bookingDetails.setText("Selected Seats: " + selectedSeats);
                });

                seatButtons[i][j] = btn;
                seatPanel.add(btn);
            }
            row++;
        }
        add(seatPanel, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bookingDetails = new JTextArea(5,30);
        bookingDetails.setEditable(false);
        bottomPanel.add(new JScrollPane(bookingDetails), BorderLayout.CENTER);

        confirmButton = new JButton("Confirm Booking");
        bottomPanel.add(confirmButton, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        confirmButton.addActionListener(e -> sendBookingToServer());
        movieList.addActionListener(e -> loadBookedSeats());
        timeList.addActionListener(e -> loadBookedSeats());
        dateList.addActionListener(e -> loadBookedSeats());

        loadBookedSeats(); // initial
    }

    private void loadBookedSeats() {
        bookedSeats.clear();
        selectedSeats.clear();
        for(int i=0;i<5;i++){
            for(int j=0;j<6;j++) seatButtons[i][j].setBackground(Color.GREEN);
        }

        try(Socket socket = new Socket("localhost",12345);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())){

            oos.writeObject("GET_BOOKED_SEATS");
            oos.flush();
            oos.writeObject((String)movieList.getSelectedItem());
            oos.writeObject((String)timeList.getSelectedItem());
            oos.writeObject((LocalDate)dateList.getSelectedItem());
            oos.flush();

            List<String> booked = (List<String>) ois.readObject();
            bookedSeats.addAll(booked);

            for(int i=0;i<5;i++){
                for(int j=0;j<6;j++){
                    String seat = seatButtons[i][j].getText();
                    if(bookedSeats.contains(seat)) seatButtons[i][j].setBackground(Color.RED);
                }
            }

        } catch(Exception e){ JOptionPane.showMessageDialog(this, "Server Error: "+e.getMessage()); }
    }

    private void sendBookingToServer(){
        String name = nameField.getText().trim();
        if(name.isEmpty() || selectedSeats.isEmpty()){
            JOptionPane.showMessageDialog(this, "Enter name and select seats!");
            return;
        }

        Booking booking = new Booking(
                name,
                theatreName,
                (String)movieList.getSelectedItem(),
                (String)timeList.getSelectedItem(),
                (LocalDate)dateList.getSelectedItem(),
                String.join(",",selectedSeats)
        );

        try(Socket socket = new Socket("localhost",12345);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())){

            oos.writeObject(booking);
            oos.flush();
            String response = (String)ois.readObject();
            JOptionPane.showMessageDialog(this, response);

            selectedSeats.clear();
            bookingDetails.setText("");
            nameField.setText("");
            loadBookedSeats();

        } catch(Exception e){ JOptionPane.showMessageDialog(this, "Server Error: "+e.getMessage()); }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new CustomerUI().setVisible(true));
    }
}

