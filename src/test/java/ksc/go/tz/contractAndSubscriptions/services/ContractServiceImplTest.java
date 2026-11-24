package ksc.go.tz.contractAndSubscriptions.services;

import ksc.go.tz.billing.repository.InvoiceRepository;
import ksc.go.tz.billing.repository.PaymentRepository;
import ksc.go.tz.contractAndSubscriptions.repository.SubscriptionRepository;
import afriUtils.responses.AfriException;
import ksc.go.tz.billing.services.InvoiceService;
import ksc.go.tz.contractAndSubscriptions.dto.ContractDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractReasonDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.contractAndSubscriptions.dto.ContractSignDto;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.repository.ContractRepository;
import ksc.go.tz.enums.ContractStatus;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.QuoteStatus;
import ksc.go.tz.enums.SignatureStatus;
import ksc.go.tz.quotation.entities.Quote;
import ksc.go.tz.quotation.repository.QuoteRepository;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractServiceImplTest {

    private final UUID user = UUID.randomUUID();
    private ContractRepository contractRepository;
    private QuoteRepository quoteRepository;
    private InvoiceService invoiceService;
    private ContractServiceImpl service;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        quoteRepository = mock(QuoteRepository.class);
        invoiceService = mock(InvoiceService.class);
        service = new ContractServiceImpl(contractRepository, quoteRepository, invoiceService, mock(ContractPdfRenderer.class),
                mock(InvoiceRepository.class), mock(SubscriptionRepository.class), mock(PaymentRepository.class));
        when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> {
            Contract c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
            return c;
        });
    }

    @Test
    void createsDraftFromAcceptedQuoteWithDefaultsAndLinksInvoice() {
        Quote quote = quote(QuoteStatus.ACCEPTED);
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));

        ContractResponseDto created = service.addContract(dto(quote, LocalDate.now().plusDays(5), LocalDate.now().plusYears(1)), user);

        assertEquals("DRAFT", created.getStatus());
        assertEquals("PENDING", created.getSignatureStatus());
        assertTrue(created.getContractNumber().startsWith("CT-"));
        assertEquals("FUMIGATION", created.getServiceLine());
        assertEquals(0, new BigDecimal("170000.00").compareTo(created.getContractValue()));
        assertEquals("MONTHLY", created.getFrequency());
        assertEquals(quote.getSite().getSite_owner(), created.getClientId());
        verify(invoiceService).attachContract(eq(quote), any(Contract.class));
    }

    @Test
    void rejectsQuoteThatIsNotAccepted() {
        Quote quote = quote(QuoteStatus.DRAFT);
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));

        AfriException e = assertThrows(AfriException.class,
                () -> service.addContract(dto(quote, LocalDate.now(), LocalDate.now().plusDays(30)), user));
        assertTrue(e.getMessage().contains("only ACCEPTED quotations"));
        verify(contractRepository, never()).save(any());
    }

    @Test
    void rejectsSecondContractForSameQuote() {
        Quote quote = quote(QuoteStatus.ACCEPTED);
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));
        when(contractRepository.existsByQuoteId(quote.getId())).thenReturn(true);

        assertThrows(AfriException.class, () -> service.addContract(dto(quote, LocalDate.now(), LocalDate.now().plusDays(30)), user));
    }

    @Test
    void rejectsEndBeforeStart() {
        Quote quote = quote(QuoteStatus.ACCEPTED);
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));

        AfriException e = assertThrows(AfriException.class,
                () -> service.addContract(dto(quote, LocalDate.now().plusDays(10), LocalDate.now()), user));
        assertTrue(e.getMessage().contains("End date must be on or after the start date"));
    }

    @Test
    void sendThenSignBeforeStartDateIsSigned() {
        Contract contract = existing(ContractStatus.DRAFT, LocalDate.now().plusDays(10), LocalDate.now().plusYears(1));

        service.sendContract(contract.getId().toString(), user);
        assertEquals(ContractStatus.SENT, contract.getStatus());
        assertNotNull(contract.getSentAt());

        service.approveContract(contract.getId().toString(), new ContractSignDto("Jane Doe", null), user);
        assertEquals(ContractStatus.SIGNED, contract.getStatus());
        assertEquals(SignatureStatus.SIGNED, contract.getSignatureStatus());
        assertEquals("Jane Doe", contract.getSignedBy());
    }

    @Test
    void signingOnOrAfterStartDateActivatesImmediately() {
        Contract contract = existing(ContractStatus.SENT, LocalDate.now(), LocalDate.now().plusMonths(6));

        service.approveContract(contract.getId().toString(), new ContractSignDto("Jane Doe", null), user);

        assertEquals(ContractStatus.ACTIVE, contract.getStatus());
    }

    @Test
    void invalidTransitionsAreRejected() {
        Contract draft = existing(ContractStatus.DRAFT, LocalDate.now(), LocalDate.now().plusDays(30));
        assertThrows(AfriException.class,
                () -> service.approveContract(draft.getId().toString(), new ContractSignDto("Jane", null), user));
        assertThrows(AfriException.class,
                () -> service.terminateContract(draft.getId().toString(), new ContractReasonDto("x"), user));

        Contract active = existing(ContractStatus.ACTIVE, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        assertThrows(AfriException.class, () -> service.updateContract(active.getId().toString(), new ContractDto(), user));
        assertThrows(AfriException.class, () -> service.deleteContract(active.getId().toString(), user));
    }

    @Test
    void rejectAndTerminateRecordReasons() {
        Contract sent = existing(ContractStatus.SENT, LocalDate.now(), LocalDate.now().plusDays(30));
        service.rejectContract(sent.getId().toString(), new ContractReasonDto(" Price too high "), user);
        assertEquals(ContractStatus.REJECTED, sent.getStatus());
        assertEquals(SignatureStatus.REJECTED, sent.getSignatureStatus());
        assertEquals("Price too high", sent.getRejectionReason());

        Contract active = existing(ContractStatus.ACTIVE, LocalDate.now().minusDays(10), LocalDate.now().plusDays(30));
        service.terminateContract(active.getId().toString(), new ContractReasonDto("Client moved"), user);
        assertEquals(ContractStatus.TERMINATED, active.getStatus());
        assertNotNull(active.getTerminatedAt());
    }

    @Test
    void dailyTransitionsActivateStartedAndExpireEndedContracts() {
        LocalDate today = LocalDate.now();
        Contract starting = contract(ContractStatus.SIGNED, today, today.plusMonths(3));
        Contract alreadyOver = contract(ContractStatus.SIGNED, today.minusMonths(2), today.minusDays(1));
        Contract ended = contract(ContractStatus.ACTIVE, today.minusYears(1), today.minusDays(1));
        when(contractRepository.findByStatusAndStartDateLessThanEqual(ContractStatus.SIGNED, today)).thenReturn(List.of(starting, alreadyOver));
        when(contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, today)).thenReturn(List.of(ended));

        assertEquals(3, service.applyDateTransitions());
        assertEquals(ContractStatus.ACTIVE, starting.getStatus());
        assertEquals(ContractStatus.EXPIRED, alreadyOver.getStatus());
        assertEquals(ContractStatus.EXPIRED, ended.getStatus());
    }

    @Test
    void paginationRejectsUnknownStatus() {
        assertThrows(AfriException.class,
                () -> service.getAllContractsWithPaginationAndSortingAndFiltering(0, 10, "createdAt", "desc", "NOPE", null, null));
    }

    // ------------------------------------------------------------------ fixtures

    private Contract existing(ContractStatus status, LocalDate start, LocalDate end) {
        Contract contract = contract(status, start, end);
        when(contractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));
        return contract;
    }

    private Contract contract(ContractStatus status, LocalDate start, LocalDate end) {
        Contract contract = new Contract();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("CT-TEST-" + status);
        contract.setQuote(quote(QuoteStatus.ACCEPTED));
        contract.setClientId("client-1");
        contract.setStatus(status);
        contract.setSignatureStatus(SignatureStatus.PENDING);
        contract.setStartDate(start);
        contract.setEndDate(end);
        contract.setCreatedBy(user);
        return contract;
    }

    private static Quote quote(QuoteStatus status) {
        Sites site = new Sites();
        site.setId(UUID.randomUUID());
        site.setSite_owner("owner-" + site.getId());
        site.setPlotCoordinates(Map.of("latitude", -6.7, "longitude", 39.2));
        Quote quote = new Quote();
        quote.setId(UUID.randomUUID());
        quote.setQuoteNumber("QT-TEST");
        quote.setSite(site);
        quote.setStatus(status);
        quote.setServiceType(LeadServiceType.FUMIGATION);
        quote.setFrequency(Frequency.MONTHLY);
        quote.setPriceMin(new BigDecimal("170000.00"));
        quote.setPriceMax(new BigDecimal("170000.00"));
        return quote;
    }

    private static ContractDto dto(Quote quote, LocalDate start, LocalDate end) {
        ContractDto dto = new ContractDto();
        dto.setQuoteId(quote.getId().toString());
        dto.setStartDate(start);
        dto.setEndDate(end);
        return dto;
    }
}
