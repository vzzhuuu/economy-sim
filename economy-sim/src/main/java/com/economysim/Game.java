package com.economysim;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Game {
    private static final int MAX_DAYS = 30;
    private static final double WIN_GOLD = 1000.0;
    private static final double STARTING_GOLD = 200.0;
    private static final double TRAVEL_COST = 10.0;

    private Player player;
    private List<Item> items;
    private List<City> cities;
    private List<Market> markets;
    private EventManager eventManager;
    private Scanner scanner;
    private int currentDay;

    public Game() {
        this.items = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.markets = new ArrayList<>();
        this.eventManager = new EventManager();
        this.scanner = new Scanner(System.in);
        this.currentDay = 1;
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
            currentDay++;
        }
        System.out.println("Time's up! Final gold: " + player.getGold() + "g");
    }

    private void playDay() {
        clearScreen();
        System.out.println("\n--- Day " + currentDay + " ---");
        System.out.println(player);
        boolean dayEnded = false;

        while (!dayEnded) {
            printMenu();
            System.out.print(">> ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> handleBuy();
                case 2 -> handleSell();
                case 3 -> { if (handleTravel()) dayEnded = true; }
                case 4 -> printMarket();
                case 5 -> dayEnded = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void printMenu() {
        clearScreen();
        System.out.println("\nWhat will you do?");
        System.out.println("1. Buy");
        System.out.println("2. Sell");
        System.out.println("3. Travel (ends day)");
        System.out.println("4. View market");
        System.out.println("5. End day");
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
        player.buy(items.get(choice), getMarketForCity(player.getCurrentCity()));
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
        player.sell(items.get(choice), getMarketForCity(player.getCurrentCity()));
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
        System.out.println(market);
        for (int i = 0; i < items.size(); i++) {
            assert market != null;
            System.out.println((i + 1) + ". " + items.get(i).getName()
            + ". " + market.getPrice(items.get(i)) + "g");
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}









