package ksc.go.tz.job.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A staff member belonging to a crew. Jobs are assigned to crews, so this is how a staff member's
 * jobs are known. One live membership per (crew, staff) pair is enforced in CrewMemberService.
 */
@Entity
@Table(name = "crew_members")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "crew")
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class CrewMember extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    /** The staff member's user ID (same value as time_logs.staff_id). */
    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Column(name = "staff_name")
    private String staffName;

    /** Role in the crew, e.g. Cleaner, Fumigator, Team lead. */
    @Column(name = "member_role")
    private String memberRole;

    @Column(name = "joined_on")
    private LocalDate joinedOn;
}
