package com.juno.appling.domain.vo.product;

import com.juno.appling.domain.entity.product.Product;
import com.juno.appling.domain.vo.BaseVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ProductVo extends BaseVo {
    private Long id;
    private String mainTitle;
    private String mainExplanation;
    private String productMainExplanation;
    private String productSubExplanation;
    private int originPrice;
    private int price;
    private String purchaseInquiry;
    private String origin;
    private String producer;
    private String mainImage;
    private String image1;
    private String image2;
    private String image3;
    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;
    private SellerVo seller;
    private CategoryVo category;

    public static ProductVo productReturnVo(Product product){
        return ProductVo.builder()
                .id(product.getId())
                .mainTitle(product.getMainTitle())
                .mainExplanation(product.getMainExplanation())
                .productMainExplanation(product.getProductMainExplanation())
                .productSubExplanation(product.getProductSubExplanation())
                .purchaseInquiry(product.getPurchaseInquiry())
                .producer(product.getProducer())
                .origin(product.getOrigin())
                .originPrice(product.getOriginPrice())
                .price(product.getPrice())
                .mainImage(product.getMainImage())
                .image1(product.getImage1())
                .image2(product.getImage2())
                .image3(product.getImage3())
                .category(CategoryVo.builder()
                        .categoryId(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .createdAt(product.getCategory().getCreatedAt())
                        .modifiedAt(product.getCategory().getModifiedAt())
                        .build())
                .build();
    }
}
