package com.roamblue.cloud.management.service;

public interface RuleService {
    void verifyPermission(int userId, int rule);
}
