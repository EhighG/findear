package com.findear.main.board.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter @Setter
@NoArgsConstructor
public class AiGeneratedColumnDto {
    private String category;
    private String color;
    private String description;
}
