package ksc.go.tz.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.billing.entities.Invoice;
import ksc.go.tz.common.LineItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Invoice details")
public class InvoiceResponseDto {

    @Schema(description = "Invoice ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String invoiceId;

    @Schema(description = "Invoice number", example = "INV-20261002-7K3Q9A")
    private String invoiceNumber;

    @Schema(description = "Quote this invoice was generated from")
    private String quoteId;

    @Schema(description = "Site being invoiced")
    private String siteId;

    @Schema(description = "Contract, once one is created from the accepted quote; otherwise null")
    private String contractId;

    @Schema(description = "Line items")
    private List<LineItem> items;

    @Schema(description = "Total amount due in TZS", example = "140000.00")
    private BigDecimal amountDue;

    @Schema(description = "Total received so far in TZS", example = "0.00")
    private BigDecimal amountPaid;

    @Schema(description = "amountDue − amountPaid, in TZS", example = "140000.00")
    private BigDecimal balanceDue;

    @Schema(description = "Date the invoice was issued", example = "2026-10-02")
    private LocalDate issueDate;

    @Schema(description = "Payment due date", example = "2026-10-16")
    private LocalDate dueDate;

    @Schema(description = "PENDING, PARTIALLY_PAID, PAID or CANCELLED", example = "PENDING")
    private String status;

    public InvoiceResponseDto(Invoice invoice) {
        this.invoiceId = invoice.getId().toString();
        this.invoiceNumber = invoice.getInvoiceNumber();
        this.quoteId = invoice.getQuote() != null ? invoice.getQuote().getId().toString() : null;
        this.siteId = invoice.getSite() != null ? invoice.getSite().getId().toString() : null;
        this.contractId = invoice.getContract() != null ? invoice.getContract().getId().toString() : null;
        this.items = List.copyOf(invoice.getItems());
        this.amountDue = invoice.getAmountDue();
        this.amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        this.balanceDue = this.amountDue != null ? this.amountDue.subtract(this.amountPaid) : null;
        this.issueDate = invoice.getIssueDate();
        this.dueDate = invoice.getDueDate();
        this.status = invoice.getStatus();
    }
}
