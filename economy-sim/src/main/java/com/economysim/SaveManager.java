package com.economysim;

import org.sqlite.core.DB;

import java.sql.*;
import java.util.List;

public class SaveManager {
    private static final String DB_URL = "jdbc:sqlite:savegame.db";

    public SaveManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS game_state (id INTEGER PRIMARY KEY, day INTEGER, gold REAL, current_city TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (item_name TEXT PRIMARY KEY, quantity INTEGER)");

            stmt.execute("CREATE TABLE IF NOT EXISTS market_prices (city TEXT, item_name TEXT, price REAL, PRIMARY KEY (city, item_name))");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void save(Player player, List<Market> markets, int currentDay) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // save game state
            PreparedStatement gs = conn.prepareStatement(
                    "INSERT OR REPLACE INTO game_state (id, day, gold, current_city) VALUES (1, ?, ?,?)"
            );
            gs.setInt(1, currentDay);
            gs.setDouble(2, player.getGold());
            gs.setString(3, player.getCurrentCity().getName());
            gs.executeUpdate();

            // save inventory
            PreparedStatement inv = conn.prepareStatement(
                    "INSERT OR REPLACE INTO inventory (item_name, quantity) VALUES (?, ?)"
            );
            for (var entry: player.getInventory().entrySet()) {
                inv.setString(1, entry.getKey().getName());
                inv.setInt(2, entry.getValue());
                inv.executeUpdate();
            }

            // save market prices
            PreparedStatement mp = conn.prepareStatement(
                    "INSERT OR REPLACE INTO market_prices (city, item_name, price) VALUES (?, ?, ?)"
            );
            for (Market market: markets) {
                for (var entry: market.getPrices().entrySet()) {
                    mp.setString(1, market.getCity().getName());
                    mp.setString(2, entry.getKey().getName());
                    mp.setDouble(3, entry.getValue());
                    mp.executeUpdate();
                }
            }

            System.out.println("Game saved!");
        } catch (SQLException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    public boolean hasSave() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM game_state")) {
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public void load(Player player,List<Market> markets, List<Item> items, List<City> cities, int[] currentDay) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // load game state
            ResultSet gs = conn.createStatement().executeQuery("SELECT * FROM game_state WHERE id = 1");
            currentDay[0] = gs.getInt("day");
            player.setGold(gs.getDouble("gold"));
            String cityName = gs.getString("current_city");
            for (City city: cities) {
                if (city.getName().equals(cityName)) {
                    player.setCurrentCity(city);
                    break;
                }
            }

            // load inventory
            ResultSet inv = conn.createStatement().executeQuery("SELECT * FROM inventory");
            while (inv.next()) {
                String itemName = inv.getString("item_name");
                int quantity = inv.getInt("quantity");
                for (Item item: items) {
                    if (item.getName().equals(itemName)) {
                        player.getInventory().put(item, quantity);
                        break;
                    }
                }
            }

            // load market prices
            ResultSet mp = conn.createStatement().executeQuery("SELECT * FROM market_prices");
            while (mp.next()) {
                String cityName2 = mp.getString("city");
                String itemName = mp.getString("item_name");
                double price = mp.getDouble("price");
                for (Market market: markets) {
                    if (market.getCity().getName().equals(cityName2)) {
                        for (Item item: items) {
                            if (item.getName().equals(itemName)) {
                                market.getPrices().put(item, price);
                                break;
                            }
                        }
                    }
                }
            }

            System.out.println("Game loaded!");
        } catch (SQLException e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }

    public void deleteSave() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM game_state");
            stmt.execute("DELETE FROM inventory");
            stmt.execute("DELETE FROM market_prices");

            System.out.println("Save deleted!");
        } catch (SQLException e) {
            System.out.println("Error deleting save: " + e.getMessage());
        }
    }
}
