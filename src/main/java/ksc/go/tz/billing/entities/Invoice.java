package ksc.go.tz.billing.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.common.LineItem;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"contract", "quote", "site", "items"})
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Invoice extends BaseEntity<UUID> {

    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;

    // Set once a contract is created from the accepted quote.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Sites site;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "amount_due", precision = 19, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", precision = 19, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDate billingPeriodEnd;

    @Column
    private String status;

    @Column(name = "pdf_local_url")
    private String pdfLocalUrl;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "invoice_items", joinColumns = @JoinColumn(name = "invoice_id"))
    @OrderColumn(name = "line_no")
    private List<LineItem> items = new ArrayList<>();
}
