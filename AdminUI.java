
import model.Booking;
import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class AdminUI extends JFrame {

    private JTextArea bookingsArea;
    private JButton refreshBtn;

    public AdminUI(){
        setTitle("🎟️ Admin - Booked Customers");
        setSize(700,500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        bookingsArea = new JTextArea();
        bookingsArea.setFont(new Font("Monospaced",Font.PLAIN,12));
        bookingsArea.setEditable(false);
        add(new JScrollPane(bookingsArea),BorderLayout.CENTER);

        refreshBtn = new JButton("Refresh Bookings");
        add(refreshBtn,BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadBookings());

        loadBookings();
    }

    private void loadBookings(){
        try(Socket socket = new Socket("localhost",12345);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())){

            oos.writeObject("ADMIN_VIEW");
            oos.flush();

            List<Booking> bookings = (List<Booking>) ois.readObject();

            bookingsArea.setText(String.format("%-15s %-10s %-20s %-12s %-12s %-15s\n",
                    "Customer","Theatre","Movie","Date","Show Time","Seats"));
            bookingsArea.append("-------------------------------------------------------------------------------\n");

            for(Booking b : bookings){
                bookingsArea.append(String.format("%-15s %-10s %-20s %-12s %-12s %-15s\n",
                        b.getCustomerName(),
                        b.getTheatre(),
                        b.getMovie(),
                        b.getShowDate(),
                        b.getShowTime(),
                        b.getSeats()));
            }

        } catch(Exception e){ JOptionPane.showMessageDialog(this,"Server Error: "+e.getMessage()); }
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new AdminUI().setVisible(true)); }
}
