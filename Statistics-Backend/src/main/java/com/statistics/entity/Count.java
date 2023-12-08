package com.statistics.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Count {
    @Id
    private String id;
    private int count;

    private Count(String id, int count) {
        this.id = id;
        this.count = count;
    }

    public static Count createOf(String id, int count) {
        return new Count(id, count);
    }
}