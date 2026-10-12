package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExpenseClaimForm {

    private Long id;
    private String claimNumber;
    private String claimType = "项目费用";
    private Long claimantEmployeeId;
    private Long reimbursementCompanyId;
    private String currency;
    private Long sourceDocumentId;
    private boolean submitForApproval;
    private List<LineRow> lines = new ArrayList<>();

    @Getter
    @Setter
    public static class LineRow {
        private Long id;
        private boolean removed;
        private Integer lineNumber;
        private Long costOccurrenceId;
        private BigDecimal claimedReimbursementAmount;
        private String rejectionReason;
    }
}
