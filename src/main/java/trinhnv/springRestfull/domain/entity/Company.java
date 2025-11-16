package trinhnv.springRestfull.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Entity(name = "companies")
@Table

public class Company extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="Tên công ty không được để trống")
    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private String address;

    private String logo;


}
