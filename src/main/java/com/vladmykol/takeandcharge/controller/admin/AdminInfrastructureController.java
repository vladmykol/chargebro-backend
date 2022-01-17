package com.vladmykol.takeandcharge.controller.admin;

import com.vladmykol.takeandcharge.entity.IncomingRequest;
import com.vladmykol.takeandcharge.repository.RequestLogRepositoryCustom;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.apavlidi.core.RestFullAPI.collectRestApiParams;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_ADMIN;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_INFR;

@RestController
@RequestMapping(API_ADMIN + API_INFR)
@RequiredArgsConstructor
public class AdminInfrastructureController {
    private final RequestLogRepositoryCustom requestLogRepository;

    @GetMapping("/logs")
    @Operation(description = "Get all incoming request with filtering available \n\n" +
            "EXAMPLES: \n\n" +
            " == Filtering == \n\n" +
            "1.Filter logs by request that contains the string 'jndi'.\n\n" +
            "GET /logs?q={\"request\":\"jndi\"}\n\n" +
            " == Sorting == \n\n" +
            "1. Order the results by the date.\n\n" +
            "GET /logs?sort=-createDate\n\n" +
            " == Pagination == \n\n" +
            "1. Limit the results by 5 entries.\n\n" +
            "GET /logs?pageSize=5\n\n" +
            "2. Next page of the results after limiting it by 5 entries.\n" +
            "GET /logs?pageSize=5&page=2\n\n" +
            " == Selecting ==\n\n" +
            "1. Retrieve specifically only the request and the method of the results objects\n\n" +
            "GET /logs?select=request,method\n\n" +
            " == All together ==\n\n" +
            "1. Retrieve only the objects that have the string \"swagger\" in the request field. For those objects, only the fields \"request\" and \"method\" is returned sorted by createDate descending. The results are limited by 100 entries.\n\n" +
            "GET /logs?select=request,method&q={\"request\":\"swagger\"}&pageSize=5&sort=createDate")
    public List<IncomingRequest> getAllLogs(@RequestParam(defaultValue = "{\"select\":\"request,method,createDate\", \"q\":\"{request:.*swag.*}\", \"sort\":\"-createDate\"}") Map<String, String> filters) {
        Map<String, String> restApiParams = collectRestApiParams(filters);
        return requestLogRepository.filter(restApiParams);


    }
}
