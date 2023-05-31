package com.juno.appling.controller.member;

import com.juno.appling.BaseTest;
import com.juno.appling.domain.dto.member.JoinDto;
import com.juno.appling.domain.dto.member.LoginDto;
import com.juno.appling.domain.vo.member.LoginVo;
import com.juno.appling.service.member.MemberAuthService;
import com.juno.appling.service.member.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
class MemberControllerDocs extends BaseTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberAuthService memberAuthService;

    private final String PREFIX = "/api/member";

    @Test
    @DisplayName(PREFIX)
    void member() throws Exception {
        //given
        String email = "juno4@member.com";
        String password = "password";
        JoinDto joinDto = new JoinDto(email, password, "name", "nick", "19941030");
        memberAuthService.join(joinDto);
        LoginDto loginDto = new LoginDto(email, password);
        LoginVo loginVo = memberAuthService.login(loginDto);

        //when
        ResultActions resultActions = mock.perform(
                RestDocumentationRequestBuilders.get(PREFIX)
                .header(AUTHORIZATION, "Bearer "+loginVo.getAccessToken())
        ).andDo(print());

        //then
        resultActions.andDo(docs.document(
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("access token")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.member_id").type(JsonFieldType.NUMBER).description("member id"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("email"),
                        fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("nickname"),
                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("name"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("role"),
                        fieldWithPath("data.created_at").type(JsonFieldType.STRING).description("생성일"),
                        fieldWithPath("data.modified_at").type(JsonFieldType.STRING).description("수정일")
                )
        ));

    }
}