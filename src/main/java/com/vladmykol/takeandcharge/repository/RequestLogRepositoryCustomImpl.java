package com.vladmykol.takeandcharge.repository;

import com.apavlidi.exceptions.WrongQueryParam;
import com.vladmykol.takeandcharge.entity.IncomingRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

import static com.apavlidi.core.FilterApplier.*;
import static com.apavlidi.domain.Filter.SEARCH;
import static com.apavlidi.domain.Filter.SORT;

@RequiredArgsConstructor
public class RequestLogRepositoryCustomImpl implements RequestLogRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<IncomingRequest> filter(Map<String, String> restApiParams) {
        var query = toRestApiQuery(restApiParams);
        return mongoTemplate.find(query, IncomingRequest.class);
    }

    public Query toRestApiQuery(Map<String, String> restApiQueries) {
        Query query = new Query();
        customApplySortQueryParam(query, restApiQueries);
        customApplySearchQueryParam(query, restApiQueries);
        applySelectQueryParam(query, restApiQueries);
        applyPageQueryParam(query, restApiQueries);
        applyPageSizeQueryParam(query, restApiQueries);
        return query;
    }

    private void customApplySortQueryParam(Query query, Map<String, String> restApiQueries) {
        try {
            if (restApiQueries.get(SORT.getCode()) != null) {
                String sortQueryParam = restApiQueries.get(SORT.getCode());
                Sort.Direction sortDir =
                        sortQueryParam.charAt(0) == '-' ? Sort.Direction.DESC : Sort.Direction.ASC;
                String sortBy = sortDir.equals(Sort.Direction.DESC) ? sortQueryParam.substring(1)
                        : sortQueryParam;
                query.with(Sort.by(sortDir, sortBy));
            }
        } catch (IndexOutOfBoundsException subStringException) {
            throw new WrongQueryParam();
        }
    }

    private void customApplySearchQueryParam(Query query, Map<String, String> restApiQueries) {
        try {
            if (restApiQueries.get(SEARCH.getCode()) != null) {
                JSONObject jsonCriteria = new JSONObject(restApiQueries.get(SEARCH.getCode()));
                JSONArray keys = jsonCriteria.names();
                for (int i = 0; i < keys.length(); ++i) {
                    String field = keys.getString(i);
                    String value = jsonCriteria.getString(field);
                    query.addCriteria(Criteria.where(field).regex(value));
                }
            }
        } catch (JSONException e) {
            throw new WrongQueryParam("JSON format in <" + SEARCH.getCode() + "> request param is incorrect");
        }
    }
}
