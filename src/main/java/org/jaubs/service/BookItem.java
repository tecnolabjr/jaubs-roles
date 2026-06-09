package org.jaubs.service;

public class BookItem {

    private Long id;
    private String name;
    private String author;
    private Double price;
    private BookCondition condition;
    private String notes;
    private String creator;
    Boolean sold;

    public enum BookCondition {
        FAIR, GOOD, ASNEW
    }

    public BookItem(Long id, String name, String author, Double price, BookCondition condition,
                    String notes, String creator, Boolean sold) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
        this.condition = condition;
        this.notes = notes;
        this.creator = creator;
        this.sold = sold;
    }

    public static BookItem emptyItem() {
        return new BookItem(null, "", "", null, BookCondition.GOOD,
                "", "", false);
    }

    public synchronized Long getId() {
        return id;
    }

    public synchronized void setId(Long id) {
        this.id = id;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized String getAuthor() {
        return author;
    }

    public synchronized void setAuthor(String author) {
        this.author = author;
    }

    public synchronized Double getPrice() {
        return price;
    }

    public synchronized void setPrice(Double price) {
        this.price = price;
    }

    public synchronized BookCondition getCondition() {
        return condition;
    }

    public synchronized void setCondition(BookCondition condition) {
        this.condition = condition;
    }

    public synchronized String getNotes() {
        return notes;
    }

    public synchronized void setNotes(String notes) {
        this.notes = notes;
    }

    public synchronized String getCreator() {
        return creator;
    }

    public synchronized void setCreator(String creator) {
        this.creator = creator;
    }

    public synchronized Boolean getSold() {
        return sold;
    }

    public synchronized void setSold(Boolean sold) {
        this.sold = sold;
    }

    public synchronized void copyFrom(BookItem item) {
        if (this.id.equals(item.getId())) {
            this.setName(item.getName());
            this.setSold(item.getSold());
            this.setAuthor(item.getAuthor());
            this.setCondition(item.getCondition());
            this.setNotes(item.getNotes());
            this.setPrice(item.getPrice());
            this.setCreator(item.getCreator());
        }
    }

}
