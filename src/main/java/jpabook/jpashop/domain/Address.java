package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;
import javax.persistence.Inheritance;

@Embeddable
@Getter
// 값타입은 기본적으로 변경되면 안된다.
public class Address {
    protected Address() { }

    private String city;
    private String street;
    private String zipcode;

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
