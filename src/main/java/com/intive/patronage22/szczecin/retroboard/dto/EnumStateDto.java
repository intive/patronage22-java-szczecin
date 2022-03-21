package com.intive.patronage22.szczecin.retroboard.dto;

public enum EnumStateDto {

    CREATED, VOTING, ACTIONS, DONE;

    private static final EnumStateDto[] vals = values();

    public EnumStateDto next()
    {
        return vals[(this.ordinal()+1)];
    }
}

