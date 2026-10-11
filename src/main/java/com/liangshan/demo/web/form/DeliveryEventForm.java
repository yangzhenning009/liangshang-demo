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
public class DeliveryEventForm {

    private Long projectId;
    private String deliveryNumber;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime submittedAt;
    private String deliverableDescription;
    private Long submitterCompanyId;
    private Long submitterLegalPersonId;
    private Long submitterPersonId;
    private Long submittedByEmployeeId;
    private Long resubmissionOfDeliveryEventId;
    private Long sourceDocumentId;
    private List<Line> lines = new ArrayList<>();

    @Getter
    @Setter
    public static class Line {
        private Long stageDeliverableId;
        private BigDecimal submittedQuantity;
        private String unit;
        private String deliveredArtifactVersion;
        private Long deliverableDocumentId;
        private boolean removed;
    }
}
