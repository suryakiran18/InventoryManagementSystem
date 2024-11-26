import java.util.*;

public class Main {
    private final Map<String, Item> inventory = new HashMap<>();
    private final Map<String, PriorityQueue<Item>> categoryMap = new TreeMap<>();
    private final PriorityQueue<Item> globalMaxHeap = new PriorityQueue<>();
    private final int restockingThreshold;

    public Main(int restockingThreshold) {
        this.restockingThreshold = restockingThreshold;
        initializeSampleData();
    }

    // Item Class
    static class Item implements Comparable<Item> {
        private final String id;
        private String name;
        private final String category;
        private int quantity;

        public Item(String id, String name, String category, int quantity) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        @Override
        public int compareTo(Item other) {
            return Integer.compare(other.quantity, this.quantity);
        }

        @Override
        public String toString() {
            return String.format("Item{id='%s', name='%s', category='%s', quantity=%d}",
                    id, name, category, quantity);
        }
    }

    // Add Item (Updated with Merge Functionality)
    public void addItem(Item item) {
        if (inventory.containsKey(item.getId())) {
            // Merge logic: Update name and quantity if the new quantity is higher
            Item existingItem = inventory.get(item.getId());
            if (item.getQuantity() > existingItem.getQuantity()) {
                existingItem.setQuantity(item.getQuantity());
                existingItem.setName(item.getName());
                refreshCategory(existingItem);
                refreshGlobalHeap(existingItem);
                System.out.printf("Merged Item: %s. Updated to new name '%s' and higher quantity.%n",
                        item.getId(), item.getName());
            } else {
                System.out.printf("Item %s already exists with equal or higher quantity. No changes made.%n",
                        item.getId());
            }
        } else {
            inventory.put(item.getId(), item);
            categoryMap.computeIfAbsent(item.getCategory(), k -> new PriorityQueue<>()).offer(item);
            globalMaxHeap.offer(item);
            checkRestocking(item);
            System.out.printf("Added new item: %s%n", item.getId());
        }
    }

    // Update Item Quantity
    public void updateItem(String id, int newQuantity) {
        Item item = inventory.get(id);
        if (item == null) {
            System.out.println("Item not found!");
            return;
        }
        item.setQuantity(newQuantity);
        refreshCategory(item);
        refreshGlobalHeap(item);
        checkRestocking(item);
    }

    // Delete Item
    public void deleteItem(String id) {
        Item item = inventory.remove(id);
        if (item != null) {
            categoryMap.get(item.getCategory()).remove(item);
            globalMaxHeap.remove(item);
        }
    }

    // Get Items by Category
    public List<Item> getItemsByCategory(String category) {
        PriorityQueue<Item> pq = categoryMap.get(category);
        if (pq == null) return Collections.emptyList();
        return new ArrayList<>(pq);
    }

    // Get Top K Items
    public List<Item> getTopKItems(int k) {
        PriorityQueue<Item> tempHeap = new PriorityQueue<>(globalMaxHeap);
        List<Item> topK = new ArrayList<>();
        for (int i = 0; i < k && !tempHeap.isEmpty(); i++) {
            topK.add(tempHeap.poll());
        }
        return topK;
    }

    // Check Restocking
    private void checkRestocking(Item item) {
        if (item.getQuantity() < restockingThreshold) {
            System.out.printf("Restocking Alert: Item %s (%s) is below threshold!%n",
                    item.getName(), item.getId());
        }
    }

    private void refreshCategory(Item item) {
        PriorityQueue<Item> pq = categoryMap.get(item.getCategory());
        pq.remove(item);
        pq.offer(item);
    }

    private void refreshGlobalHeap(Item item) {
        globalMaxHeap.remove(item);
        globalMaxHeap.offer(item);
    }

    // Initialize Sample Data
    private void initializeSampleData() {
        addItem(new Item("101", "Laptop", "Electronics", 50));
        addItem(new Item("102", "Phone", "Electronics", 5)); // Restocking alert
        addItem(new Item("103", "Chair", "Furniture", 30));
        addItem(new Item("104", "Table", "Furniture", 15));
        addItem(new Item("105", "Apple", "Groceries", 100));
        addItem(new Item("106", "Milk", "Groceries", 8)); // Restocking alert
    }

    // Main Method with User Interaction
    public static void main(String[] args) {
        Main ims = new Main(10);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Inventory Management System ===");
            System.out.println("1. Add Item");
            System.out.println("2. Update Item Quantity");
            System.out.println("3. Delete Item");
            System.out.println("4. View Items by Category");
            System.out.println("5. View Top K Items by Quantity");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            switch (choice) {
                case 1:
                    System.out.print("Enter Item ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Enter Item Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Item Category: ");
                    String category = scanner.nextLine();
                    System.out.print("Enter Item Quantity: ");
                    int quantity = scanner.nextInt();
                    ims.addItem(new Item(id, name, category, quantity));
                    break;

                case 2:
                    System.out.print("Enter Item ID to Update: ");
                    id = scanner.nextLine();
                    System.out.print("Enter New Quantity: ");
                    quantity = scanner.nextInt();
                    ims.updateItem(id, quantity);
                    break;

                case 3:
                    System.out.print("Enter Item ID to Delete: ");
                    id = scanner.nextLine();
                    ims.deleteItem(id);
                    break;

                case 4:
                    System.out.print("Enter Category to View: ");
                    category = scanner.nextLine();
                    List<Item> items = ims.getItemsByCategory(category);
                    if (items.isEmpty()) {
                        System.out.println("No items found in this category.");
                    } else {
                        items.forEach(System.out::println);
                    }
                    break;

                case 5:
                    System.out.print("Enter the number of top items to view: ");
                    int k = scanner.nextInt();
                    List<Item> topItems = ims.getTopKItems(k);
                    if (topItems.isEmpty()) {
                        System.out.println("No items available.");
                    } else {
                        topItems.forEach(System.out::println);
                    }
                    break;

                case 6:
                    System.out.println("Exiting system. Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
}
