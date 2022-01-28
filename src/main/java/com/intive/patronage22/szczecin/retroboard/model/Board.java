package com.intive.patronage22.szczecin.retroboard.model;

import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Board implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 32, nullable = false)
    private String name;

    @Column(nullable = false)
    private EnumStateDto state;

    @OneToOne
    private User creator;
}
