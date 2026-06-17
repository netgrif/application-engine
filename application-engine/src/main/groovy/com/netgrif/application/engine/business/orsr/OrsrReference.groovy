package com.netgrif.application.engine.business.orsr

@Deprecated(since = "7.0.0")
class OrsrReference {

    String id

    String name = ""

    String created = ""

    String street = ""

    String streetNumber = ""

    String city = ""

    String postalCode = ""

    @Override
    String toString() {
        return "$name {created: $created, address: [$street $streetNumber, $city, $postalCode]}"
    }
}