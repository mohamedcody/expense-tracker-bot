package com.mohamed.expensetrackerbot.handler;


import com.mohamed.expensetrackerbot.budget.BudgetService;
import com.mohamed.expensetrackerbot.expense.model.Expense;
import com.mohamed.expensetrackerbot.expense.service.ExpenseService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageHandler {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    // 🔥 هنا بنعمل Map عشان البوت يفتكر حالة كل يوزر
    private final Map<Long, String> userStates = new HashMap<>();

    public MessageHandler(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    public String handleUserMessage(Long chatId, String messageText) {

        // 1. لو اليوزر داس على أمر الميزانية
        if (messageText.equals("/budget")) {
            userStates.put(chatId, "WAITING_FOR_BUDGET");
            return "💰 الميزانية الشهرية\n\nاكتب المبلغ بس، مثال:\n10000";
        }

        // 🔥 التعديل السحري: لو اليوزر داس على أمر تاني (زي /today) وهو كان بيعمل ميزانية
        // نمسح حالة الميزانية دي فوراً عشان نفك الحصار!
        if (messageText.startsWith("/") && !messageText.equals("/budget")) {
            userStates.remove(chatId);
        }

        // 2. لو البوت فاكر إنك بتدخل ميزانية (لما تبعت الرقم)
        if ("WAITING_FOR_BUDGET".equals(userStates.get(chatId))) {
            try {
                BigDecimal amount = new BigDecimal(messageText.trim());
                String response = budgetService.setBudget(chatId, amount); //[cite: 1]

                // نمسح الحالة بعد ما نخلص عشان يرجع يسجل مصاريف عادي
                userStates.remove(chatId);
                return response;
            } catch (NumberFormatException e) {
                // لو بعت كلام بدل الأرقام، مش هنمسح الحالة وهنخليه يحاول تاني
                return "❌ لازم تكتب رقم صحيح للميزانية، جرب تاني.";
            }
        }

        // 3. باقي الأوامر العادية
        if (messageText.equals("/start")) {
            return "أهلاً بيك يا صاحبي! ابعتلي مصروفك بالشكل ده: \nمثال: 50 Food";
        } else if (messageText.equals("/today")) {
            return expenseService.getTodayReport(chatId); //[cite: 8, 9]
        } else if (messageText.equals("/month")) {
            // ده التعديل بتاع الشهر اللي عملناه الخطوة اللي فاتت
            BigDecimal totalSpent = expenseService.getTotalMonthExpenses(chatId);
            return budgetService.getBudgetStatus(chatId, totalSpent); //[cite: 1]
        }

        // 4. لو مفيش أي حاجة من اللي فوق، يبقى ده مصروف جديد
        return processExpense(chatId, messageText); //[cite: 9]
    }



    // git Hub
    // Extracted Regex logic for clean code
    private String processExpense(Long chatId, String messageText) {
        try {
            Matcher matcher = Pattern.compile("\\d+(\\.\\d+)?").matcher(messageText);

            if (matcher.find()) {
                String amountStr = matcher.group();
                BigDecimal amount = new BigDecimal(amountStr);
                String category = messageText.replace(amountStr, "").trim();

                if (category.isEmpty()) {
                    category = "General";
                }

                Expense expense = new Expense();
                expense.setUserId(chatId);
                expense.setAmount(amount);
                expense.setCategory(category);
                expense.setDescription(messageText);

                expenseService.addExpense(expense);
                return "عاش! تم تسجيل مصروف: " + amount + " في قسم [" + category + "] ✅";
            } else {
                return "مش شايف أي أرقام في رسالتك! جرب تكتب مثلاً: 50 مواصلات";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "حصلت مشكلة في تسجيل المصروف، جرب تاني!";
        }
    }
}