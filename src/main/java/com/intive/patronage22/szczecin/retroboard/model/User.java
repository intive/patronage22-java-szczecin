package com.intive.patronage22.szczecin.retroboard.model;

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
@Table(name = "User")
public class User implements Serializable {
    @Id
    @Column(name = "uid", length = 128, nullable = false, unique = true)
    private String uid;

    @Column(name = "name", length = 64)
    private String name;

    @ManyToMany(mappedBy = "users")
    private Set<Board> userBoards = new HashSet<>();
}
