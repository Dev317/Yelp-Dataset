package com.cs.yelp_project.checkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path="/checkin")
public class CheckinController {
    private CheckinService service;

    @Autowired
    public CheckinController(CheckinService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public Iterable<CheckIn> list() {
        return service.list();
    }
}
