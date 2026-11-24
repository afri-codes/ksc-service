package ksc.go.tz.common.pdf;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The company shown on generated documents (quotations, invoices, contracts), from {@code ksc.company.*}.
 */
@Getter
@Component
public class CompanyDetails {

    @Value("${ksc.company.name:KSC Limited}")
    private String name;

    @Value("${ksc.company.tagline:Cleaning, Fumigation & Property Management}")
    private String tagline;

    @Value("${ksc.company.email:ksc@gmail.com}")
    private String email;

    @Value("${ksc.company.phone:}")
    private String phone;

    @Value("${ksc.company.address:Dar es Salaam, Tanzania}")
    private String address;

    public CompanyDetails() {
    }

    /** For tests and tools that build documents outside Spring. */
    public CompanyDetails(String name, String tagline, String email, String phone, String address) {
        this.name = name;
        this.tagline = tagline;
        this.email = email;
        this.phone = phone == null ? "" : phone;
        this.address = address;
    }

    public String contactLine() {
        return email + (phone == null || phone.isBlank() ? "" : " · " + phone);
    }

    public List<String> headerLines() {
        return List.of(tagline, address, contactLine());
    }

    public String footerLine() {
        return name + " · " + contactLine();
    }
}
