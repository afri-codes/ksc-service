package ksc.go.tz.enums;

/**
 * Contract lifecycle:
 * <pre>
 * DRAFT ──send──► SENT ──sign──► SIGNED ──start date──► ACTIVE ──end date──► EXPIRED
 *                   └──reject──► REJECTED      SIGNED / ACTIVE ──terminate──► TERMINATED
 * </pre>
 */
public enum ContractStatus {
    /** Being prepared; editable and deletable. */
    DRAFT,
    /** Sent to the client for signature. */
    SENT,
    /** Signed by the client; becomes ACTIVE on its start date. */
    SIGNED,
    /** In force: between start and end date. */
    ACTIVE,
    /** Ended naturally after its end date. */
    EXPIRED,
    /** Ended early by either party. */
    TERMINATED,
    /** The client declined to sign. */
    REJECTED
}
