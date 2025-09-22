package com.oocl.tspsolver.client;

import com.oocl.tspsolver.service.HttpService;
import org.springframework.stereotype.Service;

@Service
public class AmapClient {
    public String getDistanceMatrix(String url) throws Exception{
        return HttpService.sendGet(url);
    }
}
