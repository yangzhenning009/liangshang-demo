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
public class PaymentForm {

    private Long id;
    private Long payerCompanyId;
    private Long payerLegalPersonId;
    private Long payerPersonId;
    private Long payeeCompanyId;
    private Long payeeLegalPersonId;
    private Long payeePersonId;
    private Long paymentForCompanyId;
    private Long paymentForLegalPersonId;
    private Long paymentForPersonId;
    private Long receiptForCompanyId;
    private Long receiptForLegalPersonId;
    private Long receiptForPersonId;
    private Long agencyBasisDocumentId;
    private String paymentNumber;
    private String paymentKind = "普通收付";
    private String paymentPurpose = "合同款";
    private Long relatedEmployeeId;
    private Long refundOfPaymentId;
    private Long payerCompanyBankAccountId;
    private Long payeeCompanyBankAccountId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime paidAt;
    private BigDecimal paymentAmount;
    private String currency;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bankValueDate;
    private BigDecimal bankFeeAmount;
    private String paymentChannel = "银行转账";
    private String payerBankAccountNumberSnapshot;
    private String payerBankAccountNameSnapshot;
    private String payerBankNameSnapshot;
    private String payeeBankAccountNumberSnapshot;
    private String payeeBankAccountNameSnapshot;
    private String payeeBankNameSnapshot;
    private String bankTransactionNumber;
    private String paymentPlatformTransactionNumber;
    private Long confirmedByEmployeeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime confirmedAt;
    private Long sourceDocumentId;
    private String status = "草稿";
    private List<AllocationRow> allocations = new ArrayList<>();

    @Getter
    @Setter
    public static class AllocationRow {
        private Long id;
        private boolean removed;
        private Long receivableItemId;
        private Long payableItemId;
        private BigDecimal settlementAmount;
        private String currency;
        private String settlementAction = "apply";
        private Long reversesAllocationId;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime allocatedAt;
        private Long allocatedByEmployeeId;
        private Long settlementBasisDocumentId;
        private String status = "已确认";
    }
}
