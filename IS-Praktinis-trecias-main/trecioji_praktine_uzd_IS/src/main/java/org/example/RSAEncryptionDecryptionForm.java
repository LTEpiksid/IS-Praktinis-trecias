package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;

public class RSAEncryptionDecryptionForm {
    private JPanel mainPanel;
    private JLabel imageLabel;
    private JTextField pField;
    private JTextField qField;
    private JTextArea outputArea;
    private JButton insertXButton;
    private JButton saveToFileButton;
    private JButton loadFromFileButton;
    private BigInteger[] encrypted;
    private BigInteger n;

    public RSAEncryptionDecryptionForm() {
        initializeUI();
        setUpListeners();
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Logo Panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/Logo.png"));
        if (imageIcon != null) {
            imageLabel.setIcon(imageIcon);
        } else {
            System.err.println("Failed to load image: Logo.png");
        }
        logoPanel.add(imageLabel, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inputPanel.add(new JLabel("Enter p:"));
        pField = new JTextField(10);
        inputPanel.add(pField);
        inputPanel.add(new JLabel("Enter q:"));
        qField = new JTextField(10);
        inputPanel.add(qField);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        insertXButton = new JButton("Insert x");
        saveToFileButton = new JButton("Save to File");
        loadFromFileButton = new JButton("Load from File");
        buttonPanel.add(insertXButton);
        buttonPanel.add(saveToFileButton);
        buttonPanel.add(loadFromFileButton);
        inputPanel.add(buttonPanel);

        // Output Panel
        outputArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Add components to main panel
        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Create frame
        JFrame frame = new JFrame("RSA Encryption Decryption Form");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private void setUpListeners() {
        insertXButton.addActionListener(e -> {
            performEncryption();
        });

        saveToFileButton.addActionListener(e -> {
            saveEncryptedTextToFile();
        });

        loadFromFileButton.addActionListener(e -> {
            loadEncryptedTextFromFile();
        });
    }

    private void performEncryption() {
        BigInteger p;
        BigInteger q;

        try {
            p = new BigInteger(pField.getText());
            q = new BigInteger(qField.getText());
        } catch (NumberFormatException ex) {
            outputArea.setText("Please enter valid numbers for p and q.");
            return;
        }

        if (!isPrime(p) || !isPrime(q)) {
            outputArea.setText("p and q must be prime numbers.");
            return;
        }

        String x = JOptionPane.showInputDialog("Enter x:");
        if (x == null) return;

        try {
            Long.parseLong(x);
        } catch (NumberFormatException ex) {
            outputArea.setText("Please enter valid number.");
            return;
        }

        n = p.multiply(q);
        BigInteger fi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger eValue = findE(fi);
        BigInteger d = findD(eValue, fi);

        outputArea.setText("n: " + n.toString() +
                "\nfi n: " + fi.toString() +
                "\ne: " + eValue.toString() +
                "\nd: " + d.toString());

        encrypted = encrypt(x, n, eValue);
        String encryptedText = stringify(encrypted);

        String decryptedText = decrypt(encrypted, n, d);

        outputArea.append("\n\nEncrypted text: " + encryptedText + "\nDecrypted text: " + decryptedText);

        saveToFileButton.setEnabled(true);

        writeToFile("encrypted_text.txt", encrypted, n);
    }

    private boolean isPrime(BigInteger number) {
        if (number.compareTo(BigInteger.ONE) <= 0) return false;
        if (number.equals(BigInteger.TWO)) return true;
        if (number.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false;

        BigInteger sqrt = sqrt(number);
        for (BigInteger i = BigInteger.valueOf(3); i.compareTo(sqrt) <= 0; i = i.add(BigInteger.TWO)) {
            if (number.mod(i).equals(BigInteger.ZERO)) return false;
        }
        return true;
    }

    private BigInteger sqrt(BigInteger n) {
        BigInteger a = BigInteger.ONE;
        BigInteger b = n.shiftRight(5).add(BigInteger.valueOf(8));

        while (b.compareTo(a) >= 0) {
            BigInteger mid = a.add(b).shiftRight(1);
            if (mid.multiply(mid).compareTo(n) > 0) {
                b = mid.subtract(BigInteger.ONE);
            } else {
                a = mid.add(BigInteger.ONE);
            }
        }
        return a.subtract(BigInteger.ONE);
    }

    private BigInteger findE(BigInteger fi) {
        for (BigInteger e = BigInteger.TWO; e.compareTo(fi) < 0; e = e.add(BigInteger.ONE)) {
            if (e.gcd(fi).equals(BigInteger.ONE)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Error");
    }

    private BigInteger findD(BigInteger e, BigInteger fi) {
        BigInteger fi0 = fi;
        BigInteger y = BigInteger.ZERO;
        BigInteger x = BigInteger.ONE;

        if (fi.equals(BigInteger.ONE)) {
            return BigInteger.ZERO;
        }

        while (e.compareTo(BigInteger.ONE) > 0) {
            BigInteger[] divMod = e.divideAndRemainder(fi);
            BigInteger q = divMod[0];
            BigInteger t = fi;

            fi = divMod[1];
            e = t;
            t = y;

            y = x.subtract(q.multiply(y));
            x = t;
        }

        if (x.compareTo(BigInteger.ZERO) < 0) {
            x = x.add(fi0);
        }

        return x;
    }

    private BigInteger[] encrypt(String text, BigInteger n, BigInteger e) {
        BigInteger x = new BigInteger(text);
        return new BigInteger[]{x.modPow(e, n)};
    }

    private String decrypt(BigInteger[] encrypted, BigInteger n, BigInteger d) {
        BigInteger decrypted = encrypted[0].modPow(d, n);
        return decrypted.toString();
    }

    private String stringify(BigInteger[] array) {
        StringBuilder builder = new StringBuilder();
        for (BigInteger bi : array) {
            builder.append(bi).append(" ");
        }
        return builder.toString();
    }

    private void writeToFile(String fileName, BigInteger[] encryptedText, BigInteger n) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Encrypted Text:\n");
            for (BigInteger bi : encryptedText) {
                writer.write(bi.toString() + "\n");
            }
            writer.write("\nPublic Key 'n':\n" + n.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEncryptedTextFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            Tuple<BigInteger[], BigInteger> fileData = readFromFile(selectedFile.getAbsolutePath());
            if (fileData != null) {
                encrypted = fileData.getItem1();
                n = fileData.getItem2();
                outputArea.setText("Encrypted Text loaded from file:\n" + stringify(encrypted) +
                        "\nPublic Key 'n' loaded from file:\n" + n.toString());
                saveToFileButton.setEnabled(true);
            } else {
                outputArea.setText("Failed to read encrypted text from file.");
            }
        }
    }

    private void saveEncryptedTextToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            writeToFile(selectedFile.getAbsolutePath(), encrypted, n);
        }
    }

    private Tuple<BigInteger[], BigInteger> readFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            BigInteger[] encryptedText = null;
            BigInteger n = null;
            boolean isEncryptedText = false;
            boolean isN = false;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.equals("Encrypted Text:")) {
                    isEncryptedText = true;
                    continue;
                } else if (line.equals("Public Key 'n':")) {
                    isEncryptedText = false;
                    isN = true;
                    continue;
                }

                if (isEncryptedText) {
                    if (encryptedText == null) {
                        encryptedText = new BigInteger[countLines(fileName) - 4];
                    }
                    encryptedText[index++] = new BigInteger(line);
                } else if (isN) {
                    n = new BigInteger(line);
                }
            }
            return new Tuple<>(encryptedText, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int countLines(String fileName) throws IOException {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) lines++;
        }
        return lines;
    }

    private static class Tuple<T, U> {
        private final T item1;
        private final U item2;

        public Tuple(T item1, U item2) {
            this.item1 = item1;
            this.item2 = item2;
        }

        public T getItem1() {
            return item1;
        }

        public U getItem2() {
            return item2;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RSAEncryptionDecryptionForm::new);
    }
}
