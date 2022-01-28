package com.intive.patronage22.szczecin.retroboard.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table
public class User implements Serializable {
    @Id
    @Column(length = 128, nullable = false, unique = true)
    private String uid;

    @Column(length = 64)
    private String name;
}
