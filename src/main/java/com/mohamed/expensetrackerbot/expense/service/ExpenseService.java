package com.mohamed.expensetrackerbot.expense.service;

import com.mohamed.expensetrackerbot.expense.model.Expense;
import com.mohamed.expensetrackerbot.expense.repo.ExpenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
public class ExpenseService {


    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * ميثود تسجيل المصروف مع حماية (Validation) قوية
     */
    public Expense addExpense(Expense expense) {

        // 1. التأكد إن المبلغ موجود وأكبر من صفر
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("محاولة إدخال مبلغ غير صالح أو صفر من المستخدم: {}", expense.getUserId());
            throw new IllegalArgumentException("المبلغ لازم يكون أكبر من صفر يا صاحبي!");
        }

        // 2. حماية إضافية لو الرقم مبالغ فيه (أكبر من مليون مثلاً بالغلط)
        if (expense.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            log.warn("محاولة إدخال مبلغ ضخم جداً: {} من المستخدم: {}", expense.getAmount(), expense.getUserId());
            throw new IllegalArgumentException("الرقم ده كبير جداً، اتأكد من المبلغ يا غالي!");
        }

        // لو كله تمام، نحفظ في الداتا بيز ونسجل العملية
        log.info("تم حفظ مصروف جديد بقيمة {} للمستخدم {}", expense.getAmount(), expense.getUserId());
        return expenseRepository.save(expense);
    }

    /**
     * تقرير مصاريف اليوم بلهجة مصرية لذيذة
     */
    public String getTodayReport(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Expense> todayExpenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);

        if (todayExpenses.isEmpty()) {
            return "مفيش أي مصاريف متسجلة النهاردة.. عاش يا وحش التوفير! 💸😉";
        }

        BigDecimal total = BigDecimal.ZERO;
        StringBuilder report = new StringBuilder("📊 مصاريفك النهاردة يا بطل:\n\n");

        for (Expense exp : todayExpenses) {
            report.append("🔹 ").append(exp.getCategory()).append(": ").append(exp.getAmount()).append(" جنيه\n");
            total = total.add(exp.getAmount());
        }

        report.append("\n💰 الإجمالي اللي طار النهاردة: ").append(total).append(" جنيه");
        return report.toString();
    }

    /**
     * إرجاع إجمالي مصاريف الشهر كرقم (عشان نخصمها من الميزانية)
     */
    public BigDecimal getTotalMonthExpenses(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.from(today).atEndOfMonth().atTime(LocalTime.MAX);

        List<Expense> monthExpenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth);

        BigDecimal total = BigDecimal.ZERO;
        for (Expense exp : monthExpenses) {
            total = total.add(exp.getAmount());
        }

        return total;
    }


}
