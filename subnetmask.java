import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class subnetmask {
    private static final long IPV4_SIZE = 1L << 32;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(subnetmask::createWindow);
    }

    private static void createWindow() {
        JFrame frame = new JFrame("Subnetting Helper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 440);
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(new Color(245, 248, 252));

        JLabel title = new JLabel("Subnetting Helper");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(32, 54, 83));
        content.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 12));
        center.setOpaque(false);

        JPanel inputs = new JPanel(new GridLayout(2, 2, 10, 10));
        inputs.setOpaque(false);
        inputs.add(new JLabel("IPv4 address:"));
        JTextField addressField = new JTextField("192.168.1.10");
        inputs.add(addressField);
        inputs.add(new JLabel("Usable hosts needed:"));
        JTextField hostsField = new JTextField("50");
        inputs.add(hostsField);
        center.add(inputs, BorderLayout.NORTH);

        JTextArea results = new JTextArea(9, 34);
        results.setEditable(false);
        results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        results.setBackground(Color.WHITE);
        results.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        results.setText("Enter an address and host count, then select Calculate.");
        center.add(new JScrollPane(results), BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);

        JButton calculateButton = new JButton("Calculate subnet");
        calculateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        calculateButton.addActionListener(event -> {
            try {
                long address = parseIpv4Address(addressField.getText().trim());
                long requiredHosts = Long.parseLong(hostsField.getText().trim());
                Subnet subnet = calculateSubnet(address, requiredHosts);

                results.setText("Subnet mask:       " + formatIpv4Address(subnet.mask) + "\n"
                        + "CIDR prefix:       /" + subnet.prefix + "\n"
                        + "Network address:   " + formatIpv4Address(subnet.network) + "\n"
                        + "First usable host: " + formatIpv4Address(subnet.network + 1) + "\n"
                        + "Last usable host:  " + formatIpv4Address(subnet.broadcast - 1) + "\n"
                        + "Broadcast address: " + formatIpv4Address(subnet.broadcast) + "\n"
                        + "Usable hosts:      " + (subnet.size - 2));
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(frame,
                        "Enter a whole number for the host count.",
                        "Invalid host count",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(frame,
                        exception.getMessage(),
                        "Invalid input",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        content.add(calculateButton, BorderLayout.SOUTH);

        frame.setContentPane(content);
        frame.getRootPane().setDefaultButton(calculateButton);
        frame.setVisible(true);
    }

    private static Subnet calculateSubnet(long address, long requiredHosts) {
        if (requiredHosts < 1) {
            throw new IllegalArgumentException("Enter a host count greater than zero.");
        }
        if (requiredHosts > IPV4_SIZE - 2) {
            throw new IllegalArgumentException("That host count is too large for an IPv4 subnet.");
        }

        long size = 4;
        int hostBits = 2;
        while (size - 2 < requiredHosts) {
            size <<= 1;
            hostBits++;
        }

        int prefix = 32 - hostBits;
        long mask = (IPV4_SIZE - size) & 0xFFFFFFFFL;
        long network = address & mask;
        long broadcast = network + size - 1;
        return new Subnet(prefix, size, mask, network, broadcast);
    }

    private static long parseIpv4Address(String address) {
        String[] octets = address.split("\\.", -1);
        if (octets.length != 4) {
            throw new IllegalArgumentException("Enter an IPv4 address with four numbers, such as 192.168.1.10.");
        }

        long result = 0;
        for (String octet : octets) {
            int value;
            try {
                if (octet.isEmpty()) {
                    throw new NumberFormatException();
                }
                value = Integer.parseInt(octet);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Each part of the IPv4 address must be a number from 0 to 255.");
            }

            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("Each part of the IPv4 address must be a number from 0 to 255.");
            }
            result = (result << 8) | value;
        }
        return result;
    }

    private static String formatIpv4Address(long address) {
        return ((address >> 24) & 255) + "."
                + ((address >> 16) & 255) + "."
                + ((address >> 8) & 255) + "."
                + (address & 255);
    }

    private static class Subnet {
        private final int prefix;
        private final long size;
        private final long mask;
        private final long network;
        private final long broadcast;

        private Subnet(int prefix, long size, long mask, long network, long broadcast) {
            this.prefix = prefix;
            this.size = size;
            this.mask = mask;
            this.network = network;
            this.broadcast = broadcast;
        }
    }
}