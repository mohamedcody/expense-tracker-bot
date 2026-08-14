package com.mohamed.expensetrackerbot.expense.service;

import com.mohamed.expensetrackerbot.expense.model.Expense;
import com.mohamed.expensetrackerbot.expense.repo.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {


    private final ExpenseRepository expenseRepository;

    // Clean Code Tip: بنستخدم Constructor Injection بدل @Autowired
    // دي الطريقة الاحترافية والأكثر أماناً عشان نربط الكلاسات ببعض
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * ميثود نظيفة لحفظ المصروف الجديد
     */
    public Expense addExpense(Expense expense) {
        // ممكن نحط هنا Business Logic (مثلاً نتأكد إن المبلغ أكبر من صفر)
        if (expense.getAmount() == null || expense.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("المبلغ لازم يكون أكبر من صفر يا صاحبي!");
        }

        // لو كله تمام، بنبعت الداتا للـ Repository عشان يحفظها في Supabase
        return expenseRepository.save(expense);
    }


    // ضيف الميثود دي جوه الكلاس:
    public String getTodayReport(Long userId) {
        // 1. نحدد بداية اليوم (الساعة 12 بالليل) ونهاية اليوم (الساعة 11:59 بالليل)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 2. نجيب المصاريف من الداتا بيز
        List<Expense> todayExpenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);

        // 3. لو مفيش مصاريف
        if (todayExpenses.isEmpty()) {
            return "مفيش أي مصاريف متسجلة النهاردة، عاش يا وحش التوفير! 💸";
        }

        // 4. لو فيه مصاريف، نجمعها ونرتبها في رسالة
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder report = new StringBuilder("📊 مصاريفك النهاردة:\n\n");

        for (Expense exp : todayExpenses) {
            report.append("🔹 ").append(exp.getCategory()).append(": ").append(exp.getAmount()).append("\n");
            total = total.add(exp.getAmount());
        }

        report.append("\n💰 الإجمالي: ").append(total);
        return report.toString();
    }




    public String getMonthReport(Long userId) {
        // 1. نحدد بداية ونهاية الشهر الحالي
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        // بنستخدم YearMonth عشان يعرف الشهر ده 30 ولا 31 ولا 28 يوم
        LocalDateTime endOfMonth = YearMonth.from(today).atEndOfMonth().atTime(LocalTime.MAX);

        // 2. نستخدم نفس الميثود القديمة بتاعت الـ Repository (ودي ميزة الـ Clean Code!)
        List<Expense> monthExpenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth);

        // 3. لو مفيش مصاريف
        if (monthExpenses.isEmpty()) {
            return "مفيش أي مصاريف متسجلة الشهر ده.. كمل توفير يا وحش! 💸";
        }

        // 4. لو فيه مصاريف، هنجمع الإجمالي بتاع الشهر
        BigDecimal total = BigDecimal.ZERO;

        for (Expense exp : monthExpenses) {
            total = total.add(exp.getAmount());
        }

        return "📊 تقرير شهر " + today.getMonthValue() + ":\n\n" +
                "💰 إجمالي اللي صرفته الشهر ده: " + total + "\n" +
                "خد بالك من ميزانيتك يا صاحبي! 🚨";
    }


    // الميثود دي هترجع إجمالي مصاريف الشهر كرقم عشان نقدر نخصمه من الميزانية
    public BigDecimal getTotalMonthExpenses(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        // بنستخدم YearMonth عشان يعرف الشهر ده كام يوم
        LocalDateTime endOfMonth = YearMonth.from(today).atEndOfMonth().atTime(LocalTime.MAX);

        // بنجيب المصاريف من الداتا بيز
        List<Expense> monthExpenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth);

        // بنجمع المصاريف
        BigDecimal total = BigDecimal.ZERO;
        for (Expense exp : monthExpenses) {
            total = total.add(exp.getAmount());
        }

        return total;
    }


}
