package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AcceptanceEventForm {

    private Long deliveryEventId;
    private String acceptanceNumber;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime acceptedAt;
    private Long acceptorCompanyId;
    private Long acceptorLegalPersonId;
    private Long acceptorPersonId;
    private Long acceptedByEmployeeId;
    private Long acceptedByPersonId;
    private String acceptanceComments;
    private Long sourceDocumentId;
    private Long supersedesAcceptanceEventId;
    private List<Line> lines = new ArrayList<>();

    @Getter
    @Setter
    public static class Line {
        private Long deliveryLineId;
        private BigDecimal reviewedQuantity;
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private String rejectionReason;
        private boolean removed;
    }
}
