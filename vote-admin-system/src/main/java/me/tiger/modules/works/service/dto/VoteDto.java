package me.tiger.modules.works.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "投票请求体")
public class VoteDto {

    @ApiModelProperty(value = "作品id")
    private Integer worksId;

    @ApiModelProperty(value = "投票数量")
    private Integer count;

    @ApiModelProperty(value = "投票人用户名")
    private String voterUserName;
}
