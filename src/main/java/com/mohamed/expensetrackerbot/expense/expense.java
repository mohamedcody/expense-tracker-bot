package com.mohamed.expensetrackerbot.expense;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "expenses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // ID اليوزر على تليجرام عشان كل واحد يشوف مصاريفه هو بس

    @Column(nullable = false)
    private BigDecimal amount; // المبلغ اللي اتصرف

    @Column(nullable = false)
    private String category; // نوع المصروف (أكل، مواصلات، فواتير...)

    private String description; // تفاصيل المصروف لو اليوزر كتبها

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // وقت تسجيل المصروف

    // الميثود دي بتخلي الوقت يتسجل تلقائي أول ما نعمل Save في الداتا بيز
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }




}

