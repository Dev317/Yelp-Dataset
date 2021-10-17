package com.cs.yelp_project.checkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@Data
@Getter
public class CheckIn {

    @Id
    private String business_id;

    @Length(max=5000000)
    private String date;
    private Integer total_checkin;

    public CheckIn() {}


}
