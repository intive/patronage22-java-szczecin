package com.intive.patronage22.szczecin.retroboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class BoardCardVotesKey implements Serializable {

    @Column(name = "card_id", nullable = false)
    Integer cardId;

    @Column(name = "voter_uid", nullable = false)
    String voter;
}