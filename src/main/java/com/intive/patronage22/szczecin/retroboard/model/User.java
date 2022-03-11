package com.intive.patronage22.szczecin.retroboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(schema = "retro", name = "user_data")
public class User implements Serializable {
    @Id
    @Column(name = "uid", length = 128, nullable = false, unique = true)
    private String uid;

    @Column(name = "email", length = 64)
    private String email;

    @Column(name = "display_name", length = 64)
    private String displayName;

    @ManyToMany(mappedBy = "users")
    private Set<Board> userBoards = new HashSet<>();

    @OneToMany(mappedBy = "creator")
    private Set<Board> createdBoards;
}
