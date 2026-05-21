package com.vex.owl.auth.domain.code.model;

import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String targetId;
    private String type;
}