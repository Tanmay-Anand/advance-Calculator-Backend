package com.tanmay.advance_Calculator_Backend.controller;

import com.tanmay.advance_Calculator_Backend.domain.Calculation;
import com.tanmay.advance_Calculator_Backend.dto.CalculationRequest;
import com.tanmay.advance_Calculator_Backend.dto.CalculationResponse;
import com.tanmay.advance_Calculator_Backend.service.CalculatorService;
import com.tanmay.advance_Calculator_Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
public class CalculatorController {

    private final CalculatorService calculatorService;
    private final UserService userService;

//    Body: { "expression": "2 + 3 * 5" }
//    Returns: { "result": "17", "expression": "2 + 3 * 5" }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody CalculationRequest request) {
        try {
            // Get current authenticated user
            Long userId = userService.getCurrentUser().getId();

            // Evaluate the expression
            String result = calculatorService.evaluate(request.getExpression());

            // Save to history
            Calculation calc = calculatorService.saveToHistory(
                    userId,
                    request.getExpression(),
                    result
            );

            // Return response
            CalculationResponse response = new CalculationResponse(
                    calc.getId(),
                    calc.getExpression(),
                    calc.getResult(),
                    calc.getTimestamp()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid expression: " + e.getMessage());
        }
    }

    /**
     * Get calculation history for current user
     * GET /api/calculator/history
     * Returns: List of calculations
     */
    @GetMapping("/history")
    public ResponseEntity<List<Calculation>> getHistory() {
        Long userId = userService.getCurrentUser().getId();
        List<Calculation> history = calculatorService.getHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get archived calculations for current user
     * GET /api/calculator/archive
     * Returns: List of archived calculations
     */
    @GetMapping("/archive")
    public ResponseEntity<List<Calculation>> getArchive() {
        Long userId = userService.getCurrentUser().getId();
        List<Calculation> archive = calculatorService.getArchive(userId);
        return ResponseEntity.ok(archive);
    }

    /**
     * Save a calculation to archive
     * POST /api/calculator/archive
     * Body: { "expression": "2 + 3", "result": "5" }
     */
    @PostMapping("/archive")
    public ResponseEntity<?> saveToArchive(@RequestBody CalculationRequest request) {
        try {
            Long userId = userService.getCurrentUser().getId();
            Calculation calc = calculatorService.saveToArchive(
                    userId,
                    request.getExpression(),
                    request.getResult()
            );
            return ResponseEntity.ok(calc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete an archived calculation
     * DELETE /api/calculator/archive/{id}
     */
    @DeleteMapping("/archive/{id}")
    public ResponseEntity<?> deleteFromArchive(@PathVariable Long id) {
        try {
            Long userId = userService.getCurrentUser().getId();
            calculatorService.deleteFromArchive(id, userId);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Clear all history for current user
     * DELETE /api/calculator/history
     */
    @DeleteMapping("/history")
    public ResponseEntity<?> clearHistory() {
        Long userId = userService.getCurrentUser().getId();
        calculatorService.clearHistory(userId);
        return ResponseEntity.ok("History cleared");
    }
}
