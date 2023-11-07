package com.juno.appling.order.domain.model;

import com.juno.appling.product.domain.model.Option;
import com.juno.appling.product.enums.OptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderOption {
    private Long id;
    private String name;
    private int extraPrice;
    private int ea;
    private OptionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static OrderOption create(List<Option> optionList, Long targetId) {
        Option option = optionList.stream().filter(o -> o.getId().equals(targetId)).findFirst().orElseThrow(
                () -> new IllegalArgumentException("유효하지 않은 옵션입니다. option id = " + targetId)
        );

        return OrderOption.builder()
            .id(option.getId())
            .name(option.getName())
            .extraPrice(option.getExtraPrice())
            .ea(option.getEa())
            .status(option.getStatus())
            .createdAt(option.getCreatedAt())
            .modifiedAt(option.getModifiedAt())
            .build();
    }
}
