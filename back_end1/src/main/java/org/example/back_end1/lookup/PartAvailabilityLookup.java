package org.example.back_end1.lookup;

public interface PartAvailabilityLookup {
    record AvailabilityResult(long partId, boolean available, String status) {}
    AvailabilityResult lookupAvailability(long partId);
}