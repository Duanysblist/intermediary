package com.dduany.intermediary.recurringplan;

import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.ReferenceEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A routine: "Workout B every Monday, Wednesday and Friday". Generation turns it into ordinary
 * plan items (one per matching day), which then live their own life on the board.
 */
@Entity
@Table(name = "recurring_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "intent", nullable = false)
    private PlanIntent intent;

    @Convert(converter = DayOfWeekListConverter.class)
    @Column(name = "days", nullable = false)
    private List<DayOfWeek> days;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_entity_type")
    private ReferenceEntityType referenceEntityType;

    @Column(name = "reference_entity_id")
    private Long referenceEntityId;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
