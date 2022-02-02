package com.intive.patronage22.szczecin.retroboard.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(mappedBy = "users")
    private Set<Board> userBoards = new HashSet<>();
}
