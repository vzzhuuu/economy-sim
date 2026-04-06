package com.economysim;

public class City {
    private String name;
    private Item specialtyItem;

    public City(String name, Item specialtyItem) {
        this.name = name;
        this.specialtyItem = specialtyItem;
    }

    public String getName() {
        return name;
    }

    public Item getSpecialtyItem() {
        return specialtyItem;
    }

    @Override
    public String toString() {
        return name + " (specializes in: " + specialtyItem.getName() + ")";
    }
}
