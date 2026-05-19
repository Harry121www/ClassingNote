package com.classnote.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TagRequest {
    @NotBlank @Size(min = 1, max = 30)
    private String name;
    private String color;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
