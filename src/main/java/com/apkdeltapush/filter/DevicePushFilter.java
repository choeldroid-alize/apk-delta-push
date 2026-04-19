package com.apkdeltapush.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Filters devices eligible for APK delta push based on configurable criteria.
 */
public class DevicePushFilter {

    private final List<Predicate<DeviceFilterContext>> predicates = new ArrayList<>();

    public DevicePushFilter withMinApiLevel(int minApi) {
        predicates.add(ctx -> ctx.getApiLevel() >= minApi);
        return this;
    }

    public DevicePushFilter withMaxApiLevel(int maxApi) {
        predicates.add(ctx -> ctx.getApiLevel() <= maxApi);
        return this;
    }

    public DevicePushFilter withMinBatteryPercent(int minBattery) {
        predicates.add(ctx -> ctx.getBatteryPercent() >= minBattery);
        return this;
    }

    public DevicePushFilter requireWifi(boolean required) {
        if (required) {
            predicates.add(DeviceFilterContext::isOnWifi);
        }
        return this;
    }

    public DevicePushFilter withSerialPattern(String pattern) {
        predicates.add(ctx -> ctx.getSerial().matches(pattern));
        return this;
    }

    public boolean accepts(DeviceFilterContext context) {
        if (context == null) return false;
        return predicates.stream().allMatch(p -> p.test(context));
    }

    public List<DeviceFilterContext> filter(List<DeviceFilterContext> devices) {
        List<DeviceFilterContext> result = new ArrayList<>();
        for (DeviceFilterContext d : devices) {
            if (accepts(d)) result.add(d);
        }
        return result;
    }

    public int predicateCount() {
        return predicates.size();
    }
}
