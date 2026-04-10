package com.economysim;

import java.util.*;

public class Game {
    private static final int MAX_DAYS = 60;
    private static final double WIN_GOLD = 2000.0;
    private static final double STARTING_GOLD = 600.0;
    private static final double TRAVEL_COST = 10.0;

    private Player player;
    private List<Item> items;
    private List<City> cities;
    private List<Market> markets;
    private EventManager eventManager;
    private Scanner scanner;
    private int currentDay;
    private SaveManager saveManager;
    private boolean gameReset = false;
    private Map<String, Map<String, Integer>> distances;

    public Game() {
        this.items = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.markets = new ArrayList<>();
        this.eventManager = new EventManager();
        this.scanner = new Scanner(System.in);
        this.currentDay = 1;
        this.saveManager = new SaveManager();
        initializeGame();
    }

    private void initializeGame() {
        // items
        Item rum = new Item("Rum", 40.0);
        Item tobacco = new Item("Tobacco", 25.0);
        Item tea = new Item("Tea", 60.0);
        Item cotton = new Item("Cotton", 15.0);
        Item spice = new Item("Spice", 80.0);
        Item silk = new Item("Silk", 70.0);
        Item cannon = new Item("Cannon", 120.0);
        items.addAll(List.of(rum, tobacco, tea, cotton, spice, silk, cannon));

        // cities
        City london = new City("London", List.of(cotton, tobacco));
        City havana = new City("Havana", List.of(rum, cannon));
        City alexandria = new City("Alexandria", List.of(spice, silk));
        City bombay = new City("Bombay", List.of(tea, spice));
        City lisbon = new City("Lisbon", List.of(rum, cotton));
        cities.addAll(List.of(london, havana, alexandria, bombay, lisbon));

        // markets
        for (City city : cities) {
            markets.add(new Market(city, items));
        }

        player = new Player(STARTING_GOLD, london);
        initializeDistances();
    }

    private void initializeDistances() {
        distances = new HashMap<>();

        // London connections
        distances.put("London", new HashMap<>());
        distances.get("London").put("Lisbon", 1);
        distances.get("London").put("Havana", 2);
        distances.get("London").put("Alexandria", 2);
        distances.get("London").put("Bombay", 3);

        // Lisbon connections
        distances.put("Lisbon", new HashMap<>());
        distances.get("Lisbon").put("London", 1);
        distances.get("Lisbon").put("Havana", 1);
        distances.get("Lisbon").put("Alexandria", 2);
        distances.get("Lisbon").put("Bombay", 3);

        // Havana connections
        distances.put("Havana", new HashMap<>());
        distances.get("Havana").put("Lisbon", 1);
        distances.get("Havana").put("London", 2);
        distances.get("Havana").put("Alexandria", 3);
        distances.get("Havana").put("Bombay", 3);

        // Alexandria connections
        distances.put("Alexandria", new HashMap<>());
        distances.get("Alexandria").put("London", 2);
        distances.get("Alexandria").put("Lisbon", 2);
        distances.get("Alexandria").put("Bombay", 1);
        distances.get("Alexandria").put("Havana", 3);

        // Bombay connections
        distances.put("Bombay", new HashMap<>());
        distances.get("Bombay").put("Alexandria", 1);
        distances.get("Bombay").put("London", 3);
        distances.get("Bombay").put("Lisbon", 3);
        distances.get("Bombay").put("Havana", 3);
    }

    public void start() {
        System.out.println("=== ECONOMY SIM ===");
        if (saveManager.hasSave()) {
            System.out.println("Saved file found! (1: Load game, 2: New game)");
            System.out.print(">> ");
            int loadChoice = scanner.nextInt();
            if (loadChoice == 1) {
                int[] dayWrapper = {currentDay};
                saveManager.load(player, markets, items, cities, dayWrapper);
                currentDay = dayWrapper[0];
            }
        }
        System.out.println("Reach " + WIN_GOLD + "g in " + MAX_DAYS + " days!");
        System.out.println("Starting gold: " + STARTING_GOLD + "g\n");

        while (currentDay <= MAX_DAYS) {
            eventManager.triggerEvent(markets, items);
            playDay();
            if (player.getGold() <= 0) {
                System.out.println("You went bankrupt! Game over.");
                return;
            }
            if (player.getGold() >= WIN_GOLD) {
                System.out.println("You reached " + WIN_GOLD + "g! You win!");
                return;
            }
            if (!gameReset) {
                for (Market market: markets) {
                    market.recordPrices();
                    market.dailyPriceUpdate();
                }
                currentDay++;
            }

        }
        System.out.println("Time's up! Final gold: " + player.getGold() + "g");
    }

    private void playDay() {
        player.resetActions();
        printPlayerStats();
        boolean dayEnded = false;

        while (!dayEnded && player.hasActions(1)) {
            gameReset = false;
            printMenu();
            System.out.print(">> ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> handleTradeMenu();
                case 2 -> { if (handleTravel()) dayEnded = true; }
                case 3 -> handleShipMenu();
                case 4 -> dayEnded = true;
                default -> System.out.println("Invalid choice.");
            }
            if (gameReset) return;
        }
    }

    private void printMenu() {
        System.out.println("\nWhat will you do?");
        System.out.println("1. Trade");
        System.out.println("2. Travel");
        System.out.println("3. Ship");
        System.out.println("4. End day");
    }

