package net.opentechnology.triki.mtd.enums;

import java.util.Arrays;
import java.util.Optional;

public enum VatObligationStatus {
    ALL("All", null),
    O("Open", "O"),
    F("Fulfilled", "F");

    private final String description;
    private final String code;

    private VatObligationStatus(String description, String code){
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static VatObligationStatus getVatObligationStatus(String desc) throws EnumValueException {
        Optional<VatObligationStatus> optOblig = Arrays.stream(VatObligationStatus.values())
                                                       .filter(oblig -> oblig.description.equals(desc))
                                                       .findFirst();

        if(optOblig.isPresent()){
            return optOblig.get();
        }
        else {
            throw new EnumValueException("Could not find status " + desc);
        }
    }
}
