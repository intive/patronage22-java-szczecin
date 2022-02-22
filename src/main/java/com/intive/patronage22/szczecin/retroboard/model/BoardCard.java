package com.intive.patronage22.szczecin.retroboard.model;

import com.intive.patronage22.szczecin.retroboard.dto.BoardCardsColumn;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "retro", name = "board_card")
@Data
public class BoardCard implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(name = "text", length = 128, nullable = false)
    private String text;

    @Column(name = "column", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private BoardCardsColumn column;

    @ManyToOne
    @JoinColumn(name = "creator_uid")
    private User creator;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardCardAction> boardCardActions;
}
