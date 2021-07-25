package me.tiger.modules.works.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "work_article")
public class WorksArticle {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "works_id")
    private Integer worksId;

    @Column(name = "article_content")
    private String articleContent;

}