    private void handleBuy() {
        printMarket();
        System.out.println("Enter item number to buy:");
        System.out.print(">> ");
        int choice = scanner.nextInt() - 1;
        if (choice < 0 || choice >= items.size()) {
            System.out.println("Invalid item.");
            return;
        }
        System.out.println("How many?");
        System.out.print(">> ");
        int qty = scanner.nextInt();
        if (qty <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        boolean anySuccess = false;
        for (int i = 0; i < qty; i++) {
            boolean success = player.buy(items.get(choice), getMarketForCity(player.getCurrentCity()));
            if (!success) break;
            anySuccess = true;
        }
        if (anySuccess) player.useActions(1);
    }

    private void handleSell() {
        System.out.println("your inventory:");
        for (int i = 0; i < items.size(); i++) {
            int quantity = player.getInventory().getOrDefault(items.get(i), 0);
            if (quantity > 0) {
                System.out.println((i+1) + ". " + items.get(i).getName() + ": " + quantity);
            }
        }
        System.out.println("Enter item number to sell:");
        System.out.print(">> ");
        int choice = scanner.nextInt() - 1;
        if (choice < 0 || choice >= items.size()) {
            System.out.println("Invalid item.");
            return;
        }
        System.out.println("How many?");
        System.out.print(">> ");
        int qty = scanner.nextInt();
        if (qty <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        boolean anySuccess = false;
        for (int i = 0; i < qty; i++) {
            boolean success = player.sell(items.get(choice), getMarketForCity(player.getCurrentCity()));
            if (!success) break;
            anySuccess = true;
        }
        if (anySuccess) player.useActions(1);
    }

    private boolean handleTravel() {
        System.out.println("Choose destination:");
        for (int i = 0; i < cities.size(); i++) {
            City dest = cities.get(i);
            int dist = distances.get(player.getCurrentCity().getName()).getOrDefault(dest.getName(), 99);
            boolean canReach = dist <= player.getShip().getRange();
            System.out.println((i + 1) + ". " + dest.getName() + " (distance: " + dist + ")"
                    + (canReach ? "" : " [out of range]"));
        }
        System.out.print(">> ");
        int choice =scanner.nextInt() - 1;
        if (choice < 0 || choice >= cities.size()) {
            System.out.println("Invalid city.");
            return false;
        }
        City dest = cities.get(choice);
        int dist = distances.get(player.getCurrentCity().getName()).getOrDefault(dest.getName(), 99);
        if (dist > player.getShip().getRange()) {
            System.out.println("Your ship can't reach that far! Upgrade your ship.");
            return false;
        }
        return player.travel(dest, TRAVEL_COST * dist);
    }

    private void handleTradeMenu() {
        System.out.println("\n--- Trade ---");
        System.out.println("1. Buy");
        System.out.println("2. Sell");
        System.out.println("3. View market");
        System.out.println("4. Back");
        System.out.print(">> ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> handleBuy();
            case 2 -> handleSell();
            case 3 -> printMarket();
            case 4 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private void handleShipMenu() {
        System.out.println("\n--- Ship ---");
        System.out.println("1. View ship");
        System.out.println("2. Upgrade ship");
        System.out.println("3. Save game");
        System.out.println("4. Reset game");
        System.out.println("5. Back");
        System.out.print(">> ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> System.out.println(player.getShip());
            case 2 -> player.getShip().upgrade(player);
            case 3 -> saveManager.save(player, markets, currentDay);
            case 4 -> resetGame();
            case 5 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private void printMarket() {
        Market market = getMarketForCity(player.getCurrentCity());
        assert market != null;
        System.out.println("Market in: " + market.getCity().getName());
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).getName()
                    + ": " + market.getPrice(items.get(i)) + "g"
                    + " | history: " + market.getPriceHistory(items.get(i)));
        }

    }

    private Market getMarketForCity(City city) {
        for (Market market: markets) {
            if (market.getCity() == city) return market;
        }
        return null;
    }

    private void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println("\n".repeat(50)); // fallback
        }
    }

    private void printPlayerStats() {
        System.out.println("====================");
        System.out.println(" Day: " + currentDay + "/" + MAX_DAYS);
        System.out.println(" Gold: " + Math.round(player.getGold() * 100.0) / 100.0 + "g");
        System.out.println(" Location: " + player.getCurrentCity().getName());
        System.out.println(" Actions: " + player.getActionsRemaining() + "/" + Player.ACTIONS_PER_DAY);
        System.out.println(" Inventory:");
        for (int i = 0; i < items.size(); i++) {
            int quantity = player.getInventory().getOrDefault(items.get(i), 0);
            if (quantity > 0) {
                System.out.println((i+1) + ". " + items.get(i).getName() + ": " + quantity);
            }
        }
        System.out.println("====================");
    }

    private void resetGame() {
        System.out.println("Are you sure? (1: Yes, 2: No)");
        System.out.print(">> ");
        int confirm = scanner.nextInt();
        if (confirm == 1) {
            gameReset = true;
            saveManager.deleteSave();
            items.clear();
            cities.clear();
            markets.clear();
            currentDay = 1;
            initializeGame();
            System.out.println("Game reset!");
        }
    }
}









