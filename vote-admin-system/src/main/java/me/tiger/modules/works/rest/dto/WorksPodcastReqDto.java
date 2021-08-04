package me.tiger.modules.works.rest.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class WorksPodcastReqDto {
    private String url;
    private String imagePath;
    private Timestamp beginTime;
}
