package me.julionxn.ttapp.endpoint.model;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class User {

    private Long id;
    private Long timeArrival; //ya
    private Long timeAttendance; //ya
    private Long timeStartProduction;
    private Long timeStartProductGive; //ya
    private Long timeEndProductGive; //ya
    private Long arrivalPreparation;
    private Long timeStartPreparation; //ya
    private Long timeEndPreparation; //ya
    private Long arrivalCash;
    private Long timeStartCash;
    private Long timeEndCash;
    private Long endTime;

    private List<Stops> flow = new ArrayList<>();
    private Map<Long, Product> products = new HashMap<>();

}
