package com.mohamed.expensetrackerbot.handler;


import com.mohamed.expensetrackerbot.expense.model.Expense;
import com.mohamed.expensetrackerbot.expense.service.ExpenseService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageHandler {

    private final ExpenseService expenseService;

    // Constructor Injection
    public MessageHandler(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Main method to decide what to do
    public String handleUserMessage(Long chatId, String messageText) {
        if (messageText.equals("/start")) {
            return "أهلاً بيك يا صاحبي! ابعتلي مصروفك بالشكل ده: \nمثال: 50 Food";
        } else if (messageText.equals("/today")) {
            return expenseService.getTodayReport(chatId);
        } else if (messageText.equals("/month")) {
            return expenseService.getMonthReport(chatId);
        }

        // If it's not a command, treat it as a new expense
        return processExpense(chatId, messageText);
    }

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