package com.zalo.Spring_Zalo.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private int id;
    private String name;
    private String banner;
    
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate timeStartEvent;
    
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate timeEndEvent;
    
    private int point;
    
    private boolean visible;
}


