package com.cybercafe.billing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * Main GUI application for the Cybercafe Billing System.
 */
public class CybercafeBillingSystem {

    private static final double RATE_PER_HOUR = 5000.0;
    private static final double VIP_DISCOUNT = 0.2;

    private JFrame frame;
    private JTextField customerNameField;
    private JLabel statusLabel;
    private JLabel durationLabel;
    private JLabel billLabel;
    private JButton startButton;
    private JButton endButton;
    private JButton payButton;
    private JComboBox<String> sessionTypeCombo;
    private DefaultListModel<String> orderListModel;
    private JList<String> orderList;
    private JComboBox<Product> productCombo;
    private JButton addProductButton;

    private Session currentSession;
    private Order currentOrder;
    private Timer timer;
    private DecimalFormat currencyFormat;

    // Sample product list
    private Product[] productsAvailable = {
            new Product("Snack", 10000),
            new Product("Drink", 7000),
            new Product("Coffee", 15000),
            new Product("Water", 5000)
    };

    public CybercafeBillingSystem() {
        currencyFormat = new DecimalFormat("Rp #,##0.00");
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Cybercafe Billing System");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.setContentPane(panel);

        // Customer Name entry
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel nameLabel = new JLabel("Customer Name:");
        customerNameField = new JTextField(20);
        namePanel.add(nameLabel);
        namePanel.add(customerNameField);
        panel.add(namePanel);

        // Session type dropdown
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel typeLabel = new JLabel("Session Type:");
        sessionTypeCombo = new JComboBox<>(new String[]{"Normal", "VIP (20% Discount)"});
        typePanel.add(typeLabel);
        typePanel.add(sessionTypeCombo);
        panel.add(typePanel);

        // Buttons: Start, End, Pay
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        startButton = new JButton("Start Session");
        endButton = new JButton("End Session");
        endButton.setEnabled(false);
        payButton = new JButton("Pay");
        payButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(endButton);
        buttonPanel.add(payButton);
        panel.add(buttonPanel);

        // Status and billing display
        statusLabel = new JLabel("Status: Waiting to start");
        durationLabel = new JLabel("Duration: 0 hours 0 minutes");
        billLabel = new JLabel("Bill: Rp 0.00");
        panel.add(statusLabel);
        panel.add(durationLabel);
        panel.add(billLabel);

        // Product selection UI
        JLabel productLabel = new JLabel("Select Product:");
        productCombo = new JComboBox<>(productsAvailable);
        addProductButton = new JButton("Add Product");
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        productPanel.add(productLabel);
        productPanel.add(productCombo);
        productPanel.add(addProductButton);
        panel.add(productPanel);

        // List ordered products
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
        orderList.setBorder(BorderFactory.createTitledBorder("Order List"));
        JScrollPane scrollPane = new JScrollPane(orderList);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        panel.add(scrollPane);

        // Buttons listeners
        startButton.addActionListener(e -> startSession());
        endButton.addActionListener(e -> endSession());
        payButton.addActionListener(e -> payBill());
        addProductButton.addActionListener(e -> addProductToOrder());

        // Timer updates duration and bill every minute (60000 ms)
        timer = new Timer(60000, e -> updateDurationAndBill());

        frame.setVisible(true);
    }

    private void startSession() {
        String name = customerNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a customer name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sessionType = (String) sessionTypeCombo.getSelectedItem();
        if ("Normal".equals(sessionType)) {
            currentSession = new NormalSession(name, RATE_PER_HOUR);
        } else {
            currentSession = new VIPDiscountSession(name, RATE_PER_HOUR, VIP_DISCOUNT);
        }

        currentOrder = new Order();

        statusLabel.setText("Status: Session started for " + currentSession.getCustomerName());
        durationLabel.setText("Duration: 0 hours 0 minutes");
        billLabel.setText("Bill: Rp 0.00");

        startButton.setEnabled(false);
        endButton.setEnabled(true);
        payButton.setEnabled(false);
        customerNameField.setEnabled(false);
        sessionTypeCombo.setEnabled(false);

        orderListModel.clear();

        timer.start();
    }

    private void endSession() {
        if (currentSession != null) {
            currentSession.endSession();
            timer.stop();
            updateDurationAndBill();
            statusLabel.setText("Status: Session ended for " + currentSession.getCustomerName());
            endButton.setEnabled(false);
            payButton.setEnabled(true);
        }
    }

    private void updateDurationAndBill() {
        if (currentSession == null) return;

        durationLabel.setText("Duration: " + currentSession.getDurationFormatted());

        double sessionBill = currentSession.calculateBill();
        double productsBill = currentOrder.getTotalPrice();
        double totalBill = sessionBill + productsBill;

        billLabel.setText("Bill: " + currencyFormat.format(totalBill));
    }

