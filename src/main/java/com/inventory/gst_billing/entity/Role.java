package com.inventory.gst_billing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id   //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // identity strat means auto increment 1,2,3...
    //GENRATED BY DB NOT HIBERNATE, DELETION WILL LEAD TO SKIPPED NO SO NOT NECESSARY SEQUENTIAL
    // IDEALLY BEST TO USE SEQUENCE TYPE THAN IDENTITY FOR POST GRES CAUSE BATCH INSERT N FASTER
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String roleName;


}