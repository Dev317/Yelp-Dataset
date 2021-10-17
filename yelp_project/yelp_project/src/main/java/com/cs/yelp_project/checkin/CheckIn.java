package com.cs.yelp_project.checkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.validator.constraints.Length;


@Entity
@AllArgsConstructor
@Data
@Setter
@Getter
public class CheckIn {

    @Id
    private String business_id;

    @Length(max=5000000)
    private String date;
    private Integer total_checkin;

    public CheckIn() {}


}
