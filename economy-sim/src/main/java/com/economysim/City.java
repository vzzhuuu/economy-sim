package com.economysim;

import java.util.List;

public class City {
    private String name;
    private List<Item> specialtyItems;

    public City(String name, List<Item> specialtyItem) {
        this.name = name;
        this.specialtyItems = specialtyItem;
    }

    public String getName() {
        return name;
    }

    public List<Item> getSpecialtyItem() {
        return specialtyItems;
    }

    public boolean isSpecialty(Item item) {
        return specialtyItems.contains(item);
    }

    @Override
    public String toString() {
        return name + " (specialises in: " + specialtyItems.stream()
                .map(Item::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none") + ")";
    }}
