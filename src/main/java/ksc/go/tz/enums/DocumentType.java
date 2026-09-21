package ksc.go.tz.enums;

public enum DocumentType {
    CERTIFICATE_OF_INCORPORATION("Certificate of Incorporation"),
    BUSINESS_LICENSE("Business License"),
    TAX_IDENTIFICATION_CERTIFICATE("Tax Identification Certificate"),
    VAT_REGISTRATION_CERTIFICATE("VAT Registration Certificate"),
    MEMORANDUM_AND_ARTICLES_OF_ASSOCIATION("Memorandum and Articles of Association"),
    POWER_OF_ATTORNEY("Power of Attorney"),
    PROOF_OF_ADDRESS("Proof of Address"),
    BANK_REFERENCE_LETTER("Bank Reference Letter"),
    NATIONAL_ID_OR_PASSPORT("National ID or Passport"),
    COMPANY_LOGO("Company Logo"),
    DRIVER_PHOTO("Driver Photo"),
    VEHICLE_PHOTO("Vehicle Photo"),
    DRIVER_LICENSE("Driver License"),
    VEHICLE_REGISTRATION_DOCUMENT("Vehicle Registration Document"),
    VEHICLE_INSPECTION_CERTIFICATE("Vehicle Inspection Certificate"),
    INSURANCE_CERTIFICATE("Insurance Certificate"),
    SAFETY_COMPLIANCE_CERTIFICATE("Safety Compliance Certificate"),
    ENVIRONMENTAL_COMPLIANCE_CERTIFICATE("Environmental Compliance Certificate"),
    REGULATORY_CERTIFICATE("Regulatory Certificate");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
