package com.juno.appling.member.domain;

import com.juno.appling.member.enums.MemberApplySellerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class MemberApplySeller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    private Long id;

    @NotNull
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private MemberApplySellerStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private MemberApplySeller(@NotNull Long memberId, MemberApplySellerStatus status,
        LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.memberId = memberId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static MemberApplySeller of(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        return new MemberApplySeller(memberId, MemberApplySellerStatus.APPLY, now, now);
    }

    public void patchApplyStatus(MemberApplySellerStatus status) {
        this.status = status;
    }
}
