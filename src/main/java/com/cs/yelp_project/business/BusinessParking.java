package com.cs.yelp_project.business;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BusinessParking {

    private String garage;
    private String street;
    private String validated;
    private String lot;
    private String valet;

    public BusinessParking() {}
}
