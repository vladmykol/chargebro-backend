package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.IncomingRequest;

import java.util.List;
import java.util.Map;

public interface RequestLogRepositoryCustom {
    List<IncomingRequest> filter(Map<String, String> restApiParams);
}
