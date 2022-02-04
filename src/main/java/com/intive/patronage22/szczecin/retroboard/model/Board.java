package com.intive.patronage22.szczecin.retroboard.model;

import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Board")
public class Board implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Enumerated
    private EnumStateDto state = EnumStateDto.CREATED;

    @OneToOne
    private User creator;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "BoardUsers",
            joinColumns = {@JoinColumn(name = "id")},
            inverseJoinColumns = {@JoinColumn(name = "uid")}
    )
    private Set<User> users = new HashSet<>();
}
