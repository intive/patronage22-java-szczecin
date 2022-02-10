package com.intive.patronage22.szczecin.retroboard.model;

import com.intive.patronage22.szczecin.retroboard.dto.EnumStateDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
    @Column(name = "state", nullable = false)
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
