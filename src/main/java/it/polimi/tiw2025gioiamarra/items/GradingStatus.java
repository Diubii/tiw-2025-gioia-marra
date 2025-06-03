package it.polimi.tiw2025gioiamarra.items;

public enum GradingStatus {
    NotInserted("Non inserito"),
    Inserted("Inserito"),
    Published("Pubblicato"),
    Rejected("Rifiutato"),
    Verbalized("Verbalizzato");

    final String name;
    GradingStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
