package com.intive.patronage22.szczecin.retroboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "retro", name = "board_card_votes")
@Data
public class BoardCardVotes implements Serializable {

    @EmbeddedId
    private BoardCardVotesKey id;

    @ManyToOne
    @MapsId("cardId")
    @JoinColumn(name = "card_id")
    private BoardCard card;

    @ManyToOne
    @MapsId("voter")
    @JoinColumn(name = "voter_uid")
    private User voter;

    @Column(name = "count", length = 16)
    private Integer votes;

}
