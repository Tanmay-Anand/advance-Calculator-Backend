package com.tanmay.advance_Calculator_Backend.service;

import com.tanmay.advance_Calculator_Backend.domain.Calculation;
import com.tanmay.advance_Calculator_Backend.repository.CalculationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Stack;

/**
 * Service layer for calculator operations
 * Handles business logic and expression evaluation
 */
@Service
@RequiredArgsConstructor
public class CalculatorService {

    private final CalculationRepository calculationRepository;

    /**
     * Evaluate a mathematical expression using custom parser
     * Supports: +, -, *, /, (, ), decimal numbers
     *
     * @param expression - the math expression to evaluate
     * @return result as string
     */
    public String evaluate(String expression) throws Exception {
        // Clean the expression
        expression = expression.trim()
                .replace(" ", "")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", String.valueOf(Math.PI))
                .replace("e", String.valueOf(Math.E));

        if (expression.isEmpty()) {
            throw new Exception("Empty expression");
        }

        // Evaluate the expression
        double result = evaluateExpression(expression);

        // Format result - if it's a whole number, return without decimal
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.valueOf((long) result);
        }

        return String.valueOf(result);
    }

    /**
     * Evaluate mathematical expression using two-stack algorithm
     */
    private double evaluateExpression(String expression) throws Exception {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // Skip whitespace
            if (c == ' ') {
                continue;
            }

            // If current character is a number or decimal point
            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();

                // Read the complete number (including decimals)
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                i--; // Adjust index

                numbers.push(Double.parseDouble(num.toString()));
            }
            // If current character is opening bracket
            else if (c == '(') {
                operators.push(c);
            }
            // If current character is closing bracket
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                if (!operators.isEmpty()) {
                    operators.pop(); // Remove '('
                }
            }
            // If current character is an operator
            else if (c == '+' || c == '-' || c == '*' || c == '/') {
                // Handle negative numbers at the start or after operators
                if (c == '-' && (i == 0 || expression.charAt(i - 1) == '(' ||
                        expression.charAt(i - 1) == '+' || expression.charAt(i - 1) == '-' ||
                        expression.charAt(i - 1) == '*' || expression.charAt(i - 1) == '/')) {

                    StringBuilder num = new StringBuilder("-");
                    i++;

                    while (i < expression.length() &&
                            (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                        num.append(expression.charAt(i));
                        i++;
                    }
                    i--;

                    numbers.push(Double.parseDouble(num.toString()));
                } else {
                    // Process operators with higher or equal precedence
                    while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                        numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                    }
                    operators.push(c);
                }
            } else {
                throw new Exception("Invalid character: " + c);
            }
        }

        // Process remaining operators
        while (!operators.isEmpty()) {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }

        if (numbers.isEmpty()) {
            throw new Exception("Invalid expression");
        }

        return numbers.pop();
    }

    /**
     * Check if operator1 has higher or equal precedence than operator2
     */
    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    /**
     * Apply an operator to two operands
     */
    private double applyOperation(char operator, double b, double a) throws Exception {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new Exception("Cannot divide by zero");
                }
                return a / b;
            default:
                throw new Exception("Invalid operator: " + operator);
        }
    }

    /**
     * Save a calculation to history
     */
    @Transactional
    public Calculation saveToHistory(Long userId, String expression, String result) {
        Calculation calc = new Calculation();
        calc.setUserId(userId);
        calc.setExpression(expression);
        calc.setResult(result);
        calc.setTimestamp(LocalDateTime.now());
        calc.setType(Calculation.CalculationType.HISTORY);

        return calculationRepository.save(calc);
    }

    /**
     * Save a calculation to archive
     */
    @Transactional
    public Calculation saveToArchive(Long userId, String expression, String result) {
        Calculation calc = new Calculation();
        calc.setUserId(userId);
        calc.setExpression(expression);
        calc.setResult(result);
        calc.setTimestamp(LocalDateTime.now());
        calc.setType(Calculation.CalculationType.ARCHIVE);

        return calculationRepository.save(calc);
    }

    /**
     * Get calculation history for a user
     */
    public List<Calculation> getHistory(Long userId) {
        return calculationRepository.findByUserIdAndTypeOrderByTimestampDesc(
                userId,
                Calculation.CalculationType.HISTORY
        );
    }

    /**
     * Get archived calculations for a user
     */
    public List<Calculation> getArchive(Long userId) {
        return calculationRepository.findByUserIdAndTypeOrderByTimestampDesc(
                userId,
                Calculation.CalculationType.ARCHIVE
        );
    }

    /**
     * Delete from archive (with security check)
     */
    @Transactional
    public void deleteFromArchive(Long id, Long userId) throws Exception {
        Calculation calc = calculationRepository.findByIdAndUserId(id, userId);

        if (calc == null) {
            throw new Exception("Calculation not found or access denied");
        }

        if (calc.getType() != Calculation.CalculationType.ARCHIVE) {
            throw new Exception("Can only delete from archive");
        }

        calculationRepository.delete(calc);
    }

    /**
     * Clear all history for a user
     */
    @Transactional
    public void clearHistory(Long userId) {
        calculationRepository.deleteByUserIdAndType(
                userId,
                Calculation.CalculationType.HISTORY
        );
    }
}