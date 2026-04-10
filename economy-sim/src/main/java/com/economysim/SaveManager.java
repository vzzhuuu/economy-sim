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

            stmt.execute("CREATE TABLE IF NOT EXISTS ship (id INTEGER PRIMARY KEY, type TEXT, cargo_capacity INTEGER, range INTEGER, upgrade_cost REAL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS active_contracts (id INTEGER PRIMARY KEY AUTOINCREMENT, target_city TEXT, item_name TEXT, quantity INTEGER, reward REAL, deadline INTEGER)");

            stmt.execute("CREATE TABLE IF NOT EXISTS city_contracts (id INTEGER PRIMARY KEY AUTOINCREMENT, city TEXT, target_city TEXT, item_name TEXT, quantity INTEGER, reward REAL, deadline INTEGER)");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void save(Player player, List<Market> markets, int currentDay, List<Contract> activeContracts, List<City> cities) {
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

            // save ship
            PreparedStatement ship = conn.prepareStatement("INSERT OR REPLACE INTO ship (id, type, cargo_capacity, range, upgrade_cost) VALUES (1, ?, ?, ?, ?");
            ship.setString(1, player.getShip().getType().name());
            ship.setInt(2, player.getShip().getCargoCapacity());
            ship.setInt(3, player.getShip().getRange());
            ship.setDouble(4, player.getShip().getUpgradeCost());
            ship.executeUpdate();

            // save active contracts
            conn.createStatement().execute("DELETE FROM active_contracts");
            PreparedStatement ac = conn.prepareStatement("INSERT INTO active_contracts (target_city, item_name, quantity, reward, deadline) VALUES (?, ?, ?, ?, ?)");
            for (Contract c: activeContracts) {
                ac.setString(1, c.getTargetCity().getName());
                ac.setString(2, c.getRequiredItem().getName());
                ac.setInt(3, c.getRequiredQuantity());
                ac.setDouble(4, c.getReward());
                ac.setInt(5, c.getDeadline());
                ac.executeUpdate();
            }

            // save city contracts
            conn.createStatement().execute("DELETE FROM city_contracts");
            PreparedStatement cc = conn.prepareStatement("INSERT INTO city_contracts (city, target_city, item_name, quantity, reward, deadline) VALUES (?, ?, ?, ?, ?, ?)");
            for (City city: cities) {
                for (Contract c: city.getAvailableContracts()) {
                    cc.setString(1, city.getName());
                    cc.setString(2, c.getTargetCity().getName());
                    cc.setString(3, c.getRequiredItem().getName());
                    cc.setInt(4, c.getRequiredQuantity());
                    cc.setDouble(5, c.getReward());
                    cc.setInt(6, c.getDeadline());
                    cc.executeUpdate();
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

    public void load(Player player,List<Market> markets, List<Item> items, List<City> cities, int[] currentDay, List<Contract> activeContracts) {
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

            // load ship
            ResultSet ship = conn.createStatement().executeQuery("SELECT * FROM ship WHERE id = 1");
            if (ship.next()) {
                String shipType = ship.getString("type");
                player.getShip().loadFromSave(
                        Ship.Type.valueOf(shipType),
                        ship.getInt("cargo_capacity"),
                        ship.getInt("range"),
                        ship.getDouble("upgrade_cost")
                );
            }

            // load active contracts
            ResultSet ac = conn.createStatement().executeQuery("SELECT * FROM active_contracts");
            activeContracts.clear();
            while (ac.next()) {
                String targetCityName = ac.getString("target_city");
                String item_name = ac.getString("item_name");
                City targetCity = cities.stream().filter(c -> c.getName().equals(targetCityName))
                        .findFirst().orElse(null);
                Item item = items.stream().filter(i -> i.getName().equals(item_name))
                        .findFirst().orElse(null);
                if (targetCity != null && item != null) {
                    activeContracts.add(new Contract(targetCity, item, ac.getInt("quantity"),
                            ac.getDouble("reward"), ac.getInt("deadline")));
                }
            }

            // load city contracts
            ResultSet cc = conn.createStatement()
                    .executeQuery("SELECT * FROM city_contracts");
            for (City city : cities) {
                city.getAvailableContracts().clear();
            }
            while (cc.next()) {
                String cityName3 = cc.getString("city");
                String targetCityName = cc.getString("target_city");
                String itemName = cc.getString("item_name");
                City city = cities.stream().filter(c -> c.getName().equals(cityName3))
                        .findFirst().orElse(null);
                City targetCity = cities.stream().filter(c -> c.getName().equals(targetCityName))
                        .findFirst().orElse(null);
                Item item = items.stream().filter(i -> i.getName().equals(itemName))
                        .findFirst().orElse(null);
                if (city != null && targetCity != null && item != null) {
                    city.getAvailableContracts().add(new Contract(targetCity, item, cc.getInt("quantity"),
                            cc.getDouble("reward"), cc.getInt("deadline")));
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
