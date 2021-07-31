package me.tiger.modules.works.rest.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class WorksInfoReqDto {
    String userName;
    String phone;
    String description;
    String article;
}
