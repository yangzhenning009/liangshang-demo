package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JournalForm {

    private Long id;
    private Long ledgerId;
    private Long accountingPeriodId;
    private String voucherNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate voucherDate;
    private String entryDescription;
    private Long preparedByEmployeeId;
    private Long reviewedByEmployeeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reviewedAt;
    private String adjustmentReason;
    private Long reversalOfEntryId;
    private String status = "草稿";
    private List<LineRow> lines = new ArrayList<>();
    private List<SourceRow> sources = new ArrayList<>();

    @Getter
    @Setter
    public static class LineRow {
        private Long id;
        private boolean removed;
        private Integer lineNumber;
        private Long accountingAccountId;
        private String lineDescription;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private String currency;
        private Long counterpartyCompanyId;
        private Long counterpartyLegalPersonId;
        private Long counterpartyPersonId;
        private Long employeeId;
        private Long departmentId;
        private Long projectId;
        private Long contractStageId;
        private Long sourceDocumentId;
    }

    @Getter
    @Setter
    public static class SourceRow {
        private Long id;
        private boolean removed;
        private Integer lineNumber;
        private Long invoiceLineId;
        private Long paymentId;
        private Long receivableItemId;
        private Long payableItemId;
        private Long receivableAdjustmentId;
        private Long payableAdjustmentId;
        private Long costAllocationId;
        private Long costOccurrenceId;
        private Long revenueRecognitionId;
        private BigDecimal sourceContributionAmount;
        private String currency;
        private Long sourceDocumentId;
    }
}
