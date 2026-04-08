package com.economysim;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Game {
    private static final int MAX_DAYS = 30;
    private static final double WIN_GOLD = 750.0;
    private static final double STARTING_GOLD = 450.0;
    private static final double TRAVEL_COST = 5.0;

    private Player player;
    private List<Item> items;
    private List<City> cities;
    private List<Market> markets;
    private EventManager eventManager;
    private Scanner scanner;
    private int currentDay;
    private SaveManager saveManager;
    private boolean gameReset = false;

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
        Item wheat = new Item("Wheat", 10.0);
        Item silk = new Item("Silk", 50.0);
        Item spice = new Item("Spice", 30.0);
        items.add(wheat);
        items.add(silk);
        items.add(spice);

        // cities
        City london = new City("London", wheat);
        City venice = new City("Venice", silk);
        City cairo = new City("Cairo", spice);
        cities.add(london);
        cities.add(venice);
        cities.add(cairo);

        // markets
        markets.add(new Market(london, items));
        markets.add(new Market(venice, items));
        markets.add(new Market(cairo, items));

        player = new Player(STARTING_GOLD, london);
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
                case 1 -> handleBuy();
                case 2 -> handleSell();
                case 3 -> { if (handleTravel()) dayEnded = true; }
                case 4 -> printMarket();
                case 5 -> dayEnded = true;
                case 6 -> saveManager.save(player, markets, currentDay);
                case 7 -> resetGame();
                default -> System.out.println("Invalid choice.");
            }
            if (gameReset) return;
        }
    }

    private void printMenu() {
//        clearScreen();
        System.out.println("\nWhat will you do?");
        System.out.println("1. Buy");
        System.out.println("2. Sell");
        System.out.println("3. Travel (ends day)");
        System.out.println("4. View market");
        System.out.println("5. End day");
        System.out.println("6. Save game");
        System.out.println("7. Reset game");
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
            System.out.println((i+1) + ". " + items.get(i).getName() + ": " + quantity);
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
            System.out.println((i + 1) + ". " + cities.get(i).getName());
        }
        System.out.print(">> ");
        int choice =scanner.nextInt() - 1;
        if (choice < 0 || choice >= cities.size()) {
            System.out.println("Invalid city.");
            return false;
        }
        return player.travel(cities.get(choice), TRAVEL_COST);
    }

    private void printMarket() {
        Market market = getMarketForCity(player.getCurrentCity());
        // System.out.println(market);
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
            int qty = player.getInventory().getOrDefault(items.get(i), 0);
            System.out.println("   " + items.get(i).getName() + ": " + qty);
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









