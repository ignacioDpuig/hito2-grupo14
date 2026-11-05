package com.grupo14.hito2.events;

/** Se publica al dar de alta un usuario; el mail se manda recién cuando la transacción se confirma. */
public record CredencialesGeneradasEvent(String email, String nombre, String password) {
}