    private void addProductToOrder() {
        if (currentSession == null) {
            JOptionPane.showMessageDialog(frame, "Start a session before adding products.", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = (Product) productCombo.getSelectedItem();
        if (selectedProduct != null) {
            currentOrder.addProduct(selectedProduct);
            orderListModel.addElement(selectedProduct.getName() + " - " + currencyFormat.format(selectedProduct.getPrice()));
            updateDurationAndBill();
        }
    }

    private void payBill() {
        if (currentSession == null) return;

        String receipt = generateReceipt();

        String customerName = currentSession.getCustomerName();
        String sessionType = currentSession instanceof VIPDiscountSession ? "VIP" : "Normal";
        String duration = currentSession.getDurationFormatted();
        double sessionBill = currentSession.calculateBill();
        double productsBill = currentOrder.getTotalPrice();
        double totalBill = sessionBill + productsBill;
        // Simpan receipt ke database
        DatabaseManager.saveReceipt(customerName, sessionType, duration,
                                    sessionBill, productsBill, totalBill, receipt);
        // -------------------------------------------------------
        // Create receipt display area
        JTextArea receiptArea = new JTextArea(receipt);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Create action buttons
        JButton saveBtn = new JButton("Save Receipt");
        JButton printBtn = new JButton("Print");
        JButton closeBtn = new JButton("Close");

        // Create dialog first and make it final
        final JDialog receiptDialog = new JDialog(frame, "Payment Receipt", true);
        
        saveBtn.addActionListener(e -> saveReceipt(receipt));
        printBtn.addActionListener(e -> printReceipt(receipt));
        closeBtn.addActionListener(e -> {
            resetSession();
            receiptDialog.dispose();  // Now this will work
        });

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveBtn);
        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);

        // Main panel layout
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Configure dialog
        receiptDialog.setContentPane(panel);
        receiptDialog.pack();
        receiptDialog.setLocationRelativeTo(frame);
        receiptDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        receiptDialog.setVisible(true);
    }

    private String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        
        // Add header
        receipt.append("================================\n");
        receipt.append("       CYBER CAFE RECEIPT       \n");
        receipt.append("================================\n\n");
        
        // Customer info
        receipt.append("Customer: ").append(currentSession.getCustomerName()).append("\n");
        receipt.append("Session Type: ").append(currentSession instanceof VIPDiscountSession ? "VIP" : "Normal").append("\n");
        receipt.append("Duration: ").append(currentSession.getDurationFormatted()).append("\n\n");
        
        // Session charges
        double sessionBill = currentSession.calculateBill();
        receipt.append("Session Charges:\n");
        receipt.append(String.format("%-20s %10s\n", "Base Rate", currencyFormat.format(RATE_PER_HOUR) + "/hr"));
        if (currentSession instanceof VIPDiscountSession) {
            receipt.append(String.format("%-20s %10s\n", "VIP Discount (20%)", currencyFormat.format(sessionBill - (sessionBill / 0.8))));
        }
        receipt.append(String.format("%-20s %10s\n\n", "Session Total", currencyFormat.format(sessionBill)));
        
        // Product charges
        if (!currentOrder.getProducts().isEmpty()) {
            receipt.append("Products Ordered:\n");
            for (Product p : currentOrder.getProducts()) {
                receipt.append(String.format("%-20s %10s\n", p.getName(), currencyFormat.format(p.getPrice())));
            }
            receipt.append(String.format("%-20s %10s\n\n", "Products Total", currencyFormat.format(currentOrder.getTotalPrice())));
        }
        
        // Grand total
        double totalBill = sessionBill + currentOrder.getTotalPrice();
        receipt.append(String.format("%-20s %10s\n", "GRAND TOTAL", currencyFormat.format(totalBill)));
        receipt.append("\nThank you for your business!\n");
        
        return receipt.toString();
    }

    private void saveReceipt(String receipt) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Receipt As");
        fileChooser.setSelectedFile(new File("CyberCafe_Receipt_" + System.currentTimeMillis() + ".txt")); // Default filename
        
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) { // Use frame as parent
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.write(receipt); // Write the receipt to the file
                JOptionPane.showMessageDialog(frame, "Receipt saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE); // Use frame as parent
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(frame, "Error saving receipt: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE); // Use frame as parent
            }
        }
    }

    private void printReceipt(String receipt) {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            if (job.printDialog()) { // Show print dialog
                job.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if (pageIndex > 0) return Printable.NO_SUCH_PAGE; // Only one page
                    
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    
                    String[] lines = receipt.split("\n"); // Split receipt into lines
                    int y = 20; // Starting Y position
                    for (String line : lines) {
                        g2d.drawString(line, 20, y); // Draw each line
                        y += 15; // Move down for the next line
                    }
                    return Printable.PAGE_EXISTS; // Indicate that the page exists
                });
                job.print(); // Print the job
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(frame, "Error printing receipt: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE); // Use frame as parent
        }
    }

    private void resetSession() {
        currentSession = null;
        currentOrder = null;

        statusLabel.setText("Status: Waiting to start");
        durationLabel.setText("Duration: 0 hours 0 minutes");
        billLabel.setText("Bill: Rp 0.00");

        startButton.setEnabled(true);
        endButton.setEnabled(false);
        payButton.setEnabled(false);

        customerNameField.setEnabled(true);
        customerNameField.setText("");
        sessionTypeCombo.setEnabled(true);

        orderListModel.clear();
    }

    public static void main(String[] args) {
        // Apply Nimbus look & feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // ignore; fallback to default
        }

        SwingUtilities.invokeLater(() -> new CybercafeBillingSystem());
    }
}
