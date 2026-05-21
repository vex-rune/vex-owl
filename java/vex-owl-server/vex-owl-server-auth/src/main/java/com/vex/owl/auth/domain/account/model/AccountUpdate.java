package com.vex.owl.auth.domain.account.model;

import jakarta.validation.constraints.Max;

import java.util.function.Supplier;

public record AccountUpdate(
        String id,
        @Max(150) String account,
        Supplier<String> password
) {
}