package com.acra.model;


import lombok.Data;

@Data
public class Rule {
    private Long id;
    private String name;
    private String description;
    private boolean enabled;
}