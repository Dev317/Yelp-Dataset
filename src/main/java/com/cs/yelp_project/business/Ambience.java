package com.cs.yelp_project.business;

import lombok.AllArgsConstructor;
import lombok.Data;

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