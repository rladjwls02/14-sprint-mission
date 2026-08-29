package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@ToString
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private final String email;
    private String password;

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    public User(String email, String name, String password) {
        this.name = name;
        this.email = email;
        this.password = password;

        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}
