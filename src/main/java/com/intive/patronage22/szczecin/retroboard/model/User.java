package com.intive.patronage22.szczecin.retroboard.model;

import org.sonatype.inject.Nullable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
public class User implements Serializable {
    @Id
    @NotBlank
    @Size(max = 128)
    private String uid;

    @Nullable
    @Size(max = 64)
    private String name;
}
