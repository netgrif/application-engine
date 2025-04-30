package com.netgrif.application.engine.objects.business.orsr;

public class OrsrReference {
    String id;

    String name = "";

    String created = "";

    String street = "";

    String streetNumber = "";

    String city = "";

    String postalCode = "";

    @Override
    public String toString() {
        return "$name {created: $created, address: [$street $streetNumber, $city, $postalCode]}";
    }
}
