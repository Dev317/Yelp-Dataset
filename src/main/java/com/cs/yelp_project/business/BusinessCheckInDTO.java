package com.cs.yelp_project.business;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class BusinessCheckInDTO implements Serializable {

    private static final long serialVersionUID = -8661467404585749884L;

    @NotBlank
    private String id;

    @NotBlank
    private String categories;

    @NotBlank
    private String state;

    private int totalCheckIn;

    public BusinessCheckInDTO(String id, String categories, String state, int totalCheckIn) {
        this.id = id;
        this.categories = categories;
        this.state = state;
        this.totalCheckIn = totalCheckIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getTotalCheckIn() {
        return totalCheckIn;
    }

    public void setTotalCheckIn(int totalCheckIn) {
        this.totalCheckIn = totalCheckIn;
    }

    @Override
    public String toString() {
        return "BusinessCheckInDTO{" +
                "id='" + id + '\'' +
                ", categories='" + categories + '\'' +
                ", state='" + state + '\'' +
                ", totalCheckIn=" + totalCheckIn +
                '}';
    }
}
