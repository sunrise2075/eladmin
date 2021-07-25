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
@Table(name = "works_files")
public class WorksFiles {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "works_id")
    private Integer worksId;

    @Column(name = "relative_file_path")
    private String relativeFilePath;

}
