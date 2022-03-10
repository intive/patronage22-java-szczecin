package com.intive.patronage22.szczecin.retroboard.model;

import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
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
@Table(schema = "retro", name = "board")
public class Board implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "maximum_number_of_votes", length = 16)
    private Integer maximumNumberOfVotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private EnumStateDto state = EnumStateDto.CREATED;

    @ManyToOne
    private User creator;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(schema = "retro",
            name = "users_boards",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "user_uid")
    )
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardCard> boardCards;
}
