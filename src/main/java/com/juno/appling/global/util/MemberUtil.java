package com.juno.appling.global.util;

import com.juno.appling.global.security.TokenProvider;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.domain.MemberRepository;
import com.juno.appling.member.domain.Seller;
import com.juno.appling.member.domain.SellerRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;

    public Member getMember(HttpServletRequest request) {
        String token = tokenProvider.resolveToken(request);
        Long memberId = tokenProvider.getMemberId(token);
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원입니다."));
    }

    public Seller getSeller(HttpServletRequest request){
        Member member = getMember(request);
        return sellerRepository.findByMember(member)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 판매자입니다."));
    }

}
