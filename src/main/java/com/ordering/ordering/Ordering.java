
package com.ordering.ordering;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Ordering extends JFrame {

    private final Color BLACK = new Color(25, 20, 35);
    private final Color PURPLE = new Color(124, 58, 237);
    private final Color DARK_PURPLE = new Color(91, 33, 182);
    private final Color LIGHT_PURPLE = new Color(245, 240, 255);
    private final Color BACKGROUND = new Color(246, 243, 250);
    private final Color TEXT = new Color(40, 33, 52);
    private final Color MUTED = new Color(112, 103, 125);
    private final Color BORDER = new Color(226, 219, 237);
    private final Color GREEN = new Color(22, 140, 80);
    private final Color RED = new Color(200, 48, 60);

    private final String MENU_FILE = "menu.csv";
    private final String RECEIPT_FOLDER = "receipts";
    private final String REPORT_FILE = "sales_report.txt";

    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<CartItem> cartItems = new ArrayList<>();

    private JPanel menuGrid;
    private JPanel categoryBar;
    private JPanel cartRows;
    private JScrollPane menuScroll;

    private JTextField searchField;
    private JTextField customerField;
    private JSpinner quantitySpinner;

    private JComboBox<String> categoryBox;
    private JComboBox<String> orderTypeBox;
    private JComboBox<String> discountBox;

    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private JLabel cartCountLabel;
    private JLabel statusLabel;

    private String activeCategory = "All";
    private double subtotal;
    private double discount;
    private double total;
    private double totalSales;
    private int completedOrders;
    private int orderNumber = 1;

    public Ordering() {
        setTitle("Corner Cafe - Ordering System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1320, 820);
        setLocationRelativeTo(null);

        loadMenu();
        buildGUI();
        refreshMenuCards();
        refreshCart();
    }

    // ==================== MAIN GUI ====================

    private void buildGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        root.add(createSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setBackground(BACKGROUND);
        main.setBorder(new EmptyBorder(20, 20, 14, 20));

        main.add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createMenuPanel(),
                createCartPanel()
        );

        split.setResizeWeight(0.63);
        split.setDividerSize(9);
        split.setBorder(null);
        split.setContinuousLayout(true);
        split.setBackground(BACKGROUND);

        main.add(split, BorderLayout.CENTER);
        main.add(createStatusBar(), BorderLayout.SOUTH);

        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(205, 0));
        side.setBackground(BLACK);
        side.setBorder(new EmptyBorder(25, 15, 20, 15));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("CORNER CAFE");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("FRESH FOOD. GOOD MOOD.");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        tagline.setForeground(new Color(196, 181, 253));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(logo);
        side.add(Box.createVerticalStrut(5));
        side.add(tagline);
        side.add(Box.createVerticalStrut(38));

        JLabel heading = new JLabel("WORKSPACE");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heading.setForeground(new Color(170, 155, 190));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(heading);
        side.add(Box.createVerticalStrut(12));

        addSidebarButton(side, "Dashboard", this::showDashboard);
        addSidebarButton(side, "New Order", this::newOrder);
        addSidebarButton(side, "Save Menu", this::saveMenu);
        addSidebarButton(side, "View Receipt", this::showReceipt);
        addSidebarButton(side, "Sales Report", this::showSales);
        addSidebarButton(side, "About", this::showAbout);

        side.add(Box.createVerticalGlue());

        JLabel footer = new JLabel(
                "<html><div style='text-align:center'>"
                + "CORNER CAFE<br>Ordering System"
                + "</div></html>"
        );
        footer.setForeground(new Color(190, 180, 205));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        side.add(footer);
        return side;
    }

    private void addSidebarButton(
            JPanel side, String text, Runnable action) {

        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 43));
        button.setPreferredSize(new Dimension(175, 43));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(0, 12, 0, 8));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(BLACK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(DARK_PURPLE);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BLACK);
            }
        });

        button.addActionListener(e -> action.run());

        side.add(button);
        side.add(Box.createVerticalStrut(7));
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Cafe Ordering");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(BLACK);

        JLabel sub = new JLabel(
                "Choose your favorites and build your order."
        );
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(sub);

        JLabel badge = new JLabel("  ● READY TO ORDER  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(DARK_PURPLE);
        badge.setBackground(new Color(237, 233, 254));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(9, 8, 9, 8));

        panel.add(titles, BorderLayout.WEST);
        panel.add(badge, BorderLayout.EAST);
        return panel;
    }

    // ==================== MENU PANEL ====================

    private JPanel createMenuPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(10, 12));

        JPanel top = new JPanel(new BorderLayout(8, 10));
        top.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Explore the Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(BLACK);

        JLabel subtitle = new JLabel("Select a meal, snack, drink, or dessert.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(100, 39));
        searchField.setBorder(inputBorder());
        searchField.setToolTipText("Search meals and drinks");

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        refreshMenuCards();
                    }

                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        refreshMenuCards();
                    }

                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        refreshMenuCards();
                    }
                }
        );

        categoryBox = new JComboBox<>(new String[]{
            "All Categories", "Meals", "Snacks", "Drinks", "Desserts"
        });
        styleCombo(categoryBox);
        categoryBox.addActionListener(e -> {
            activeCategory = categoryBox.getSelectedIndex() == 0
                    ? "All"
                    : categoryBox.getSelectedItem().toString();
            refreshMenuCards();
        });

        JPanel filters = new JPanel(new GridLayout(1, 2, 8, 0));
        filters.setOpaque(false);
        filters.add(searchField);
        filters.add(categoryBox);

        top.add(heading, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);

        menuGrid = new JPanel(new GridBagLayout());
        menuGrid.setBackground(BACKGROUND);
        menuGrid.setBorder(new EmptyBorder(8, 8, 8, 8));

        menuScroll = new JScrollPane(menuGrid);
        menuScroll.setBorder(new LineBorder(BORDER, 1, true));
        menuScroll.getViewport().setBackground(BACKGROUND);
        menuScroll.getVerticalScrollBar().setUnitIncrement(18);
        menuScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel qtyLabel = new JLabel("Quantity to add:");
        qtyLabel.setForeground(TEXT);
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        quantitySpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quantitySpinner.setPreferredSize(new Dimension(75, 38));

        JButton addButton = button("Add Selected Item", PURPLE);
        addButton.addActionListener(e -> addSelectedItem());

        gbc.gridx = 0;
        gbc.weightx = 0;
        bottom.add(qtyLabel, gbc);

        gbc.gridx = 1;
        bottom.add(quantitySpinner, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        bottom.add(addButton, gbc);

        panel.add(top, BorderLayout.NORTH);
        panel.add(menuScroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== MEAL CARDS ====================

    private void refreshMenuCards() {
        if (menuGrid == null) {
            return;
        }

        menuGrid.removeAll();

        String query = searchField == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        int shown = 0;
        int columns = 2;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;

        for (MenuItem item : menuItems) {
            boolean categoryMatches = activeCategory.equals("All")
                    || item.category.equalsIgnoreCase(activeCategory);

            boolean searchMatches = item.name.toLowerCase(Locale.ROOT)
                    .contains(query)
                    || item.category.toLowerCase(Locale.ROOT)
                    .contains(query);

            if (!categoryMatches || !searchMatches) {
                continue;
            }

            int column = shown % columns;
            int row = shown / columns;

            gbc.gridx = column;
            gbc.gridy = row;
            gbc.weightx = 1;

            menuGrid.add(createMealCard(item), gbc);
            shown++;
        }

        if (shown == 0) {
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.weighty = 1;
            menuGrid.add(emptyMessage("No matching menu items found."), gbc);
            gbc.gridwidth = 1;
        } else {
            gbc.gridx = 0;
            gbc.gridy = (shown + columns - 1) / columns;
            gbc.gridwidth = columns;
            gbc.weighty = 1;
            menuGrid.add(Box.createGlue(), gbc);
            gbc.gridwidth = 1;
        }

        menuGrid.revalidate();
        menuGrid.repaint();
    }

    private JPanel createMealCard(MenuItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.setPreferredSize(new Dimension(205, 205));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.setOpaque(false);

        JLabel emoji = new JLabel(item.emoji);
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 29));
        emoji.setHorizontalAlignment(SwingConstants.CENTER);
        emoji.setOpaque(true);
        emoji.setBackground(LIGHT_PURPLE);
        emoji.setPreferredSize(new Dimension(57, 54));

        JLabel category = new JLabel(item.category.toUpperCase());
        category.setFont(new Font("Segoe UI", Font.BOLD, 9));
        category.setForeground(DARK_PURPLE);

        JLabel name = new JLabel(item.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(BLACK);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(category);
        info.add(Box.createVerticalStrut(6));
        info.add(name);

        top.add(emoji, BorderLayout.WEST);
        top.add(info, BorderLayout.CENTER);

        JLabel price = new JLabel(money(item.price));
        price.setFont(new Font("Segoe UI", Font.BOLD, 22));
        price.setForeground(DARK_PURPLE);

        JLabel stock = new JLabel(
                item.stock > 0
                ? "Available: " + item.stock
                : "OUT OF STOCK"
        );
        stock.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stock.setForeground(item.stock > 0 ? GREEN : RED);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(price);
        details.add(Box.createVerticalStrut(4));
        details.add(stock);

        JButton add = button(
                item.stock > 0 ? "+  Add to Cart" : "Out of Stock",
                item.stock > 0 ? PURPLE : MUTED
        );
        add.setEnabled(item.stock > 0);
        add.addActionListener(e -> {
            quantitySpinner.setValue(1);
            addItem(item, (Integer) quantitySpinner.getValue());
        });

        card.add(top, BorderLayout.NORTH);
        card.add(details, BorderLayout.CENTER);
        card.add(add, BorderLayout.SOUTH);

        return card;
    }

    private JPanel emptyMessage(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);

        JLabel label = new JLabel(message);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(label);
        return panel;
    }

    // ==================== CART PANEL ====================

    private JPanel createCartPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(10, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Your Order");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(BLACK);

        JLabel subtitle = new JLabel("Review before checkout");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        cartCountLabel = new JLabel("0 items");
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountLabel.setForeground(DARK_PURPLE);

        header.add(titles, BorderLayout.WEST);
        header.add(cartCountLabel, BorderLayout.EAST);

        cartRows = new JPanel();
        cartRows.setBackground(Color.WHITE);
        cartRows.setLayout(new BoxLayout(cartRows, BoxLayout.Y_AXIS));
        cartRows.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane cartScroll = new JScrollPane(cartRows);
        cartScroll.setBorder(new LineBorder(BORDER, 1, true));
        cartScroll.getViewport().setBackground(Color.WHITE);
        cartScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        customerField = new JTextField();
        customerField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerField.setBorder(inputBorder());
        customerField.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 38));

        bottom.add(fieldLabel("CUSTOMER NAME"));
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(customerField);
        bottom.add(Box.createVerticalStrut(10));

        orderTypeBox = new JComboBox<>(
                new String[]{"Dine In", "Takeout"});
        discountBox = new JComboBox<>(new String[]{
            "No Discount", "Senior/PWD - 20%", "Promo - 10%"
        });

        styleCombo(orderTypeBox);
        styleCombo(discountBox);

        JPanel options = new JPanel(new GridLayout(1, 2, 8, 0));
        options.setOpaque(false);

        JPanel orderTypePanel = new JPanel(new BorderLayout(0, 5));
        orderTypePanel.setOpaque(false);
        orderTypePanel.add(fieldLabel("ORDER TYPE"), BorderLayout.NORTH);
        orderTypePanel.add(orderTypeBox, BorderLayout.CENTER);

        JPanel discountPanel = new JPanel(new BorderLayout(0, 5));
        discountPanel.setOpaque(false);
        discountPanel.add(fieldLabel("DISCOUNT"), BorderLayout.NORTH);
        discountPanel.add(discountBox, BorderLayout.CENTER);

        options.add(orderTypePanel);
        options.add(discountPanel);

        discountBox.addActionListener(e -> updateTotals());

        bottom.add(options);
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(new JSeparator());
        bottom.add(Box.createVerticalStrut(10));

        subtotalLabel = new JLabel("₱0.00");
        discountLabel = new JLabel("₱0.00");
        totalLabel = new JLabel("₱0.00");

        bottom.add(summaryRow("Subtotal", subtotalLabel, false));
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(summaryRow("Discount", discountLabel, false));
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(summaryRow("TOTAL", totalLabel, true));
        bottom.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);

        JButton remove = button("Remove Selected", BLACK);
        JButton clear = button("Clear Cart", RED);

        remove.addActionListener(e -> removeSelected());
        clear.addActionListener(e -> clearCart());

        actions.add(remove);
        actions.add(clear);

        bottom.add(actions);
        bottom.add(Box.createVerticalStrut(8));

        JButton checkout = button("Complete Checkout", PURPLE);
        checkout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        checkout.setPreferredSize(new Dimension(100, 46));
        checkout.addActionListener(e -> checkout());

        bottom.add(checkout);

        panel.add(header, BorderLayout.NORTH);
        panel.add(cartScroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel summaryRow(
            String name, JLabel value, boolean isTotal) {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel label = new JLabel(name);
        label.setFont(new Font(
                "Segoe UI",
                isTotal ? Font.BOLD : Font.PLAIN,
                isTotal ? 16 : 13
        ));
        label.setForeground(isTotal ? BLACK : MUTED);

        value.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                isTotal ? 23 : 13
        ));
        value.setForeground(isTotal ? DARK_PURPLE : TEXT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    private JPanel createCartRow(CartItem item) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setBackground(Color.WHITE);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(10, 5, 10, 5)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(item.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TEXT);

        JLabel quantity = new JLabel(
                item.quantity + " × " + money(item.price)
        );
        quantity.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        quantity.setForeground(MUTED);

        info.add(name);
        info.add(Box.createVerticalStrut(5));
        info.add(quantity);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lineTotal = new JLabel(money(item.price * item.quantity));
        lineTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lineTotal.setForeground(DARK_PURPLE);
        lineTotal.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel qtyButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        qtyButtons.setOpaque(false);

        JButton minus = smallButton("−");
        JButton plus = smallButton("+");

        minus.setEnabled(item.quantity > 1);
        plus.setEnabled(item.quantity < item.menuItem.stock);

        minus.addActionListener(e -> changeQuantity(item, -1));
        plus.addActionListener(e -> changeQuantity(item, 1));

        qtyButtons.add(minus);
        qtyButtons.add(plus);

        right.add(lineTotal);
        right.add(Box.createVerticalStrut(5));
        right.add(qtyButtons);

        row.add(info, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(28, 25));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(DARK_PURPLE);
        button.setBackground(LIGHT_PURPLE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void refreshCart() {
        if (cartRows == null) {
            return;
        }

        cartRows.removeAll();

        int count = 0;

        for (CartItem item : cartItems) {
            cartRows.add(createCartRow(item));
            cartRows.add(Box.createVerticalStrut(3));
            count += item.quantity;
        }

        if (cartItems.isEmpty()) {
            JPanel empty = emptyMessage(
                    "Your cart is empty.\nChoose a meal to get started.");
            empty.setPreferredSize(new Dimension(180, 150));
            cartRows.add(empty);
        }

        cartCountLabel.setText(
                count + (count == 1 ? " item" : " items"));

        cartRows.revalidate();
        cartRows.repaint();
        updateTotals();
    }

    // ==================== ORDERING FUNCTIONS ====================

    private void addSelectedItem() {
        JOptionPane.showMessageDialog(
                this,
                "Choose a meal card and click its Add to Cart button.",
                "Choose a Menu Item",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void addItem(MenuItem item, int quantity) {
        if (quantity <= 0) {
            showError("Quantity must be greater than zero.");
            return;
        }

        if (item.stock < quantity) {
            showError("Only " + item.stock + " units of "
                    + item.name + " are available.");
            return;
        }

        CartItem existing = null;

        for (CartItem cartItem : cartItems) {
            if (cartItem.menuItem == item) {
                existing = cartItem;
                break;
            }
        }

        int currentQuantity = existing == null ? 0 : existing.quantity;

        if (currentQuantity + quantity > item.stock) {
            showError("Not enough stock for " + item.name + ".");
            return;
        }

        if (existing == null) {
            cartItems.add(new CartItem(item, quantity));
        } else {
            existing.quantity += quantity;
        }

        refreshCart();
        setStatus(item.name + " added to your order.");
    }

    private void changeQuantity(CartItem item, int amount) {
        int newQuantity = item.quantity + amount;

        if (newQuantity <= 0) {
            cartItems.remove(item);
        } else if (newQuantity <= item.menuItem.stock) {
            item.quantity = newQuantity;
        } else {
            showError("No more stock available for " + item.name + ".");
            return;
        }

        refreshCart();
    }

    private void removeSelected() {
        if (cartItems.isEmpty()) {
            showError("Your cart is already empty.");
            return;
        }

        String[] names = new String[cartItems.size()];

        for (int i = 0; i < cartItems.size(); i++) {
            names[i] = cartItems.get(i).name
                    + " — " + cartItems.get(i).quantity + " pcs";
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select an item to remove:",
                "Remove Item",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names,
                names[0]
        );

        if (selected == null) {
            return;
        }

        for (int i = 0; i < cartItems.size(); i++) {
            if (names[i].equals(selected)) {
                cartItems.remove(i);
                break;
            }
        }

        refreshCart();
    }

    private void clearCart() {
        if (cartItems.isEmpty()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Remove all items from the cart?",
                "Clear Cart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            cartItems.clear();
            refreshCart();
            setStatus("Cart cleared.");
        }
    }

    private void updateTotals() {
        subtotal = 0;

        for (CartItem item : cartItems) {
            subtotal += item.price * item.quantity;
        }

        discount = 0;

        if (discountBox != null) {
            String selected = (String) discountBox.getSelectedItem();

            if ("Senior/PWD - 20%".equals(selected)) {
                discount = subtotal * 0.20;
            } else if ("Promo - 10%".equals(selected)
                    && subtotal >= 500) {
                discount = subtotal * 0.10;
            }
        }

        total = Math.max(0, subtotal - discount);

        if (subtotalLabel != null) {
            subtotalLabel.setText(money(subtotal));
            discountLabel.setText(money(discount));
            totalLabel.setText(money(total));
        }
    }

    // ==================== CHECKOUT ====================

    private void checkout() {
        if (cartItems.isEmpty()) {
            showError("Add at least one item before checkout.");
            return;
        }

        String customer = customerField.getText().trim();

        if (customer.isEmpty()) {
            showError("Enter the customer's name first.");
            customerField.requestFocus();
            return;
        }

        updateTotals();

        for (CartItem item : cartItems) {
            if (item.quantity > item.menuItem.stock) {
                showError("Insufficient stock for " + item.name + ".");
                return;
            }
        }

        String paymentText = JOptionPane.showInputDialog(
                this,
                "Customer: " + customer
                + "\nTotal: " + money(total)
                + "\n\nEnter cash received:",
                "Checkout",
                JOptionPane.QUESTION_MESSAGE
        );

        if (paymentText == null) {
            return;
        }

        double cash;

        try {
            cash = Double.parseDouble(paymentText.trim());

            if (!Double.isFinite(cash) || cash < 0) {
                showError("Enter a valid payment amount.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Payment must be a number.");
            return;
        }

        if (cash < total) {
            showError("Insufficient payment. Required: " + money(total));
            return;
        }

        double change = cash - total;
        String receipt = buildReceipt(customer, cash, change);

        for (CartItem item : cartItems) {
            item.menuItem.stock -= item.quantity;
        }

        totalSales += total;
        completedOrders++;

        saveMenuFile();
        saveReceipt(receipt, orderNumber);

        JTextArea receiptArea = new JTextArea(receipt);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setPreferredSize(new Dimension(390, 430));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Order Completed",
                JOptionPane.INFORMATION_MESSAGE
        );

        orderNumber++;
        cartItems.clear();
        customerField.setText("");
        discountBox.setSelectedIndex(0);

        refreshCart();
        refreshMenuCards();

        setStatus("Payment complete. Change: " + money(change));
    }

    private String buildReceipt(
            String customer, double cash, double change) {

        StringBuilder out = new StringBuilder();

        out.append("====================================\n");
        out.append("             CORNER CAFE\n");
        out.append("           OFFICIAL RECEIPT\n");
        out.append("====================================\n");
        out.append("Order No.: ").append(orderNumber).append("\n");
        out.append("Date: ").append(
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )).append("\n");
        out.append("Customer: ").append(customer).append("\n");
        out.append("Order Type: ").append(orderTypeBox.getSelectedItem())
                .append("\n");
        out.append("------------------------------------\n");

        for (CartItem item : cartItems) {
            out.append(item.name).append("\n");
            out.append("  ").append(item.quantity)
                    .append(" x ").append(money(item.price))
                    .append(" = ")
                    .append(money(item.price * item.quantity))
                    .append("\n");
        }

        out.append("------------------------------------\n");
        out.append("Subtotal: ").append(money(subtotal)).append("\n");
        out.append("Discount: ").append(money(discount)).append("\n");
        out.append("TOTAL:    ").append(money(total)).append("\n");
        out.append("Cash:     ").append(money(cash)).append("\n");
        out.append("Change:   ").append(money(change)).append("\n");
        out.append("====================================\n");
        out.append("       THANK YOU FOR VISITING!\n");
        out.append("====================================\n");

        return out.toString();
    }

    // ==================== FILE STORAGE ====================

    private void loadMenu() {
        File file = new File(MENU_FILE);

        if (!file.exists()) {
            createDefaultMenu();
            saveMenuFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().contains("item")) {
                        continue;
                    }
                }

                String[] parts = line.split(",", -1);

                if (parts.length < 4) {
                    continue;
                }

                try {
                    String name = parts[0].trim();
                    String category = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    int stock = Integer.parseInt(parts[3].trim());

                    if (!name.isEmpty() && price >= 0 && stock >= 0) {
                        menuItems.add(new MenuItem(
                                name, category, price, stock,
                                emojiFor(category, name)));
                    }
                } catch (NumberFormatException ignored) {
                    // Skip malformed menu rows.
                }
            }

        } catch (IOException e) {
            showError("Could not load menu: " + e.getMessage());
        }

        if (menuItems.isEmpty()) {
            createDefaultMenu();
            saveMenuFile();
        }
    }

    private void createDefaultMenu() {
        menuItems.clear();

        menuItems.add(new MenuItem("Chicken Meal", "Meals", 99, 50, "🍗"));
        menuItems.add(new MenuItem("Burger", "Meals", 75, 40, "🍔"));
        menuItems.add(new MenuItem("Rice Meal", "Meals", 85, 35, "🍛"));
        menuItems.add(new MenuItem("Spaghetti", "Meals", 80, 25, "🍝"));
        menuItems.add(new MenuItem("Fries", "Snacks", 45, 30, "🍟"));
        menuItems.add(new MenuItem("Sandwich", "Snacks", 55, 25, "🥪"));
        menuItems.add(new MenuItem("Iced Tea", "Drinks", 35, 50, "🧋"));
        menuItems.add(new MenuItem("Milk Tea", "Drinks", 65, 30, "🥤"));
        menuItems.add(new MenuItem("Coffee", "Drinks", 45, 40, "☕"));
        menuItems.add(new MenuItem("Ice Cream", "Desserts", 40, 25, "🍨"));
        menuItems.add(new MenuItem("Cake", "Desserts", 55, 20, "🍰"));
        menuItems.add(new MenuItem("Donut", "Desserts", 30, 20, "🍩"));
    }

    private String emojiFor(String category, String name) {
        String value = (category + " " + name).toLowerCase(Locale.ROOT);

        if (value.contains("chicken")) return "🍗";
        if (value.contains("burger")) return "🍔";
        if (value.contains("rice") || value.contains("meal")) return "🍛";
        if (value.contains("spaghetti") || value.contains("pasta")) return "🍝";
        if (value.contains("fries")) return "🍟";
        if (value.contains("sandwich")) return "🥪";
        if (value.contains("tea")) return "🧋";
        if (value.contains("coffee")) return "☕";
        if (value.contains("ice cream")) return "🍨";
        if (value.contains("cake")) return "🍰";
        if (value.contains("donut")) return "🍩";
        if (category.equalsIgnoreCase("Drinks")) return "🥤";
        if (category.equalsIgnoreCase("Desserts")) return "🍰";
        if (category.equalsIgnoreCase("Snacks")) return "🍟";

        return "🍽";
    }

    private void saveMenuFile() {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(MENU_FILE))) {

            writer.println("Item,Category,Price,Stock");

            for (MenuItem item : menuItems) {
                writer.println(item.name + ","
                        + item.category + ","
                        + item.price + ","
                        + item.stock);
            }

        } catch (IOException e) {
            showError("Could not save menu: " + e.getMessage());
        }
    }

    private void saveMenu() {
        saveMenuFile();

        JOptionPane.showMessageDialog(
                this,
                "Menu saved to " + MENU_FILE,
                "Menu Saved",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void saveReceipt(String receipt, int number) {
        File folder = new File(RECEIPT_FOLDER);

        if (!folder.exists() && !folder.mkdirs()) {
            showError("Could not create the receipts folder.");
            return;
        }

        File file = new File(folder, "receipt_" + number + ".txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(receipt);
        } catch (IOException e) {
            showError("Could not save receipt: " + e.getMessage());
        }
    }

    // ==================== REPORTS ====================

    private void showDashboard() {
        int stockTotal = 0;
        int lowStock = 0;

        for (MenuItem item : menuItems) {
            stockTotal += item.stock;
            if (item.stock <= 5) {
                lowStock++;
            }
        }

        String message =
                "CORNER CAFE DASHBOARD\n\n"
                + "Menu products: " + menuItems.size() + "\n"
                + "Units in stock: " + stockTotal + "\n"
                + "Low-stock products: " + lowStock + "\n"
                + "Completed orders: " + completedOrders + "\n"
                + "Total sales this session: " + money(totalSales);

        JOptionPane.showMessageDialog(
                this, message, "Dashboard",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSales() {
        String report =
                "CORNER CAFE - SALES REPORT\n"
                + "Generated: "
                + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + "\n\nCompleted orders: " + completedOrders
                + "\nTotal sales this session: " + money(totalSales)
                + "\nCurrent cart total: " + money(total);

        try (PrintWriter writer = new PrintWriter(
                new FileWriter(REPORT_FILE))) {
            writer.println(report);
        } catch (IOException e) {
            showError("Could not save the sales report.");
        }

        JTextArea area = new JTextArea(report);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(
                this, new JScrollPane(area),
                "Sales Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showReceipt() {
        File folder = new File(RECEIPT_FOLDER);
        File[] files = folder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (files == null || files.length == 0) {
            showError("No saved receipts yet.");
            return;
        }

        java.util.Arrays.sort(files,
                (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }

        String selected = (String) JOptionPane.showInputDialog(
                this, "Choose a receipt:", "Saved Receipts",
                JOptionPane.PLAIN_MESSAGE, null, names, names[0]);

        if (selected == null) {
            return;
        }

        File file = new File(folder, selected);

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file))) {

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            JTextArea area = new JTextArea(content.toString());
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setEditable(false);
            area.setCaretPosition(0);

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(400, 430));

            JOptionPane.showMessageDialog(
                    this, scroll, selected,
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            showError("Could not read the receipt.");
        }
    }

    private void newOrder() {
        if (!cartItems.isEmpty()) {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Clear the current order and start again?",
                    "New Order",
                    JOptionPane.YES_NO_OPTION);

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        cartItems.clear();
        customerField.setText("");
        searchField.setText("");
        categoryBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);
        discountBox.setSelectedIndex(0);

        refreshMenuCards();
        refreshCart();
        setStatus("Started a new order.");
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                "CORNER CAFE ORDERING SYSTEM\n\n"
                + "Java Swing desktop application.\n\n"
                + "Features:\n"
                + "• Menu cards and search\n"
                + "• Category filtering\n"
                + "• Shopping cart and quantity controls\n"
                + "• Discounts and cash payments\n"
                + "• Inventory tracking\n"
                + "• Receipt and report files\n"
                + "• CSV menu storage\n\n"
                + "Purple • Black • White",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== UI HELPERS ====================

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        return panel;
    }

    private Border inputBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 9, 7, 9));
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(MUTED);
        return label;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
        combo.setPreferredSize(new Dimension(100, 37));
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hover = color.equals(PURPLE)
                ? DARK_PURPLE
                : color.darker();

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        statusLabel = new JLabel("Ready to accept orders.");
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel version = new JLabel("CORNER CAFE • JAVA SWING");
        version.setForeground(MUTED);
        version.setFont(new Font("Segoe UI", Font.BOLD, 10));

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(version, BorderLayout.EAST);
        return panel;
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Notice",
                JOptionPane.WARNING_MESSAGE);
    }

    private String money(double value) {
        return String.format(Locale.US, "₱%,.2f", value);
    }

    // ==================== DATA CLASSES ====================

    private static class MenuItem {
        String name;
        String category;
        double price;
        int stock;
        String emoji;

        MenuItem(
                String name,
                String category,
                double price,
                int stock,
                String emoji) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.emoji = emoji;
        }
    }

    private static class CartItem {
        MenuItem menuItem;
        String name;
        double price;
        int quantity;

        CartItem(MenuItem item, int quantity) {
            this.menuItem = item;
            this.name = item.name;
            this.price = item.price;
            this.quantity = quantity;
        }
    }

    // ==================== APPLICATION ENTRY ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep Swing's default look and feel.
            }

            new Ordering().setVisible(true);
        });
    }
}
