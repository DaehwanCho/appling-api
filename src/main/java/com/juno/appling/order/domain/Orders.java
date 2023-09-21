package com.juno.appling.order.domain;

import com.juno.appling.member.domain.Member;
import com.juno.appling.order.enums.OrdersStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Audited
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private OrdersStatus status;

    private String ordererName;
    private String ordererAddress;
    private String ordererZonecode;
    private String ordererTel;
    private String recipientName;
    private String recipientAddress;
    private String recipientZonecode;
    private String recipientTel;
    private int orderTotalPrice;
    private String etc;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Orders(Member member, OrdersStatus status, String ordererName,
        String ordererAddress, String ordererZonecode, String ordererTel, String recipientName,
        String recipientAddress, String recipientZonecode, String recipientTel, int orderTotalPrice,
        String etc, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.member = member;
        this.status = status;
        this.ordererName = ordererName;
        this.ordererAddress = ordererAddress;
        this.ordererZonecode = ordererZonecode;
        this.ordererTel = ordererTel;
        this.recipientName = recipientName;
        this.recipientAddress = recipientAddress;
        this.recipientZonecode = recipientZonecode;
        this.recipientTel = recipientTel;
        this.orderTotalPrice = orderTotalPrice;
        this.etc = etc;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static Orders of(Member member) {
        LocalDateTime now = LocalDateTime.now();
        return new Orders(member, OrdersStatus.TEMP, "", "", "", "", "", "", "", "", 0, "", now,
            now);
    }
}
