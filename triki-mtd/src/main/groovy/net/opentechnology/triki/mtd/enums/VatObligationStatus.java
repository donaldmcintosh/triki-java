package net.opentechnology.triki.mtd.enums;

import java.util.Arrays;
import java.util.Optional;

public enum VatObligationStatus {
    O("Open"),
    F("Fulfilled");

    private final String description;

    private VatObligationStatus(String description){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static VatObligationStatus getVatObligationStatus(String desc){
        Optional<VatObligationStatus> optOblig = Arrays.stream(VatObligationStatus.values())
                                                       .filter(oblig -> oblig.description.equals(desc))
                                                       .findFirst();

        if(optOblig.isPresent()){
            return optOblig.get();
        }
        else {
            return null;
        }
    }
}
