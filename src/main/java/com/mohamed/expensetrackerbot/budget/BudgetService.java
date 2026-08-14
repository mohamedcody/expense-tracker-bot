package com.mohamed.expensetrackerbot.budget;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class BudgetService {


    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public String setBudget(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "❌ الميزانية لازم تكون أكبر من صفر يا غالي.";
        }

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        Budget budget = budgetRepository
                .findByUserIdAndMonthAndYear(userId, month, year)
                .orElse(
                        Budget.builder()
                                .userId(userId)
                                .month(month)
                                .year(year)
                                .build()
                );

        budget.setAmount(amount);
        budgetRepository.save(budget);

        return "✅ تمام يا صاحبي! ميزانيتك الشهرية اتسجلت بنجاح\n\n"
                + "💰 الميزانية: " + amount + " جنيه\n"
                + "📅 شهر: " + month + "/" + year;
    }

    public String getBudgetStatus(
            Long userId,
            BigDecimal spent
    ) {

        LocalDate today = LocalDate.now();

        Budget budget = budgetRepository
                .findByUserIdAndMonthAndYear(
                        userId,
                        today.getMonthValue(),
                        today.getYear()
                )
                .orElse(null);

        if (budget == null) {
            return "⚠️ إنت لسه محددتش ميزانية الشهر ده.\n"
                    + "دوس على /budget واكتبلي الميزانية بتاعتك.";
        }

        BigDecimal remaining = budget.getAmount().subtract(spent);

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal exceeded = remaining.abs();

            return "🚨 خلي بالك! إنت كده عديت الميزانية يا صاحبي!\n\n"
                    + "💰 كنت محدد: " + budget.getAmount() + " جنيه\n"
                    + "💸 صرفت لحد دلوقتي: " + spent + " جنيه\n"
                    + "🔴 عديت الليمت بـ: " + exceeded + " جنيه 🤦‍♂️";
        }

        return "📊 كشف حساب الميزانية\n\n"
                + "💰 الليمت بتاعك: " + budget.getAmount() + " جنيه\n"
                + "💸 اللي صرفته: " + spent + " جنيه\n"
                + "💵 الباقي معاك: " + remaining + " جنيه (حاول تمسك إيدك شوية 😉)";
    }
}