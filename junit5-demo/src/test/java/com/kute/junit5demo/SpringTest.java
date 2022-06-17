package com.kute.junit5demo;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * created by kute at 2022/4/24 下午6:13
 */
@ExtendWith(value = {SpringExtension.class})
@AutoConfigureMockMvc
//@WebMvcTest
public class SpringTest {

    @Autowired
    private MockMvc mockMvc;
}
