package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ContractDetailForm {

    private List<LineForm> lines = new ArrayList<>();
    private List<StageForm> stages = new ArrayList<>();
    private List<DeliverableForm> deliverables = new ArrayList<>();
    private List<SignatoryForm> signatories = new ArrayList<>();
    private List<LinkForm> links = new ArrayList<>();

    @Getter
    @Setter
    public static class LineForm {
        private Long id;
        private Integer lineNumber;
        private String itemName;
        private String itemSpecification;
        private String unit;
        private BigDecimal contractedQuantity;
        private BigDecimal unitNetPrice;
        private BigDecimal taxRate;
        private BigDecimal lineNetAmount;
        private BigDecimal lineTaxAmount;
        private BigDecimal lineGrossAmount;
        private boolean removed;
    }

    @Getter
    @Setter
    public static class StageForm {
        private Long id;
        private String stageCode;
        private Integer sequenceNo;
        private String stageName;
        private Long responsibleEmployeeId;
        private String status = "未开始";
        private String deliverableDescription;
        private String acceptanceCriteria;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate plannedStartAt;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate plannedDueAt;
        private BigDecimal stageNetAmount;
        private BigDecimal stageTaxAmount;
        private BigDecimal stageGrossAmount;
        private String currency;
        private boolean removed;
    }

    @Getter
    @Setter
    public static class DeliverableForm {
        private Long id;
        private Long stageId;
        private String stageCode;
        private Long contractLineTermVersionId;
        private Integer lineNumber;
        private String deliverableCode;
        private String deliverableName;
        private String deliverableSpecification;
        private String unit;
        private BigDecimal plannedQuantity;
        private String acceptanceCriteria;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate plannedDeliveryDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate plannedAcceptanceDate;
        private boolean removed;
    }

    @Getter
    @Setter
    public static class SignatoryForm {
        private Long id;
        private Long signatoryCompanyId;
        private Long signatoryLegalPersonId;
        private Long signatoryPersonId;
        private String signatoryRole = "其他";
        private String responsibilityDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
        private boolean removed;
    }

    @Getter
    @Setter
    public static class LinkForm {
        private Long id;
        private Long projectId;
        private String relationshipNote;
        private boolean removed;
    }
}
