package com.bx.implatform.controller;

import com.bx.implatform.service.SessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会话")
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {


    private final SessionService sessionService;





}
