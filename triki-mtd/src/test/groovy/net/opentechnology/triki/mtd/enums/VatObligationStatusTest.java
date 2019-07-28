package net.opentechnology.triki.mtd.enums;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class VatObligationStatusTest {

    @Test
    public void testIndexOf(){
        List<String> values = new ArrayList<>();
        values.add("All");
        values.add("Fulfilled");
        values.add("Open");

        assertEquals(0, values.indexOf("All"));
    }

}