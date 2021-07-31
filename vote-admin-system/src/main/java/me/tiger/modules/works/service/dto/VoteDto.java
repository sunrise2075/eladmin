package me.tiger.modules.works.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.tiger.annotation.Query;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@ApiModel(value = "投票请求体")
public class VoteDto {

    @ApiModelProperty(value = "作品id")
    private Integer worksId;

    @ApiModelProperty(value = "投票数量")
    private Integer count;

    @ApiModelProperty(value = "投票人用户名")
    private String voterUserName;

    /**
    * @website https://el-admin.vip
    * @description /
    * @author tiger
    * @date 2021-07-31
    **/
    @Data
    public static class WorksPodcastDto implements Serializable {

        private Integer id;

        /** 直播链接地址 */
        private String url;

        /** 海报相对路径 */
        private String imagePath;

        /** 直播开始时间 */
        private Timestamp beginTime;

        /** 直播创建日期 */
        private Timestamp createdTime;
    }

    /**
    * @website https://el-admin.vip
    * @author tiger
    * @date 2021-07-31
    **/
    @Data
    public static class WorksPodcastQueryCriteria{

        /** 模糊 */
        @Query(type = Query.Type.INNER_LIKE)
        private String url;

        /** 精确 */
        @Query
        private String imagePath;
        /** BETWEEN */
        @Query(type = Query.Type.BETWEEN)
        private List<Timestamp> beginTime;
    }
}
