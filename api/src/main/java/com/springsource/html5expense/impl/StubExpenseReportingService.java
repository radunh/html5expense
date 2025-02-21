/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.LocalDate;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.ExpenseReportingService;

public class StubExpenseReportingService implements ExpenseReportingService {

    private final Map<Long, EligibleCharge> eligibleCharges = new HashMap<Long, EligibleCharge>();

    private final Map<Long, ExpenseReportEntity> reports = new HashMap<Long, ExpenseReportEntity>();

    private final AtomicLong reportSequence = new AtomicLong();

    private final AtomicInteger expenseSequence = new AtomicInteger();

    public StubExpenseReportingService() {
        eligibleCharges.put(1L, new EligibleCharge(1L, new LocalDate(2011, 7, 31), "Delta", "Air Travel", new BigDecimal("431.00")));
        eligibleCharges.put(2L, new EligibleCharge(2L, new LocalDate(2011, 8, 22), "Hilton", "Lodging", new BigDecimal("639.00")));
        eligibleCharges.put(3L, new EligibleCharge(3L, new LocalDate(2011, 8, 22), "Chipotle", "Meals", new BigDecimal("24.00")));
        eligibleCharges.put(4L, new EligibleCharge(4L, new LocalDate(2011, 9, 04), "United", "Air Travel", new BigDecimal("537.00")));
        eligibleCharges.put(5L, new EligibleCharge(5L, new LocalDate(2011, 9, 15), "Apple Store", "Computer Supplies", new BigDecimal("49.00")));
        eligibleCharges.put(6L, new EligibleCharge(6L, new LocalDate(2011, 9, 16), "Peet's Coffee", "Meals", new BigDecimal("7.99")));
    }

    public Long createReport(String purpose) {
        ExpenseReportEntity report = new ExpenseReportEntity(reportSequence.incrementAndGet(), purpose);
        reports.put(report.getId(), report);
        return report.getId();
    }

    public Collection<EligibleCharge> getEligibleCharges() {
        return Collections.unmodifiableCollection(eligibleCharges.values());
    }

    public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReportEntity report = getReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();
        for (Long chargeId : chargeIds) {
            EligibleCharge charge = eligibleCharges.get(chargeId);
            ExpenseEntity expense = report.createExpense(charge);
            expense.id = expenseSequence.incrementAndGet();
            expenses.add(expense.data());
            eligibleCharges.remove(chargeId);
        }
        return expenses;
    }

    public String attachReceipt(Long reportId, Integer expenseId, byte[] receiptBytes) {
        ExpenseReportEntity report = getReport(reportId);
        String receipt = receipt(receiptBytes);
        report.attachReceipt(expenseId, receipt);
        return receipt;
    }

    public void submitReport(Long reportId) {
        getReport(reportId).markInReview();
    }

    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReport> openReports = new ArrayList<ExpenseReport>();
        for (ExpenseReportEntity report : reports.values()) {
            if (report.isOpen()) {
                openReports.add(report.data());
            }
        }
        return openReports;
    }

    // expense review only (not currently part of reporting interface)

    public void reject(Long reportId, List<Flag> flags) {
        getReport(reportId).markRejected(flags);
    }

    public void approve(Long reportId) {
        getReport(reportId).markApproved();
    }

    // helpers

    private ExpenseReportEntity getReport(Long reportId) {
        return reports.get(reportId);
    }

    private String receipt(byte[] receiptBytes) {
        return "receiptReference";
    }

}