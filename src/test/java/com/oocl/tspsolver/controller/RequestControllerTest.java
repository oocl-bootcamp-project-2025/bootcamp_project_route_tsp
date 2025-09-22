package com.oocl.tspsolver.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class RequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestController requestController;

//    @Test
//    void should_return_valid_response_when_post_given_valid_data() {
//        String requestBody = """
//                {
//                  "points": [
//                    { "longitude": 116.4074, "latitude": 39.9042 },
//                    { "longitude": 116.3974, "latitude": 39.917 },
//                    { "longitude": 116.0204, "latitude": 40.3584 }
//                  ],
//                  "type": 1
//                }
//                """;
//
//    }
}
