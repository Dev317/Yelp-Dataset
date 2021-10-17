package com.cs.yelp_project.business;

import lombok.AllArgsConstructor;
import lombok.Data;


import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
public class Ambience {

    private Boolean touristy;
    private Boolean hipster;
    private Boolean romantic;
    private Boolean divey;
    private Boolean intimate;
    private Boolean trendy;
    private Boolean upscale;
    private Boolean classy;
    private Boolean casual;

    public Ambience() {}
}
