package com.cs.yelp_project.business;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoodForMeal {
    private Boolean dessert;
    private Boolean latenight;
    private Boolean lunch;
    private Boolean dinner;
    private Boolean brunch;
    private Boolean breakfast;

    public GoodForMeal() {}
}
