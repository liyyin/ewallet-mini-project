package com.ewallet.ewallet_be.client;

import com.ewallet.ewallet_be.dto.ApiResponse;
import com.ewallet.ewallet_be.dto.UserInfo;
import com.ewallet.ewallet_be.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
@RequiredArgsConstructor
public class UserClient {
    private final RestClient userRestClient;

    public UserInfo getUser(Long userId){
        try {
            ApiResponse<UserInfo> body = userRestClient.get()
                    .uri("/users/{id}",userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<UserInfo>>() {});

            if (body == null || body.data() == null){
                throw new BusinessException(HttpStatus.BAD_GATEWAY,"Invalid Response from user service");
            }
            return body.data();
        }catch (HttpClientErrorException.NotFound e){
            throw new BusinessException(HttpStatus.NOT_FOUND,"User "+ userId + " not found");
        }catch (ResourceAccessException e){
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,"User service is unavailable");
        }catch (RestClientResponseException e){
            throw new BusinessException(HttpStatus.BAD_GATEWAY,"User service returned error");
        }
    }
}
