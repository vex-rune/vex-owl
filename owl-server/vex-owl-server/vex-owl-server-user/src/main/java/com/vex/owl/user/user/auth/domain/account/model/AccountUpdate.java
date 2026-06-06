package com.vex.owl.user.user.auth.domain.account.model;

import jakarta.validation.constraints.Max;

import java.util.function.Supplier;

public record AccountUpdate(
        String id,
        @Max(150) String account,
        Supplier<String> password
) {
}